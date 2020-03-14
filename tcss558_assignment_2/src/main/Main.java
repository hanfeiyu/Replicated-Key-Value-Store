package main;

import client.TCPClient;
import server.TCPServer_CentralStore;
import server.TCPServer_KVStore_DF;
import server.TCPServer_KVStore_T;
import server.TCPServer_KVStore_U;


public class Main {
	
	// Show usage if any parameter is null
	public static void showUsage() {
		System.out.println("Usage: \n");
		System.out.println("Client: ");
		System.out.println("	tc <address> <port> put <key> <msg> | TCP CLIENT: Put an object into store");
		System.out.println("	tc <address> <port> get <key> | TCP CLIENT: Get an object from store by key");
		System.out.println("	tc <address> <port> del <key> | TCP CLIENT: Delete an object from store by key");
		System.out.println("	tc <address> <port> store | TCP CLIENT: Display object store");
		System.out.println("	tc <address> <port> exit | TCP CLIENT: Shutdown server\n");
		
		System.out.println("Server: ");
		System.out.println("	ts <membership tracking method> <port> | TCP SERVER\n");
		System.out.println("	*** <membership tracking method>: server tracks membership by, 1 - dynamic config file, 2 - UDP broadcast, 3 - Centralized membership KV store");  
		System.out.println("	*** <port>: run server on <port>\n");   
		System.out.println("	*** When specifying 3 - Centralized membership KV store:"); 
		System.out.println("	*** ts 3 | TCP SERVER for central membership KV store");   
		System.out.println("	*** ts 3 <port> <central store ip address> | TCP SERVER for member KV server");   
	}
	
	// Main function
	public static void main(String[] args) {
		System.out.println("args.length: " + args.length);
		
		if (args.length < 2) {
			System.out.println("bp1");
			showUsage();
			System.exit(0);
		}
		
		// Determine node type
		if (args[0].equals("ts")) {
			if (args.length > 4) {
				showUsage();
				System.out.println("bp2");
				System.exit(0);
			}

			int trackingState = Integer.parseInt(args[1]);
			int portNum = -1;
			switch (trackingState) {
			case 1:
				portNum = Integer.parseInt(args[2]);
				TCPServer_KVStore_DF tcpServerDF = new TCPServer_KVStore_DF(portNum);
				tcpServerDF.listenAndExecute();
				break;
			case 2:
				portNum = Integer.parseInt(args[2]);
				TCPServer_KVStore_U tcpServerU = new TCPServer_KVStore_U(portNum);
				tcpServerU.listenAndExecute();
				break;
			case 3:
				if (args.length == 2) {
					TCPServer_CentralStore tcpServerCS = new TCPServer_CentralStore();
					tcpServerCS.listenAndExecute();
				} else {
					portNum = Integer.parseInt(args[2]);
					String ipAddrCentralStore = args[3];
					
					TCPServer_KVStore_T tcpServerT = new TCPServer_KVStore_T(portNum, ipAddrCentralStore);
					tcpServerT.listenAndExecute();
				}
				break;
			default:
				showUsage();
				System.out.println("bp1");
				System.exit(0);
				break;		
			}
		} else if (args[0].equals("tc")) {
			if (args[3].equals("put") && args.length!=6) {
				showUsage();
				System.exit(0);
			}
			
			if (args[3].equals("get") && args.length!=5) {
				showUsage();
				System.exit(0);
			}
			
			if (args[3].equals("del") && args.length<3 && 6<args.length) {
				showUsage();
				System.exit(0);
			}
			
			if (args[3].equals("store") && args.length!=4) {
				showUsage();
				System.exit(0);
			}
			
			if (args[3].equals("exit") && args.length!=4) {
				showUsage();
				System.exit(0);
			}
			
			String ipAddr = args[1];
			int portNum = Integer.parseInt(args[2]);
			StringBuilder sb = new StringBuilder();
			for (int i=3; i<args.length-1; i++) {
				sb.append(args[i] + " ");
			}
			sb.append(args[args.length-1]);
			String operationInfo = sb.toString();
			
			TCPClient tcpClient = new TCPClient(ipAddr, portNum, operationInfo);
			tcpClient.requestAndListen();
		}
	}
}
