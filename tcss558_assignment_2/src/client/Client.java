package client;

public abstract class Client {
	
	protected String ipAddr = null;
	protected int portNum = -1;
	protected String operationInfo;

	public Client(String ipAddr, int portNum, String operationInfo) {
		this.ipAddr = ipAddr;
		this.portNum = portNum;
		this.operationInfo = operationInfo;
	}
	
	public abstract void requestAndListen();
}
