package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class TCPClient extends Client {

	public TCPClient(String ipAddr, int portNum, String operationInfo) {
		super(ipAddr, portNum, operationInfo);
	}

	@Override
	public void requestAndListen() {
		try {
			// Set up socket
			Socket socket = new Socket(ipAddr, portNum);
			
			// Write in operation information
			PrintWriter os = new PrintWriter(socket.getOutputStream());
			os.println(operationInfo);
			os.flush();
			
			// Read response from server	    	
			InputStreamReader is = new InputStreamReader(socket.getInputStream());
		    BufferedReader br = new BufferedReader(is);
			String response = br.readLine();
	    	System.out.println(response);
	    	
	    	// Close all utilities
	    	socket.shutdownOutput();	
	    	socket.shutdownInput();
		    is.close();
		    br.close();
		    os.close();   
		    socket.close();	
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
