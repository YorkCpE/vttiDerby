package edu.vt.ece.context;

import netP5.Bytes;
import oscP5.OscP5;

public class LEDService implements ServiceInterface
{
	OscP5 server = null;
	NetworkClient client = null;
	
	int myServicePort = DefaultPorts.DEFAULT_SERVICE_PORT;
	
	public LEDService()
	{
		client = new NetworkClient("LED Service");
		
		client.plug(this, "respondToServiceRequest", MessageTypes.SERVICE_COMMAND);
		client.registerWithDiscover();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		client.registerService("LED", "Service The Controls the Brightness of an LED");
	}
	
	public static void main(String[] args) 
	{
		new LEDService();
	}

	@Override
	public void respondToServiceRequest(String ip, int port, String serviceName, byte[] command) 
	{
		String s = new String(command);
		
		System.out.println("### Set LED to "+s);
	}
}
