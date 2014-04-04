package edu.vt.ece.context;

import java.util.Random;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;

public class NetworkClient implements OscEventListener
{
	//default values, in practice we only listen to client port for everything
	private int myClientPort = DefaultPorts.DEFAULT_CLIENT_PORT;
	private int myWidgetPort = DefaultPorts.DEFAULT_WIDGET_PORT;
	private int myServicePort = DefaultPorts.DEFAULT_SERVICE_PORT;
	
	static final int DEFAULT_DISCOVER_PORT = DefaultPorts.DEFAULT_DISCOVER_PORT;
	static final String DEFAULT_DISCOVER_NAME = DiscoverServer.DEFAULT_DISCOVER_HOSTNAME;
	
	private static String DISCOVER_ACK_FUNC = "serverRegAck";
	private static String WIDGET_ACK_FUNC = "widgetRegistrationAck";
	private static String WIDGET_SUB_ACK = "widgetSubscriptionAck";
	private static String SERVICE_REG_ACK = "serviceRegistrationAck";
	private static String ECHO_RESPONSE = "echoResponse";
	
	String myName = "client";
	private int CURRENT_DISCOVER_PORT=DefaultPorts.DEFAULT_DISCOVER_PORT;
	
	NetAddress discoveryServer = null;
	NetAddress discoverWidgetPort = null;
	NetAddress discoverServicePort=null;
	
	OscP5 localServer = null;

	private boolean containsWidget=false;
	private boolean containsService=false;
	
	public NetworkClient(String name)
	{
		myName=name;
		myClientPort = new Random().nextInt(50000);
		myWidgetPort = myClientPort;
		myServicePort = myClientPort;
		
		localServer = new OscP5(this, myClientPort);
		localServer.plug(this, DISCOVER_ACK_FUNC, MessageTypes.DISCOVER_DHCP_ACK);
		localServer.plug(this, WIDGET_ACK_FUNC, MessageTypes.WIDGET_REGISTRATION_ACK);
		localServer.plug(this, WIDGET_SUB_ACK, MessageTypes.WIDGET_SUBSCRIBE_ACK);
		localServer.plug(this, SERVICE_REG_ACK, MessageTypes.SERVICE_REGISTRATION_ACK);
		localServer.plug(this, ECHO_RESPONSE, MessageTypes.ECHO_ACK);
	}

	public void echoResponse(String ip, int port)
	{
		NetAddress host = new NetAddress(ip,port);
		OscMessage echo = new OscMessage(MessageTypes.ECHO_ACK);
		OscP5.flush(echo, host);
		
	}
	
	public void commandService(String serviceName, NetAddress serviceAddress, byte[] command)
	{
		OscMessage commandMsg = new OscMessage(MessageTypes.SERVICE_COMMAND);
		commandMsg.add(localServer.ip());
		commandMsg.add(myClientPort);
		commandMsg.add(serviceName);
		commandMsg.add(command);
		
		OscP5.flush(commandMsg, serviceAddress);
	}

	public NetAddress findWidget(String widgetName)
	{
		return new SpawnLookupServer().find("widget", widgetName);
	}
	
	public NetAddress findService(String serviceName)
	{
		return new SpawnLookupServer().find("service", serviceName);
	}

	public void widgetSubscriptionAck(int success, String ip, int port)
	{
		if(success==1)
			System.out.println("### Widget Subscription was successful...");
		else
			System.out.println("### Widget Subscription Failed...");
	}
	
	public void shutdownClient(String shutdownCommand)
	{
		if(discoveryServer==null)
			return;
		
		OscMessage shutdownMsg = new OscMessage(MessageTypes.SIGN_OFF);
		shutdownMsg.add(localServer.ip());
		shutdownMsg.add(myClientPort);
		shutdownMsg.add(shutdownCommand);
		localServer.send(shutdownMsg,discoveryServer);
		
	/*	if(containsWidget==true)
		{
			OscMessage shutdownWidget = new OscMessage(MessageTypes.SIGN_OFF);
			shutdownWidget.add(localServer.ip());
			shutdownWidget.add(myWidgetPort);
			shutdownWidget.add("widget");
			localServer.send(shutdownWidget,discoveryServer);
		}
		
		if(containsService==true)
		{
			OscMessage shutdownWidget = new OscMessage(MessageTypes.SIGN_OFF);
			shutdownWidget.add(localServer.ip());
			shutdownWidget.add(myServicePort);
			shutdownWidget.add("service");
			localServer.send(shutdownWidget,discoveryServer);
		}*/
	}
	
