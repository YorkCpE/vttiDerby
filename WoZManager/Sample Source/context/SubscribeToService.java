package edu.vt.ece.context;

import netP5.NetAddress;

public class SubscribeToService {

	NetworkClient client = null;
	
	public SubscribeToService()
	{
		client = new NetworkClient("SubscribeToService");
		client.registerWithDiscover();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NetAddress host = client.findService("LED");
	}
	
	public static void main(String[] args) 
	{
		new SubscribeToService();
	}

}
