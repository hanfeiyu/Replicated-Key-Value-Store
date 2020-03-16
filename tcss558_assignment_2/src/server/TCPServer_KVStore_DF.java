package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


public class TCPServer_KVStore_DF extends Server {
	
	private static final String FILE_PATH = "/tmp/nodes.cfg";
	private static final int TIME_INTERVAL = 1000; // 1 second
	private static final String ABORT_RESPONSE = "Server response: Error! Operation abort!";
	private static final String PHASE_ONE_SUCCEED = "Phase one succeeded!";
	
	private static LinkedList<String> nodeList;
	
	//
	// Constructor
	//
	
	public TCPServer_KVStore_DF(int portNum) {
		super(portNum);
		nodeList = new LinkedList<String>();
	}
	
	// Helper function
	public static String StringArrayToString(String operation[]) {
		StringBuilder sb = new StringBuilder();
    	
		for (int i=0; i<operation.length; i++) {
    		if (i<operation.length-1) {
    			sb.append(operation[i] + " ");
    		} else {
    			sb.append(operation[i]);
    		}
    	}
		
		return sb.toString();
	}
	
	//
	// Reload function ("/tmp/node.cfg")
	//
	
	public static void ReloadConfigFile(String fileName) {  
        File file = new File(fileName);  
        BufferedReader reader = null;  
        
        // First clear the node list, then reload it
        nodeList.clear();
        try {  
            reader = new BufferedReader(new FileReader(file));  
            String line = null;  
            
            while ((line = reader.readLine()) != null) {  
            	nodeList.add(line);
            }  
            reader.close();  
            
        } catch (IOException e) {  
            e.printStackTrace();  
        } 
    }  
	
	//
	// Roll back function
	//
	
