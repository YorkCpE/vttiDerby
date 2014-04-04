package edu.vt.ece.context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import netP5.NetAddress;
import netP5.NetAddressList;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;

public class DiscoverServer implements OscEventListener {
	
	static final String DEFAULT_DISCOVER_HOSTNAME = "netserver";
	
	private static final String DISCOVER_DHCP_FUNC = "respondToClientRegistration";
	private static final String WIDGET_REGISTRATION_FUNC = "respondToWRegistration";
	//private static final String WIDGET_UNREGISTRATION_FUNC = "unregisterWidget";
	private static final String ECHO_FUNC = "respondToEcho";
	private static final String SHUTDOWN_FUNC = "purgeClient";
	private static final String WIDGET_UPDATE_FUNC = "recvWidgetUpdate";
	private static final String WIDGET_SUB_REQUEST = "widgetSubRequest";
	private static final String SERVICE_REGISTRATION_FUNC="serviceRegistration";
	
	private static final String LOOKUP_FUNC = "lookupObject";
	
	OscP5 server = null;
	NetAddress myNetAddress = null;
	NetAddressList allHosts = new NetAddressList();
	
	List<ServiceRegistration> knownServices = new LinkedList<ServiceRegistration>();
	List<WidgetRegistration> knownWidgets = new LinkedList<WidgetRegistration>();
	
	HashMap<NetAddress, byte[]> widgetData = new HashMap<NetAddress, byte[]>();
	HashMap<String, List<NetAddress>> widgetSubscribers = new HashMap<String, List<NetAddress>>();
	
	public DiscoverServer() 
	{
		OscP5 server = new OscP5(this, DefaultPorts.DEFAULT_DISCOVER_PORT);
		server.plug(this, DISCOVER_DHCP_FUNC, MessageTypes.DISCOVER_DHCP);
		server.plug(this, WIDGET_REGISTRATION_FUNC, MessageTypes.WIDGET_REGISTRATION);
		server.plug(this, ECHO_FUNC, MessageTypes.ECHO);
		server.plug(this, SHUTDOWN_FUNC, MessageTypes.SIGN_OFF);
		server.plug(this, WIDGET_UPDATE_FUNC, MessageTypes.WIDGET_UPDATE);
		server.plug(this, WIDGET_SUB_REQUEST, MessageTypes.WIDGET_SUBSCRIPTION_REQ);
		server.plug(this, SERVICE_REGISTRATION_FUNC, MessageTypes.SERVICE_REGISTRATION);
		
		server.plug(this, LOOKUP_FUNC, MessageTypes.LOOKUP);
	
		myNetAddress = new NetAddress(server.ip(), DefaultPorts.DEFAULT_DISCOVER_PORT);
	}

	public static void main(String[] args) 
	{
		new DiscoverServer();

	}
	
	void lookupObject(String type, String objectName, String ip, int port)
	{	
		NetAddress requestor = new NetAddress(ip, port);
		
		//System.out.println("### Server Lookup Request "+type +" "+objectName+ " from "+requestor);
		
		boolean found=false;
		if (type.equals("service")) 
		{
			for (ServiceRegistration s : knownServices) 
			{
				if (s.serviceName.equals(objectName)) 
				{
					OscMessage responseMessage = new OscMessage(MessageTypes.LOOKUP_ACK);
					responseMessage.add(s.netAddress.address());
					responseMessage.add(s.netAddress.port());
					spawnMessage(responseMessage, requestor);
					found=true;
				}
			}
		}
		else if(type.equals("widget") && !found)
		{
			for (WidgetRegistration w : knownWidgets) 
			{
				if (w.widgetName.equals(objectName)) 
				{
					OscMessage responseMessage = new OscMessage(MessageTypes.LOOKUP_ACK);
					responseMessage.add(w.netAddress.address());
					responseMessage.add(w.netAddress.port());
					spawnMessage(responseMessage, requestor);
					found=true;
				}
			}
		}
		
		if(!found)
		{
			OscMessage responseMessage = new OscMessage(MessageTypes.LOOKUP_ACK);
			responseMessage.add("");
			responseMessage.add(-1);
			spawnMessage(responseMessage, requestor);
		}
	}
	