	public void updateDiscoverWidgetData(String widgetName, byte[] data)
	{
		if(containsWidget==false)
			return;
		
		OscMessage updateMessage = new OscMessage(MessageTypes.WIDGET_UPDATE);
		updateMessage.add(localServer.ip());
		updateMessage.add(myWidgetPort);
		updateMessage.add(widgetName);
		updateMessage.add(data);
		
		localServer.send(updateMessage,discoveryServer);
	}
	public void registerWidget(String widgetName, String widgetDescription)
	{
		if(discoveryServer==null)
		{
			return;
		}
		
		OscMessage regMessage = new OscMessage(MessageTypes.WIDGET_REGISTRATION);
		regMessage.add(localServer.ip());
		regMessage.add(myWidgetPort);
		regMessage.add(widgetName);
		regMessage.add(widgetDescription);
		localServer.send(regMessage,discoveryServer);
	}
	
	public void widgetRegistrationAck(int port)
	{
		System.out.println("### Widget Registered with Discover");
		discoverWidgetPort = new NetAddress(discoveryServer.address(),port);
		containsWidget=true;
	}
	
	
	public void registerService(String serviceName, String serviceDescription)
	{
		if(discoveryServer==null)
		{
			return;
		}
		
		OscMessage regMessage = new OscMessage(MessageTypes.SERVICE_REGISTRATION);
		regMessage.add(localServer.ip());
		regMessage.add(myServicePort);
		regMessage.add(serviceName);
		regMessage.add(serviceDescription);
		localServer.send(regMessage,discoveryServer);
	}
	
	public void serviceRegistrationAck(int port)
	{
		System.out.println("### Service Registered With Discover");
		discoverServicePort = new NetAddress(discoveryServer.address(),port);
		containsService=true;
	}
	
	public void registerWithDiscover()
	{
		OscMessage myOscMessage = new OscMessage(MessageTypes.DISCOVER_DHCP);
		myOscMessage.add(localServer.ip());
		myOscMessage.add(myClientPort);
		myOscMessage.add(myName);
		
		//localServer.send(myOscMessage,new NetAddress("255.255.255.255",DEFAULT_DISCOVER_PORT));
		localServer.send(myOscMessage,new NetAddress(DiscoverServer.DEFAULT_DISCOVER_HOSTNAME,DefaultPorts.DEFAULT_DISCOVER_PORT));
	}
	
	public void serverRegAck(String discoverIp, int port)
	{
		CURRENT_DISCOVER_PORT=port;
		//discoveryServer = new NetAddress(discoverIp, CURRENT_DISCOVER_PORT);
		discoveryServer = new NetAddress(DiscoverServer.DEFAULT_DISCOVER_HOSTNAME, CURRENT_DISCOVER_PORT);
		
		System.out.println("### Client Registered with "+discoveryServer);
	}
	
	@Override
	public void oscEvent(OscMessage message)
	{
		if(message.isPlugged())
		{
			return;
		}
		System.out.println("### Client Unplugged message...");
		System.out.println("### Addr "+message.addrPattern()+" Type Tag "+message.typetag());
	}

	@Override
	public void oscStatus(OscStatus theStatus) 
	{
		System.out.println("### Client OSC Status Message...");
		
	}

	public void subscribeToWidget(String widgetName) 
	{
		OscMessage subRequest = new OscMessage(MessageTypes.WIDGET_SUBSCRIPTION_REQ);
		subRequest.add(localServer.ip());
		subRequest.add(myClientPort);
		subRequest.add(widgetName);
		
		localServer.send(subRequest,discoveryServer);
	}

	public void plug(Object object, String methodName, String messageAddr) 
	{
		localServer.plug(object, methodName, messageAddr);
	}
	
	private class SpawnLookupServer implements OscEventListener
	{
		NetAddress response=null;
		
		public NetAddress find(String type, String object)
		{
			int portNum = new Random().nextInt(50000)+1000;

			OscP5 server = new OscP5(this, portNum);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			OscMessage lookupMessage = new OscMessage(MessageTypes.LOOKUP);
			lookupMessage.add(type);
			lookupMessage.add(object);
			lookupMessage.add(server.ip());
			lookupMessage.add(portNum);
			
			if(discoveryServer==null)
			{
				server.send(lookupMessage,new NetAddress(DiscoverServer.DEFAULT_DISCOVER_HOSTNAME,DefaultPorts.DEFAULT_DISCOVER_PORT));
			}
			else
			{
				server.send(lookupMessage, discoveryServer);
			}
			
			
			long startTime = System.currentTimeMillis();
			while(System.currentTimeMillis()-startTime<20000 && response==null){Thread.yield();}
			
			server.dispose();
			return response;
		}

		@Override
		public void oscEvent(OscMessage theMessage) 
		{
			if(theMessage.isPlugged())
				return;
			
			if(theMessage.checkAddrPattern(MessageTypes.LOOKUP_ACK))
			{
				String ip = theMessage.get(0).stringValue();
				int port = theMessage.get(1).intValue();
				response = new NetAddress(ip, port);
			}
			
		}

		@Override
		public void oscStatus(OscStatus theStatus) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public static void main(String[] args)
	{
		new NetworkClient("default client").registerWithDiscover();
	}
}
