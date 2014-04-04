package edu.vt.ece.context;

import netP5.NetAddress;

public class ContextApplication {

	NetworkClient client = null;
	
	public ContextApplication() 
	{
		client = new NetworkClient("Context Application");
		
		NetAddress ledService=client.findService("LED");
		
		if(ledService==null)
		{
			System.out.println("Couldn't find LED Service");
		}
		else
		{
			System.out.println("Found LED Service");
		}
		
		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}*/
		
		NetAddress FSRWidget = client.findWidget("FSR");
		
		if(FSRWidget == null)
		{
			System.out.println("Couldn't find FSR Widget");
		}
		else
		{
			System.out.println("Found FSR Widget");
		}
		
		String value = "10";
		
		client.commandService("LED", ledService, value.getBytes());
		
		client.shutdownClient("all");
		
		System.exit(0);
	}
	public static void main(String[] args) 
	{
		new ContextApplication();

	}

}
