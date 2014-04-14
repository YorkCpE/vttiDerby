package edu.vt.icat.derby;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;
import processing.core.PApplet;
import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;

/**
 * WoZManager manages communication between the WoZClients, the Arduinos via Xbee, and forwards information to the Scoreboard.
 * @author Jason Forsyth
 *
 */
public class WozManager extends PApplet implements OscEventListener
{
	private static final long serialVersionUID = 1149760259465655755L;
	
	/**
	 * hostname for the OSC server. Should be set to a real host name if deployed.
	 */
	public static final String DefaultHostName = "localhost";

	private OscP5 server=null;

	private List<DerbyCar> allDerbyCars;

	/**
	 * map between the ArduinoName of a car and the DerbyCar object
	 */
	private HashMap<String, DerbyCar> arduinoNameMap;

	/**
	 * map between the XbeeName of a car and the DerbyCar Object
	 */
	private HashMap<String, DerbyCar> xbeeNameMap;

	/**
	 * Default listening port for the OSC Server
	 */
	public static final int MANAGER_DEFAULT_LISTENING_PORT=3944;

	/**
	 * Concurrent queue to send commands over the Xbee network. Queue is read by the XbeeManager
	 */
	private LinkedBlockingQueue<WoZCommand> xbeeQueue;

	/**
	 * Concurrent queue to send Heartbeat commands
	 */
	private LinkedBlockingQueue<HeartBeatResponseMessage> heartBeatQueue;
	
	/**
	 * Concurrent hashmap used by HeartBeat Monitor to exchange checkin information.
	 */
	private ConcurrentHashMap<DerbyCar, Long> carCheckin;

	public WozManager()
	{
		//create the OSC server and plug all the relevant messages
		//a plugged message will automatically be called when it is received
		//the function name must match exactly
		server = new OscP5(this, MANAGER_DEFAULT_LISTENING_PORT);
		server.plug(this,"receiveEcho",WozControlMessage.ECHO);
		server.plug(this,"receiveEchoAck",WozControlMessage.ECHO_ACK);
		server.plug(this, "heartBeat", WozControlMessage.HEARTBEAT);

		server.plug(this, "collisionWarning", WoZCommand.COLLISION_WARNING);
		server.plug(this, "laneViolation", WoZCommand.LANE_VIOLATION);
		server.plug(this, "lapStartStop", WoZCommand.LAP_STARTSTOP);

		xbeeQueue = new LinkedBlockingQueue<WoZCommand>();
		heartBeatQueue = new LinkedBlockingQueue<HeartBeatResponseMessage>();

		allDerbyCars = new LinkedList<DerbyCar>();

		carCheckin = new ConcurrentHashMap<DerbyCar, Long>();

		arduinoNameMap = new HashMap<String, DerbyCar>();

		xbeeNameMap = new HashMap<String, DerbyCar>();

		//generate all derby cars
		for(LicenseColor c : LicenseColor.values())
		{
			for(LicenseShape s : LicenseShape.values())
			{
				DerbyCar newCar = new DerbyCar(c,s);
				allDerbyCars.add(newCar);

				arduinoNameMap.put(newCar.getArduinoName(), newCar);
				xbeeNameMap.put(newCar.getXbeeName(), newCar);
			}
		}

		//start up our other threads
		
		//give the ArduinoSender our queue so they can pass things over the network
		new ArduinoSender(xbeeQueue).start();
		
		//give the HeartBeat monitor our hashmap so it can update times
		new HeartbeatMonitor(allDerbyCars,carCheckin).start();
		
		//give the Responder our queue so it can talk back to the WozClients
		new HeartBeatResponder(heartBeatQueue, carCheckin).start();
	}

	/**
	 * Called whenever an Heart Beat request is received.
	 * @param sourceIP Source ip of the client sending the heart beat request
	 * @param sourcePort Source port of the client
	 * @param args Heart beat arguments, should the Arduino name of the device
	 */
	public void heartBeat(String sourceIP, int sourcePort, String args)
	{	
		DerbyCar car = arduinoNameMap.get(args);

		heartBeatQueue.add(new HeartBeatResponseMessage(sourceIP, sourcePort, car));
		
		//Note: for some reason we can't access the Checkin hashmap directly. There is some conflict
		//in the OscP5 library that gets pissy with cross thread access.
	}


	/**
	 * Called whenever a collision warning received.
	 * @param arduinoTarget Name of the Arduino that should receive the collision warning
	 * @param args Arguments for the command
	 */
	public void collisionWarning(String arduinoTarget, String args)
	{	
		String[] splits=arduinoTarget.split(",");

		if(splits.length>2)
		{
			return;
		}

		//pass command off to the ArduinoSender
		xbeeQueue.add(new WoZCommand(LicenseColor.valueOf(splits[0]), LicenseShape.valueOf(splits[1]), WoZCommand.COLLISION_WARNING, args));
	}

	/**
	 * Called whenever a lane violation is received
	 * @param arduinoTarget Name of the Arduino that should receive the lane violation
	 * @param args Arguments for the command
	 */
	public void laneViolation(String arduinoTarget, String args)
	{
		String[] splits=arduinoTarget.split(",");

		if(splits.length>2)
		{
			return;
		}

		//pass off command to the ArduinoSender
		xbeeQueue.add(new WoZCommand(LicenseColor.valueOf(splits[0]), LicenseShape.valueOf(splits[1]), WoZCommand.LANE_VIOLATION, args));
	}

