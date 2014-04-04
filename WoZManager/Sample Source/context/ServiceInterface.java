package edu.vt.ece.context;

public interface ServiceInterface 
{
	void respondToServiceRequest(String ip, int port, String serviceName, byte[] command);
}
