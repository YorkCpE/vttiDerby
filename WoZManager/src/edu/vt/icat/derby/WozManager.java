package edu.vt.icat.derby;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.wpan.TxRequest16;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;
import processing.core.PApplet;
import processing.serial.Serial;
import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * @author Jason Forsyth
 *
 */
public class WozManager extends PApplet implements OscEventListener, PacketListener, SerialPortEventListener
{
	private static final long serialVersionUID = 1149760259465655755L;
	public static final String DefaultHostName = "netlenovo";

	private OscP5 server=null;

	private List<DerbyCar> allDerbyCars;

	public static final int DEFAULT_LISTENING_PORT=3944;

	private static XBee xbee;

	private final int[] nodeIdentifier={'M','A','N','A','G','E','R'};
	
	private LinkedBlockingQueue<WoZCommand> xbeeQueue;

	public WozManager()
	{
		server = new OscP5(this, DEFAULT_LISTENING_PORT);
		server.plug(this,"receiveEcho",WozControlMessage.ECHO);
		server.plug(this,"receiveEchoAck",WozControlMessage.ECHO_ACK);

		server.plug(this, "collisionWarning", WoZCommand.COLLISION_WARNING);
		server.plug(this, "laneViolation", WoZCommand.LANE_VIOLATION);
		server.plug(this, "lapStartStop", WoZCommand.LAP_STARTSTOP);

		xbeeQueue = new LinkedBlockingQueue<WoZCommand>();

		//try to establish connection with the Xbee
		String[] serialPorts=Serial.list();
		xbee = new XBee();

		boolean xbeeConnected=false;
		for(String port : serialPorts)
		{
			try 
			{
				xbee.open(port, 9600);
				xbeeConnected=checkXbeeConnection(xbee);
			} catch (XBeeException e) 
			{
				//e.printStackTrace();
			}

			if(xbeeConnected)
			{
				System.out.println("Manager: acquiring serial port "+port);
				break;
			}
			else
			{
				//xbee.close();
			}
		}

		if(!xbeeConnected)
		{
			System.out.println("Manager couldn't establish connection with the Xbee!!");
		}

		new ArduinoSender(xbeeQueue).start();

		allDerbyCars = new LinkedList<DerbyCar>();

		//generate all derby cars
		for(LicenseColor c : LicenseColor.values())
		{
			for(LicenseShape s : LicenseShape.values())
			{
				DerbyCar newCar = new DerbyCar(c,s);
				allDerbyCars.add(newCar);
			}
		}
	}

	/**
	 * 7E 00 0A 01 01 50 01 00 48 65 6C 6C 6F B8

		7E
		Start delimiter
		00 0A
		Length bytes
		01
		API identifier
		01
		API frame ID
		50 01
		Destination address low
		00
		Option byte
		48 65 6C 6C 6F
		Data packet
		B8
		Checksum

	 * @param xbee Xbee object to test and see if there's actually an Xbee on the serial point
	 * @return Return true if an Xbee is connected, false otherwise.
	 */
	private synchronized boolean checkXbeeConnection(XBee xbee) 
	{
		int[] commandResponse=null;
		
		commandResponse = sendATCommand("NI", null, 3000);
		
		for(int i=0;i<commandResponse.length;i++)
		{
			if(commandResponse[i]!=nodeIdentifier[i])
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * @param xbee
	 * @param commandResponse
	 * @return
	 */
	public static synchronized int[] sendATCommand(String command, int[] args, int timeout) 
	{
		int[] commandResponse=null;
		try {
			
			AtCommand atCommand = null;
			
			if(args==null)
			{
				atCommand=new AtCommand(command);
			}
			else
			{
				atCommand=new AtCommand(command, args);
			}
				
			
			XBeeResponse response = xbee.sendSynchronous(atCommand,timeout);

			if (response.getApiId() == ApiId.AT_RESPONSE) 
			{
				// since this API ID is AT_RESPONSE, we know to cast to AtCommandResponse
				AtCommandResponse atResponse = (AtCommandResponse) response;
				if (atResponse.isOk()) 
				{
					// command was successful
					commandResponse=atResponse.getValue();

					//System.out.println("Command returned " + ByteUtils.toBase16(atResponse.getValue()));
				} 
				else 
				{
					// command failed!
				}
			}
		} catch (XBeeTimeoutException e) 
		{
			e.printStackTrace();
		} catch (XBeeException e) 
		{
			e.printStackTrace();
		}
		return commandResponse;
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
		WozControlMessage echoAck = new WozControlMessage(WozControlMessage.ECHO_ACK, server.ip(), DEFAULT_LISTENING_PORT, "");
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

	/**
	 * Called when receiving an Xbee response
	 */
	@Override
	public void processResponse(XBeeResponse arg0) 
	{


	}

	public synchronized static void sendTxRequest(TxRequest16 txRequest) 
	{
		if(!xbee.isConnected())
		{
			return;
		}
		
		try {
			xbee.sendAsynchronous(txRequest);
		} catch (XBeeException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