	public void serviceRegistration(String ip, int port, String serviceName, String serviceDescription)
	{
		NetAddress host = new NetAddress(ip, port);
		ServiceRegistration newReg = new ServiceRegistration(host, serviceName, serviceDescription);
		knownServices.add(newReg);
		
		OscMessage responseMessage= new OscMessage(MessageTypes.SERVICE_REGISTRATION_ACK);
		responseMessage.add(DefaultPorts.DEFAULT_DISCOVER_PORT);
		spawnMessage(responseMessage, host);
		
		System.out.println("### Received Service Registation");
		System.out.println(serviceName+" "+host+" "+serviceDescription);
	}
	
	public void widgetSubRequest(String ip, int port, String widgetName)
	{
		NetAddress requestHost = new NetAddress(ip,port);
		
		boolean success = false;
		WidgetRegistration foundWidget = null;
		
		//add to subscribers list and create list if necessary
		for(WidgetRegistration widget : knownWidgets)
		{
			if(widget.widgetName.equals(widgetName))
			{
				foundWidget = widget;
				
				List<NetAddress> subscribers = widgetSubscribers.get(widgetName);
				if(subscribers==null)
				{
					subscribers = new LinkedList<NetAddress>();
					widgetSubscribers.put(widgetName, subscribers);
				}
				subscribers.add(requestHost);
				success=true;
			}
		}
		
		OscMessage responseMessage = new OscMessage(MessageTypes.WIDGET_SUBSCRIBE_ACK);
		if(success)
			responseMessage.add(1);
		else
			responseMessage.add(0);
		
		if(foundWidget==null)
		{
			responseMessage.add("empty");
			responseMessage.add(-1);
		}
		else
		{
			responseMessage.add(foundWidget.netAddress.address());
			responseMessage.add(foundWidget.netAddress.port());
		}
	
		spawnMessage(responseMessage, requestHost);
	}

	public void recvWidgetUpdate(String ip, int port, String widgetName, byte[] data)
	{
		NetAddress host = new NetAddress(ip,port);
		
		widgetData.put(host, data);
		
		//System.out.println("### Received update from Widget at "+host+": "+s);
		
		new UpdateWidgetSubscribers(widgetName,host,data).start();
	}
	 
	
	public void purgeClient(String ip, int port, String commandMsg)
	{
		NetAddress removeHost = new NetAddress(ip, port);
		
		//purge all client, widget, and service objects
		//associated with this client
		if(commandMsg.equals("client") || commandMsg.equals("all"))
		{
			if(allHosts.contains(ip, port))
			{
				allHosts.remove(ip, port);
				System.out.println("### Removed Client at "+removeHost);
			}
		}
		else if(commandMsg.equals("widget") || commandMsg.equals("all"))
		{
			for(WidgetRegistration w : knownWidgets)
			{
				if(w.netAddress.equals(removeHost))
				{
					if(knownWidgets.remove(w))
						System.out.println("### Removed Widget at "+removeHost);
					else
						System.out.println("### Failed to Remove Widget at "+removeHost);
				}
					
			}
		}
		else if(commandMsg.equals("service") || commandMsg.equals("all"))
		{
			for(ServiceRegistration s : knownServices)
			{
				if(s.netAddress.equals(removeHost))
				{
					if(knownServices.remove(s))
						System.out.println("### Removed Service at "+removeHost);
					else
						System.out.println("### Failed to Remove Service at "+removeHost);
				}
			}
		}
	}
	public void respondToEcho(String ip, int port)
	{
		OscMessage echoMsg = new OscMessage(MessageTypes.ECHO_ACK);
		NetAddress host = new NetAddress(ip, port);
		spawnMessage(echoMsg, host);
		
		System.out.println("### Server: Responding to Echo From "+host);
	}
	
	public void respondToWRegistration(String ip, int sensorPort,  String widgetName,  String contextDescription)
	{
		//construct response message
		OscMessage widgetAck = new OscMessage(MessageTypes.WIDGET_REGISTRATION_ACK);
		widgetAck.add(DefaultPorts.DEFAULT_DISCOVER_PORT);
		
		//create new registration and add to list
		NetAddress newHost = new NetAddress(ip, sensorPort);
		WidgetRegistration newReg = new WidgetRegistration(widgetName,contextDescription,newHost);
		knownWidgets.add(newReg);
	
		//create entry in subscribers list
		widgetSubscribers.put(widgetName, new LinkedList<NetAddress>());
		
		//print status
		System.out.println("### Received Widget Registation Request...");
		System.out.println(widgetName+" "+newHost+" "+contextDescription);
		
		//spawn response message
		spawnMessage(widgetAck,newHost);
	}
	
