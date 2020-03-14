package server;

import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;


public abstract class Server {
	
	// Operate thread
	public class OperateThread implements Callable<String> {
		
		private String operation[];
		
		public OperateThread(String operationInfo[]) {
			this.operation = operationInfo;
		}
		
		@Override
		public String call() throws Exception {
			return operate(operation);
		}
	}

	
	// Central database
	private static ConcurrentHashMap<String, String> store;
	
	// Lock list
	private static LinkedList<String> lockList;
	
	// Port number
	protected int portNum = -1;
	
	// Server constructor
	public Server(int portNum) {
		this.portNum = portNum;
		store = new ConcurrentHashMap<String, String>();
		lockList = new LinkedList<String>();
	}
	
	//
	// Assignment 1
	//
	
	// Put function
	public String put(String key, String value) {
		String putResponse = null;
		
		if (store.containsKey(key)) {
			putResponse = "Server response: key=" + key + " already exists, please try again with a new key";		
		} else {
			store.put(key, value);
			putResponse = "Server response: put key=" + key;
		}
		
		return putResponse;
	}
	
	// Get function
	public String get(String key) {
		String getResponse = null;
		
		if (store.containsKey(key)) {
			String value = store.get(key);
			getResponse = "Server response: get key=" + key + ", get value=" + value;
		} else {
			getResponse = "Server response: key=" + key + " doesn't exist";
		}
		
		return getResponse;
	}
	
	// Delete function
	public String del(String key) {
		String delResponse = null;
		
		if (store.containsKey(key)) {
			store.remove(key);
			delResponse = "Server response: delete key=" + key;
		} else {
			delResponse = "Server response: key=" + key + " doesn't exist";
		}
		
		return delResponse;
	}
	
	// Store function
	public String store() {
		String storeResponse = null;
		
		if (store.isEmpty()) {
			storeResponse = "Server response: store is empty";
		} else {
			Set<String> keys = store.keySet();  
		    
			StringBuilder sb = new StringBuilder("Server response: ");
			for (String key : keys) {
				sb.append("{key=" + key + ", value=" + store.get(key) + "} ");
			}
			
			if (sb.toString().getBytes().length > 65000) {
				byte[] storeByte = sb.toString().getBytes();
				byte[] storeByteTrimmed = new byte[65000];
						
				for (int i=0; i<65000; i++) {		
					storeByteTrimmed[i] = storeByte[i];
				}
				
				storeResponse = storeByteTrimmed.toString();
			} else {
				storeResponse = sb.toString();
			}
		}
		
		return storeResponse;
	}
	
	// Exit function
	public String exit() {
		String exitResponse = null;
		
		portNum = -1;
		exitResponse = "Server response: See you again!";
		return exitResponse;
	}
	
	//
	// Assignment 2
	//
	
	// dput1 function
	public String dput1(String key, String value) {
		String dput1Response = null;
		
		if (lockList.contains(key)) {
			dput1Response = "abort";		
		} else {
			lockList.add(key);;
			dput1Response = "acknowledge";
		}
		
		return dput1Response;
	}
	
	// dput2 function
	public String dput2(String key, String value) {
		String dput2Response = null;
		dput2Response = put(key, value);
		lockList.remove(key);
		
		return dput2Response;
	}
	
	// dputabort function
	public String dputabort(String key) {
		String dputabortReponse = "abort";
		lockList.remove(key);
		
		return dputabortReponse;
	}
	
	// ddel1 function
	public String ddel1(String key) {
		String ddel1Response = null;

		if (lockList.contains(key)) {
			ddel1Response = "abort";		
		} else {
			lockList.add(key);
			ddel1Response = "acknowledge";
		}
		
		return ddel1Response;
	}
	
	// ddel2 function
	public String ddel2(String key) {
		String ddel2Response = null;
		ddel2Response = del(key);
		lockList.remove(key);
		
		return ddel2Response;
	}
	
	// ddelabort function
	public String ddelabort(String key) {
		String ddelabortReponse = "abort";
		lockList.remove(key);
		
		return ddelabortReponse;
	}
	
	//
	// Operation function
	//
	
	// Operation abstract function 
	public String operate(String operation[]) {
		String response = null;
		
		// Determine operation type
		if (operation[0].equals("dput1")) {
        	String key = operation[1];
        	String value = operation[2];
        	response = dput1(key, value);
		} else if (operation[0].equals("dput2")) {
        	String key = operation[1];
        	String value = operation[2];
        	response = dput2(key, value);
		} else if (operation[0].equals("dputabort")) {
			String key = operation[1];
        	//String value = operation[2];
			response = dputabort(key);
		} else if (operation[0].equals("ddel1")) {
	     	String key = operation[1];
	     	response = ddel1(key);
		} else if (operation[0].equals("ddel2")) {
	     	String key = operation[1];
	     	response = ddel2(key);
		} else if (operation[0].equals("ddelabort")) {
	     	String key = operation[1];
	     	response = ddelabort(key);
        } else if (operation[0].equals("get")) {
        	String key = operation[1];
        	response = get(key);
        } else if (operation[0].equals("store")) {	        	
        	response = store();
        } else if (operation[0].equals("exit")) {	        	
        	response = exit();
        }
		
		return response;
	}

	//
	// Abstract function remains to be implemented 
	//
	
	// Listen to the client and execute the request
	public abstract void listenAndExecute();

}
