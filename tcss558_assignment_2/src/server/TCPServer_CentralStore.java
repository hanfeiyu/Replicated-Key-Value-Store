package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


public class TCPServer_CentralStore extends Server {
	
	private static final int TIME_INTERVAL = 1000; // 1 second
	private static final int PORT_NUM_CENTRAL_STORE = 4410;
	private static final String QUERY_GET = "GET";
	private static final String QUERY_PUT = "PUT";
	
	private static ConcurrentHashMap<String, Integer> nodeHashMap; 
	private static LinkedList<String> nodeList;
	
	//
	// Constructor
	//
	
	public TCPServer_CentralStore() {
		super(PORT_NUM_CENTRAL_STORE);
		nodeList = new LinkedList<String>();
		nodeHashMap = new ConcurrentHashMap<String, Integer>();
	}
	
	//
	// Helper function 1: list to string
	//
	
	public static String ListToString(LinkedList<String> list) {
		StringBuilder sb = new StringBuilder();
    	
		for (int i=0; i<list.size(); i++) {
    		if (i<list.size()-1) {
    			sb.append(list.get(i) + " ");
    		} else {
    			sb.append(list.get(i));
    		}
    	}
		
		return sb.toString();
	} 
	
	//
	// Helper function 2: string array to string
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
	// Check activity of nodes
	//
	
	public void checkNodes() {
		// Check if any nodes stopped working
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
	
	@Override
	public void listenAndExecute() {
		
		// Set a thread to check activity of nodes first
		new Thread(() -> {
			while (portNum != -1) {
				checkNodes();
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
			    String memberQuery[] = br.readLine().split(" ");	        
			    
			    // Check the type of query
		        if (memberQuery[0].equals(QUERY_PUT)) {
		        	String memberAddr = memberQuery[1];
		        	
		        	nodeHashMap.put(memberAddr, 10);
		        	
		        	if (!nodeList.contains(memberAddr)) {
		        		nodeList.add(memberAddr);
		        	}
		        	
		        } else if (memberQuery[0].equals(QUERY_GET)) {
				    // Write response
			        PrintWriter os = new PrintWriter(socket.getOutputStream());
			        String response = ListToString(nodeList);
				    
				    // Reply to client
		        	os.println(response);
		        	os.flush();
		        	
		        	// Close output utilities
		        	os.close();  
		        }		        
		        
		        // Close other utilities
			    is.close();
			    br.close();
			    socket.close();	    
		    }
		    
		    // Close serverSocket
		    serverSocket.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