	public void respondToClientRegistration(String ip, int port, String hostName)
	{	
		System.out.println("### Received Client Registration...");
		System.out.println(hostName + " " + ip + " " + port);
		
		//create new host from information sent
		NetAddress newHost = new NetAddress(ip, port);
		
		//add to hosts list
		allHosts.add(newHost);
		
		//create response message, tell the client what port future conversation will
		//occur on
		OscMessage responseMessage = new OscMessage(MessageTypes.DISCOVER_DHCP_ACK);
		responseMessage.add(myNetAddress.address());
		responseMessage.add(myNetAddress.port());
		
		//spawn new server and thread to send message, why this is needed, I have no idea...
		spawnMessage(responseMessage, newHost);
		
	}

	@Override
	public void oscEvent(OscMessage theMessage) {
		if(theMessage.isPlugged())
		{
			return;
		}
		
		String addrPattern = theMessage.addrPattern();
		String typeTag = theMessage.typetag();
		
		/* print the address pattern and the typetag of the received OscMessage */
		System.out.print("### Server: Unhandled OSC Message");
		System.out.print(" addrpattern: " + addrPattern);
		System.out.println(" typetag: " + typeTag);
		
		
		int numElements = theMessage.typetag().length();
		for(int i=0;i<numElements;i++)
		{
			switch(theMessage.typetag().charAt(i))
			{
			case 's':
				String s=theMessage.get(i).stringValue();
				System.out.println(s);
				break;
				
			case 'f':
				System.out.println(theMessage.get(i).floatValue());
				break;
			
			case 'i':
				System.out.println(theMessage.get(i).intValue());
				break;
				
			default:
				System.out.println("Unknown message type...");
				break;
			}
		}
	}
	
	private void spawnMessage(OscMessage message, NetAddress recepient)
	{
		new Sender(message,recepient).start();
	}

	@Override
	public void oscStatus(OscStatus theStatus) 
	{
		System.out.println("OSC Status: "+theStatus.id());

	}
	
	//can extend Thread class if necessary
	//private class Sender extends Thread
	private class Sender extends Thread{

		OscMessage message;
		NetAddress netAddress;
		
		public Sender(OscMessage message, NetAddress netAddress)
		{
			this.message=message;
			this.netAddress=netAddress;
		}
		
		public void start() 
		{
			//new OscP5(this,-1).send(message,netAddress);
			OscP5.flush(message, netAddress);
		}

	}
	
	@SuppressWarnings("unused")
	private class UpdateWidgetSubscribers extends Thread
	{
		List<NetAddress> subscribers = null;
		byte[] data = null;
		String widgetName;
		
		public UpdateWidgetSubscribers(String widgetName, NetAddress widgetAddress,byte[] data)
		{
			subscribers = widgetSubscribers.get(widgetName);
			this.data=data;
			this.widgetName = widgetName;
		}
		
		@Override
		public void start()
		{
			OscMessage updateWidgetData = new OscMessage(MessageTypes.WIDGET_UPDATE);
			updateWidgetData.add(widgetName);
			updateWidgetData.add(data);
			
			if(subscribers==null)
			{
				return;
			}
			
			for(NetAddress host : subscribers)
			{
				OscP5.flush(updateWidgetData, host);
			}
		}
	}

	@SuppressWarnings("unused")
	private class WidgetRegistration
	{
		public WidgetRegistration(String sensorName, String contextDescription, NetAddress newHost) 
		{
			this.widgetName=sensorName;
			this.description=contextDescription;
			
			netAddress = newHost;
		}
		
		NetAddress netAddress;
		String widgetName;
		String description;
		
		boolean equals(WidgetRegistration otherObject)
		{
			if(netAddress.equals(otherObject.netAddress) && widgetName.equals(otherObject.widgetName))
			{
				return true;
			}
			else
			{
				return false;
			}
				 
		}
	}
	
	@SuppressWarnings("unused")
	private class ServiceRegistration
	{
		NetAddress netAddress;
		String serviceName;
		String serviceDescription;
		
		public ServiceRegistration(NetAddress host, String name, String description)
		{
			netAddress=host;
			serviceName=name;
			serviceDescription=description;
		}
		
		boolean equals(ServiceRegistration otherObject)
		{
			if(netAddress.equals(otherObject.netAddress) && serviceName.equals(otherObject.serviceName))
			{
				return true;
			}
			else
			{
				return false;
			}
				 
		}
	}
}
