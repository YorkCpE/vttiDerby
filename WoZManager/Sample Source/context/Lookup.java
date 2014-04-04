package edu.vt.ece.context;

import netP5.NetAddress;

public class Lookup  {

	NetworkClient client = null;
	
	public Lookup() 
	{
		client = new NetworkClient("Looking up LED");
		NetAddress lookup = client.findService("LED");
		
		if(lookup==null)
		{
			System.out.println("Couldn't find service...");
		}
		else
		{
			System.out.println("Found service at "+lookup);
		}
		
		client.shutdownClient("all");
		
		System.exit(0);
		
	}
	public static void main(String[] args) 
	{
		new Lookup();

	}
}
