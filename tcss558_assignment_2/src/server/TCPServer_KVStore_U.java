package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class TCPServer_KVStore_U extends Server {
	
	private static final int TIME_INTERVAL = 1000; // 1 second
	private static final String UDP_SIGN = "UDP";
	private static final String IP_ADDR_BROADCAST = "255.255.255.255";
	private static final int PORT_NUM_BROADCAST = 4410;
	
	private static final String ABORT_RESPONSE = "Server response: Error! Operation abort!";
	private static final String PHASE_ONE_SUCCEED = "Phase one succeeded!";
	private static final String PHASE_TWO_FAIL = "Phase two failed!";
	
	private static ConcurrentHashMap<String, Integer> nodeHashMap; 
	private static LinkedList<String> nodeList;
	
	//
	// Constructor
	//
	
	public TCPServer_KVStore_U(int portNum) {
		super(portNum);
		nodeHashMap = new ConcurrentHashMap<String, Integer>();
		nodeList = new LinkedList<String>();
	}
	
	//
	// Check activity of nodes
	//
	
	public void checkNodes() {
		for (String key : nodeHashMap.keySet()) {
			Integer timeStamp = nodeHashMap.get(key);
			timeStamp--;
			
			if (timeStamp == 0) {
				nodeHashMap.remove(key);
				nodeList.remove(key);
			} else {
				nodeHashMap.put(key, timeStamp);
			}
		}
	}
	
	//
	// UDP service starts tracking other nodes
	//
	
	public void track() {
		try {
			while (PORT_NUM_BROADCAST != -1) {
				// Check if any nodes stopped working
				checkNodes();
				
				// Set up socket
				DatagramSocket socket = new DatagramSocket(PORT_NUM_BROADCAST);
				
				// Set up packet
				byte[] requestByte = new byte[1024];
				DatagramPacket requestPacket = new DatagramPacket(requestByte, requestByte.length);
				
				// Receive request
				socket.receive(requestPacket);
				InetAddress inetAddr = requestPacket.getAddress();
				String response = new String(requestByte, 0, requestPacket.getLength());
//				System.out.println(response);
				
				// Get addresses from other nodes
				String responseSplit[] = response.split(" ");
				if (responseSplit[0].equals(UDP_SIGN)) {
					String memberIpAddr = inetAddr.getHostAddress();
					int memberPortNum = Integer.parseInt(responseSplit[1]);
					
					String memberAddr = memberIpAddr + ":" + memberPortNum;
					nodeHashMap.put(memberAddr, 10);
					
					if (!nodeList.contains(memberAddr)) {
						nodeList.add(memberAddr);
					}
					
//					System.out.println("nodeHashMap: " + nodeHashMap);
				}
				
				// Close utilities
				socket.close();
				
				// Listen per 1 second
				Thread.sleep(TIME_INTERVAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//
	// UDP service starts broadcast 
	//
	
	public void broadcast() {	
		try {
			while (PORT_NUM_BROADCAST != -1) {
				// Send request
				InetAddress inetAddr = InetAddress.getByName(IP_ADDR_BROADCAST);
				byte[] request = (UDP_SIGN + " " + portNum).getBytes();
				DatagramPacket requestPacket = new DatagramPacket(request, request.length, inetAddr, PORT_NUM_BROADCAST);
				DatagramSocket socket = new DatagramSocket();
				socket.send(requestPacket);
				
				// Close utilities
				socket.close();
				
				// Broadcast per 1 second
				Thread.sleep(TIME_INTERVAL);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//
	// Helper function: string array to string
	//
	
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
//        	
//            System.out.println("before check leader itself");
//            System.out.println("leaderIpAddr: " + leaderIpAddr);
//            System.out.println("memberIpAddr: " + memberIpAddr);   
//            System.out.println("equals? -> " + leaderIpAddr.equals(memberIpAddr));   
            if (leaderIpAddr.equals(memberIpAddr) && memberPortNum == portNum) {
//            	System.out.println("It's leader!");
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
            	
//            	System.out.println("phase one, leaderResponse: " + leaderResponse);
            	
            	continue;
        	}
//            System.out.println("before check leader itself");
        	
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

//        	System.out.println("phaseOneResponse: " + phaseOneResponse);
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
//            	System.out.println("phase two, leaderResponse: " + leaderResponse);
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
//    			System.out.println("phase two, memberResponse: " + memberResponse);
    			
//    			if (!memberResponse.equals(leaderResponse)) {
//    				phaseTwoResponse = PHASE_TWO_FAIL;
//    			} else {
//    				phaseTwoResponse = leaderResponse;
//    			}
    	    	
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
        
//        System.out.println("phase two, phaseTwoResponse: " + phaseTwoResponse);
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
//		System.out.println("algorithm, response: " + response);
		return response;
	}
	
	
	@Override
	public void listenAndExecute() {	
		

		// Set a thread to broadcast every second
		new Thread(() -> {
			broadcast();
		}).start();
		
		// Set a thread to track membership every second
		new Thread(() -> {
			track();
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
		        
		    	// Analyze message and execute it in parallel
//		        OperateThread serverThread = new OperateThread(operation);
//		        FutureTask<String> futureTask = new FutureTask<String>(serverThread);
//		        Thread thread = new Thread(futureTask);
//		        thread.start();
//		        
//		        String response = null;
//				try {
//					response = futureTask.get();
//				
//				} catch (InterruptedException | ExecutionException e) {
//					e.printStackTrace();
//				}
		        
		        String response = null;
		        
		        // Check if the operation is put or del
		        if (operation[0].equals("put") || operation[0].equals("del")) {
		        	//
		        	// Leader has arise!!!
		        	//
//		        	System.out.println("twoPhaseAlgorithm");
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