	@SuppressWarnings("static-access")
	public String rollBackPreviousNodes(int index, String abortOperation[]) {
		String abortResponse = null;
		
		// No server needs roll back if index = 0
		if (index == 0) {
			abortResponse = "No server needs roll back";
		} else {
			// Roll back previous nodes before node_index
			for (int i=0; i<index; i++) {
	        	String nodeAddr[] = nodeList.get(i).split(":");
	        	String memberIpAddr = nodeAddr[0];
	        	int memberPortNum = Integer.parseInt(nodeAddr[1]);
	        	
	        	// Check if the member address is leader itself
	        	InetAddress leaderInetAddress = null;
	        	String leaderIpAddr = null;
	            try {
	            	leaderInetAddress = leaderInetAddress.getLocalHost();
	            	leaderIpAddr = leaderInetAddress.getHostAddress();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        	
	        	if (leaderIpAddr.equals(memberIpAddr) && memberPortNum == portNum) {
	        		String response = operate(abortOperation);
	    			if (response.equals("abort")) {
	    				abortResponse = "Roll back succeeded!";
	    			} else {
	    				abortResponse = "Roll back failed!";
	    			}
	    			
	    			continue;
	        	}
	        	
	        	// Servers that aren't leader itself
	        	try {
	    			// Set up socket
	    			Socket leaderSocket = new Socket(memberIpAddr, memberPortNum);
	    			
	    			// Write in operation information
	    			PrintWriter leaderOs = new PrintWriter(leaderSocket.getOutputStream());
	    			leaderOs.println(StringArrayToString(abortOperation));
	    			leaderOs.flush();
	    			
	    			// Read response from server	    	
	    			InputStreamReader leaderIs = new InputStreamReader(leaderSocket.getInputStream());
	    		    BufferedReader leaderBr = new BufferedReader(leaderIs);
	    			String response = leaderBr.readLine();
	    			
	    			if (response.equals("abort")) {
	    				abortResponse = "Roll back succeeded!";
	    			} else {
	    				abortResponse = "Roll back failed!";
	    			}
	    	    	
	    	    	// Close all utilities
	    	    	leaderSocket.shutdownOutput();	
	    	    	leaderSocket.shutdownInput();
	    	    	leaderIs.close();
	    		    leaderBr.close();
	    		    leaderOs.close();   
	    		    leaderSocket.close();	
	    		    
	    		} catch (IOException e) {
	    			e.printStackTrace();
	    		}
	        }   
		}
		
		return abortResponse;
	}
	
	//
	// Phase one
	//
	
	@SuppressWarnings("static-access")
	public String phaseOne(String operation[]) {
		String phaseOneResponse = null; 
		String leaderOperation[] = operation.clone();
		String memberOperation[] = operation.clone();
		String abortOperation[] = operation.clone();
		
		// First try dput1 operation
    	if (operation[0].equals("put")) {
    		leaderOperation[0] = "dput1";
    		memberOperation[0] = "dput1";
    	} else if (operation[0].equals("del")) {
    		leaderOperation[0] = "ddel1";
    		memberOperation[0] = "ddel1";
    	}
    	
        // Call the operate function for every node in the network
        for (int i=0; i<nodeList.size(); i++) {
        	String nodeAddr[] = nodeList.get(i).split(":");
        	String memberIpAddr = nodeAddr[0];
        	int memberPortNum = Integer.parseInt(nodeAddr[1]);
        	
        	// Check if the member address is leader itself
        	InetAddress leaderInetAddress = null;
        	String leaderIpAddr = null;
            try {
            	leaderInetAddress = leaderInetAddress.getLocalHost();
            	leaderIpAddr = leaderInetAddress.getHostAddress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
            if (leaderIpAddr.equals(memberIpAddr) && memberPortNum == portNum) {
            	String leaderResponse = operate(leaderOperation);
            	
            	if (leaderResponse.equals("abort")) {
            		// If operation is aborted, try up to 10 more times
        	        int cnt = 0;
        			while (leaderResponse.equals("abort") && cnt<10) {
        				leaderResponse = operate(leaderOperation);
        				cnt++;
        			}
        			
        			// Return error message if still abort
        			if (leaderResponse.equals("abort")) {
        				phaseOneResponse = ABORT_RESPONSE;
        				rollBackPreviousNodes(i, abortOperation);
        				return phaseOneResponse;
        			}
            	}
            	
            	
            	continue;
        	}
        	
        	// Servers that aren't leader itself
        	try {
    			// Set up socket
    			Socket leaderSocket = new Socket(memberIpAddr, memberPortNum);
    			
    			// Write in operation information
    			PrintWriter leaderOs = new PrintWriter(leaderSocket.getOutputStream());
    			leaderOs.println(StringArrayToString(memberOperation));
    			leaderOs.flush();
    			
    			// Read response from server	    	
    			InputStreamReader leaderIs = new InputStreamReader(leaderSocket.getInputStream());
    		    BufferedReader leaderBr = new BufferedReader(leaderIs);
    			String memberResponse = leaderBr.readLine();
    			
    			// If operation is aborted, try up to 10 more times
		        int cnt = 0;
				while (memberResponse.equals("abort") && cnt<10) {
					// Write in operation information
					leaderOs.println(StringArrayToString(memberOperation));
	    			leaderOs.flush();
	    			
	    			// Read response from server	    	
	    			memberResponse = leaderBr.readLine();
	    			
					cnt++;
				}
				
				// Roll back previous nodes if any abort messages received
				if (memberResponse.equals("abort")) {
					if (operation[0].equals("put")) {
			    		abortOperation[0] = "dputabort";
			    	} else if (operation[0].equals("del")) {
			    		abortOperation[0] = "ddelabort";
			    	}
					
					rollBackPreviousNodes(i, abortOperation);
					phaseOneResponse = ABORT_RESPONSE;
				}
    	    	
    	    	// Close all utilities
    	    	leaderSocket.shutdownOutput();	
    	    	leaderSocket.shutdownInput();
    	    	leaderIs.close();
    		    leaderBr.close();
    		    leaderOs.close();   
    		    leaderSocket.close();	
    		    
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

        	if (phaseOneResponse == null) {
        		continue;
        	} else if (phaseOneResponse.equals(ABORT_RESPONSE)) {
        		return phaseOneResponse;
        	}
        }   

        // After acknowledgement from all nodes, phase one succeeded
        phaseOneResponse = PHASE_ONE_SUCCEED;
        return phaseOneResponse;
	}
	
    //
    // Phase two
    // 
	
	@SuppressWarnings("static-access")
	public String phaseTwo(String operation[]) {
		String phaseTwoResponse = null;
		String leaderOperation[] = operation.clone();
		String memberOperation[] = operation.clone();

        // All nodes acknowledge dput1, then continue dput2
    	if (operation[0].equals("put")) {
    		leaderOperation[0] = "dput2";
    		memberOperation[0] = "dput2";
    	} else if (operation[0].equals("del")) {
    		leaderOperation[0] = "ddel2";
    		memberOperation[0] = "ddel2";
    	}
    	
        // Call the operate function for every node in the network
        for (int i=0; i<nodeList.size(); i++) {
        	String nodeAddr[] = nodeList.get(i).split(":");
        	String memberIpAddr = nodeAddr[0];
        	int memberPortNum = Integer.parseInt(nodeAddr[1]);
        	
        	// Check if the member address is leader itself
        	InetAddress leaderInetAddress = null;
        	String leaderIpAddr = null;
            try {
            	leaderInetAddress = leaderInetAddress.getLocalHost();
            	leaderIpAddr = leaderInetAddress.getHostAddress();
            } catch (Exception e) {
                e.printStackTrace();
            }
        	
            if (leaderIpAddr.equals(memberIpAddr) && memberPortNum == portNum) {
            	String leaderResponse = operate(leaderOperation);
            	phaseTwoResponse = leaderResponse;
            	
            	continue;
        	}
        	
        	try {
    			// Set up socket
    			Socket leaderSocket = new Socket(memberIpAddr, memberPortNum);
    			
    			// Write in operation information
    			PrintWriter leaderOs = new PrintWriter(leaderSocket.getOutputStream());
    			leaderOs.println(StringArrayToString(memberOperation));
    			leaderOs.flush();
    			
    			// Read response from server	    	
    			InputStreamReader leaderIs = new InputStreamReader(leaderSocket.getInputStream());
    		    BufferedReader leaderBr = new BufferedReader(leaderIs);
    			String memberResponse = leaderBr.readLine();

    			// Close all utilities
    	    	leaderSocket.shutdownOutput();	
    	    	leaderSocket.shutdownInput();
    	    	leaderIs.close();
    		    leaderBr.close();
    		    leaderOs.close();   
    		    leaderSocket.close();	
    		    
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
        }
        
        return phaseTwoResponse;
	}
	
	//
	// Two-phase algorithm
	//
	
	public String twoPhaseAlgorithm(String operation[]) {
		String response = null;
		String phaseOneResponse = null;
		String phaseTwoResponse = null;
		
		// Phase One
		phaseOneResponse = phaseOne(operation);
		if (phaseOneResponse.equals(ABORT_RESPONSE)) {
			response = ABORT_RESPONSE;
			return response;
		} 
		
		// Phase two
		phaseTwoResponse = phaseTwo(operation);
		response = phaseTwoResponse;
		return response;
	}
	
	
	@Override
	public void listenAndExecute() {	
		
    	// Set a thread to reload the node list every second
    	new Thread (() -> {
    		while (portNum != -1) {
    			ReloadConfigFile(FILE_PATH);
    			try {
					Thread.sleep(TIME_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}).start();
    	
		try {
	    	// Set up server socket
			ServerSocket serverSocket = new ServerSocket(portNum);
			
		    // Start listening
		    while (portNum != -1) {		    	
		    	// Receive request
		    	Socket socket = serverSocket.accept();
			
		    	// Read in message
				InputStreamReader is = new InputStreamReader(socket.getInputStream());
			    BufferedReader br = new BufferedReader(is);
			    String operation[] = br.readLine().split(" ");	        
				
		        // Write response
		        PrintWriter os = new PrintWriter(socket.getOutputStream());
		        String response = null;
		        
		        // Check if the operation is put or del
		        if (operation[0].equals("put") || operation[0].equals("del")) {
		        	//
		        	// Leader has arise!!!
		        	//
		        	response = twoPhaseAlgorithm(operation);
		        } else {
		        	// Analyze message and execute it while operation is not put or del
		        	response = operate(operation);
		        }

	        	// Reply to client
	        	os.println(response);
	        	os.flush();
		        
		        // Close all utilities
				socket.shutdownInput();	
		    	socket.shutdownOutput();
			    is.close();
			    br.close();
			    os.close();   
			    socket.close();	    
		    }
		    
		    // Close serverSocket
		    serverSocket.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
