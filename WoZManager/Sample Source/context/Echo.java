package edu.vt.ece.context;

import java.util.Random;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;

public class Echo implements OscEventListener 
{
	int myPort = new Random().nextInt(50000);
	
	public Echo()
	{
		OscP5 server = new OscP5(this, myPort);
		
		OscMessage echo = new OscMessage(MessageTypes.ECHO);
		echo.add(server.ip());
		echo.add(myPort);
		server.send(echo, new NetAddress(DiscoverServer.DEFAULT_DISCOVER_HOSTNAME,DefaultPorts.DEFAULT_DISCOVER_PORT));
	}

	public static void main(String[] args)
	{
		new Echo();
	}

	@Override
	public void oscEvent(OscMessage theMessage) 
	{
		System.out.println("Client Received Echo...");
		System.exit(0);
	}

	@Override
	public void oscStatus(OscStatus theStatus) {
		// TODO Auto-generated method stub
		
	}
}
