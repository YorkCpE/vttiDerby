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
 * @author Jason Forsyth
 *
 */
public class WozManager extends PApplet implements OscEventListener
{
	private static final long serialVersionUID = 1149760259465655755L;
	public static final String DefaultHostName = "localhost";

	private OscP5 server=null;

	private List<DerbyCar> allDerbyCars;

	private HashMap<String, DerbyCar> arduinoNameMap;

	private HashMap<String, DerbyCar> xbeeNameMap;

	public static final int MANAGER_DEFAULT_LISTENING_PORT=3944;

	private LinkedBlockingQueue<WoZCommand> xbeeQueue;

	private LinkedBlockingQueue<HeartBeatResponseMessage> heartBeatQueue;
	private ConcurrentHashMap<DerbyCar, Long> carCheckin;

	public WozManager()
	{
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

		new ArduinoSender(xbeeQueue).start();
		new HeartbeatMonitor(allDerbyCars,carCheckin).start();
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

		//heartBeatQueue.add(new HeartBeatResponseMessage(sourceIP, sourcePort, car));
		//long lastCheckIn=-1;

		//if(car!=null)
		//{
		//so some reason there is an error thrown when calling get()... guess I'll have to
		//spawn another thread...
		//lastCheckIn = carCheckin.get(car);
		//}

		/*//send response
		WozControlMessage heartBeatAck = new WozControlMessage(WozControlMessage.HEARTBEAT_ACK, server.ip(), MANAGER_DEFAULT_LISTENING_PORT, args+","+String.valueOf(lastCheckIn));
		NetAddress destinationAddress = new NetAddress(sourceIP, sourcePort);
		server.send(heartBeatAck.generateOscMessage(), destinationAddress);*/
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

		xbeeQueue.add(new WoZCommand(LicenseColor.valueOf(splits[0]), LicenseShape.valueOf(splits[1]), WoZCommand.COLLISION_WARNING, args));
	}

	/**
	 * Called whenever a lane violation is received
	 * @param arduinoTarget Name of the Arduino that should receive the lane violation
	 * @param args Arguments for the command
	 */
	public void laneViolation(String arduinoTarget, String args)
	{
		//System.out.println(arduinoTarget+": Lane Violation");
		String[] splits=arduinoTarget.split(",");

		if(splits.length>2)
		{
			return;
		}

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
		//System.out.println("WoZ Manager received echo from "+sourceIP+" on port "+sourcePort);

		//send response
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
		System.out.println("WoZ Manager received echoAck from "+sourceIP+" on port "+sourcePort);
	}

	public class HeartBeatResponder extends Thread
	{
		private LinkedBlockingQueue<HeartBeatResponseMessage> myQueue;
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
					request=myQueue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(request==null)
				{
					continue;
				}

				DerbyCar car=request.getCar();

				Object value = checkins.get(car);
				
				long lastCheckin=(value==null)?-1:(long)value;
				
				String hostname=request.getDesintationIP();
				int port = request.getDesinationPort();


				String args=car.getColor()+","+car.getShape()+","+lastCheckin;

				OscMessage oscMessage = new OscMessage(WozControlMessage.HEARTBEAT_ACK);
				NetAddress destination = new NetAddress(hostname, port);

				oscMessage.add(WozManager.DefaultHostName);
				oscMessage.add(WozManager.MANAGER_DEFAULT_LISTENING_PORT);
				oscMessage.add(args);

				OscP5.flush(oscMessage, destination);

			}
		}
	}

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

	public static void main(String[] args)
	{
		PApplet.main(new String[] {WozManager.class.getName() });
	}
}