	/**
	 * Called whenever a lap start/stop is received
	 * @param arduinoTarget Name of the Arduino that is starting/finishing its lap
	 * @param args Arguments for the command
	 */
	public void lapStartStop(String arduinoTarget, String args)
	{
		//System.out.println(arduinoTarget+": Lap Start Stop");
		String[] splits=arduinoTarget.split(",");

		if(splits.length>2)
		{
			return;
		}

		//pass off command to the Arduino sender
		xbeeQueue.add(new WoZCommand(LicenseColor.valueOf(splits[0]), LicenseShape.valueOf(splits[1]), WoZCommand.LAP_STARTSTOP, args));
	}

	/**
	 * Called whenever an Echo is received.
	 * @param sourceIP Source ip of the client sending the echo
	 * @param sourcePort Source port of the client
	 * @param args Echo arguments, should be empty
	 */
	public void receiveEcho(String sourceIP, int sourcePort, String args)
	{
		//send response directly
		WozControlMessage echoAck = new WozControlMessage(WozControlMessage.ECHO_ACK, server.ip(), MANAGER_DEFAULT_LISTENING_PORT, "");
		NetAddress destinationAddress = new NetAddress(sourceIP, sourcePort);
		server.send(echoAck.generateOscMessage(), destinationAddress);
	}

	/**
	 * Called when an EchoAck is received.
	 * @param sourceIP Source ip of the client sending the Echo ACK.
	 * @param sourcePort Source port of the client.
	 * @param args Should be empty
	 */

	public void receiveEchoAck(String sourceIP, int sourcePort, String args)
	{
		
	}

	/**
	 * This class responds to heartbeat requests from the WoZClients. We can't respond directly in the function call because of 
	 * cross-thread issues in the OscP5 library.
	 * @author Jason Forsyth
	 *
	 */
	public class HeartBeatResponder extends Thread
	{
		/**
		 * Queue of WoZClients to respond to
		 */
		private LinkedBlockingQueue<HeartBeatResponseMessage> myQueue;
		
		/**
		 * My concurrent copy of the Arduino checkins
		 */
		private ConcurrentHashMap<DerbyCar, Long> checkins;

		public HeartBeatResponder(LinkedBlockingQueue<HeartBeatResponseMessage> queue, ConcurrentHashMap<DerbyCar, Long> timeStamps)
		{
			myQueue=queue;
			checkins=timeStamps;
		}

		@Override
		public void start()
		{
			while(true)
			{
				HeartBeatResponseMessage request=null;
				try {
					//grab a request, block until this happens
					request=myQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				//ignore a bad object
				if(request==null)
				{
					continue;
				}

				//get the car I'm concerned about
				DerbyCar car=request.getCar();

				//get the last checkin time
				Object value = checkins.get(car);
				
				//make sure it's not a null value (because it's coming out of a hashmap)
				long lastCheckin=(value==null)?-1:(long)value;
				
				//get the destination for this message
				String hostname=request.getDesintationIP();
				int port = request.getDesinationPort();

				//compose the message arguments
				String args=car.getColor()+","+car.getShape()+","+lastCheckin;

				//create an OSC Control Message for this
				OscMessage oscMessage = new OscMessage(WozControlMessage.HEARTBEAT_ACK);
				NetAddress destination = new NetAddress(hostname, port);

				oscMessage.add(WozManager.DefaultHostName);
				oscMessage.add(WozManager.MANAGER_DEFAULT_LISTENING_PORT);
				oscMessage.add(args);

				//send it asynchronously
				OscP5.flush(oscMessage, destination);

			}
		}
	}

	/**
	 * Wrapper class for the heart beat responses
	 * @author Jason Forsyth
	 *
	 */
	public class HeartBeatResponseMessage
	{
		private String ip;
		private int port;
		private DerbyCar car;

		public HeartBeatResponseMessage(String sourceIP, int sourcePort, DerbyCar _car)
		{
			ip=sourceIP;
			port=sourcePort;
			car=_car;
		}

		public String getDesintationIP()
		{
			return ip;
		}

		public int getDesinationPort()
		{
			return port;
		}
		public DerbyCar getCar()
		{

			return car;
		}
	}


	/**
	 * Called every time an OSC message with the addr wozCommand is received. Processes the command.
	 * @param payload Comma deliminated payload received from the network
	 */

	@Override
	public void oscEvent(OscMessage theMessage) 
	{
		if(theMessage.isPlugged())
		{
			return;
		}
		else
		{
			OSCUtils.printUnknownMessage(theMessage);
		}

	}

	@Override
	public void oscStatus(OscStatus theStatus) 
	{


	}

	/**
	 * Processing setup() loop
	 */
	public void setup()
	{
		//size(400,400);
		noLoop();
	}

	/**
	 * Processing draw() loop
	 */
	public void draw()
	{

	}

	/**
	 * Main function, starts up the processing sketch
	 * @param args
	 */
	public static void main(String[] args)
	{
		PApplet.main(new String[] {WozManager.class.getName() });
	}
}
