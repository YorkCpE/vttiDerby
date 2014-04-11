package edu.vt.icat.derby;

import netP5.NetAddress;
import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;
import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ListBox;
import controlP5.ListBoxItem;
import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;
import processing.core.PApplet;

public class WozClient extends PApplet implements ControlListener,OscEventListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7449473169738178331L;

	private ControlP5 cp5;

	private OscP5 localHost = null;

	private ListBox carColorListBox;
	private ListBox carShapeListBox;

	private LicenseShape currentShape;
	private LicenseColor currentColor;
	
	private Button lapButton;
	private Button laneViolation;
	private Button collisionWarning;
	
	private boolean enableLocalXbee=false;
	private boolean connectedToWoZManager=false;
	private boolean connectedToArduino=false;
	
	private NetAddress wozManagerAddress;
	private long lastManagerEcho=0;
	private long lastArduinoEcho=0;
	
	public static final int DEFAULT_CLIENT_PORT=3847;
	private String myCurrentIP="";
	private int myCurrentPort;

	@SuppressWarnings("unused")
	private Button checkArduinoButton;
	private boolean checkArduinoConnection=false;
	
	private static final int ARDUINO_TIMEOUT=10000;

	//private Button checkArduinoConnection;

	
	
	public WozClient() 
	{
		//establish connection with WoZManager
		localHost = new OscP5(this, DEFAULT_CLIENT_PORT);
		myCurrentIP=localHost.ip();
		myCurrentPort=DEFAULT_CLIENT_PORT;
	
		//plug the echo and echo ack commands so we don't have to directly process these messages
		localHost.plug(this, "receiveEcho", WozControlMessage.ECHO);
		localHost.plug(this, "receiveEchoAck", WozControlMessage.ECHO_ACK);
		localHost.plug(this, "receiveHeartBeatAck", WozControlMessage.HEARTBEAT_ACK);

		//sleep so everything has time to setup
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		//find address for WoZManager
		wozManagerAddress = new NetAddress(WozManager.DefaultHostName,WozManager.MANAGER_DEFAULT_LISTENING_PORT);
		
		//send an echo to the WoZManager
		sendEcho(wozManagerAddress);
		
		//set our default shape and color
		currentShape = LicenseShape.Circle;
		currentColor = LicenseColor.Black;
	}
	

	public void receiveHeartBeatAck(String sourceIP, int port, String args)
	{
		String[] splits = args.split(",");
		
		if(splits.length!=3)
		{
			return;
		}
		
		String color=splits[0];
		String shape=splits[1];
		long lastCheckin = Long.valueOf(splits[2]);
		
		if(lastCheckin<=0)
		{
			connectedToArduino=false;
			lastCheckin=-1;
			return;
		}
		else
		{
			lastArduinoEcho=lastCheckin;
		}

		System.out.println("Last heart from "+color+","+shape+" "+(double)((System.currentTimeMillis()-lastCheckin)/(double)1000)+" seconds ago");
	}
	/**
	 * Called when an echo message is received. Should respond with EchoAsk to hostname and port.
	 * @param sourceIP Hostname of device sending Echo
	 * @param sourcePort Port number of device sending Echo
	 */
	public void receiveEcho(String sourceIP, int port, String args)
	{
		//System.out.println("WoZ Client Received Echo from "+sourceIP+" on "+port);
	}
	
	/**
	 * Received an EchoAck from hostname at port. Do not need to respond. Assumes echo requests are from the WoZManager
	 * @param hostname
	 * @param port
	 */
	public void receiveEchoAck(String sourceIP, int port, String args)
	{
		//System.out.println("WoZ Client Received EchoAck from "+sourceIP+" on "+port);

		connectedToWoZManager=true;
		
		lastManagerEcho=System.currentTimeMillis();
	}


	/**
	 * Processing setup() loop.
	 */
	public void setup()
	{
		size(400,600);
		noStroke();
		cp5 = new ControlP5(this);
		
		laneViolation = cp5.addButton("Lane Violation")
				.setPosition(10,200)
				.addListener(new ControlListener() {
					
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						//System.out.println("Lane Violation!");
						sendLaneViolation();
					}
				});
		
		lapButton = cp5.addButton("Lap")
				.setPosition(laneViolation.getAbsolutePosition().x+laneViolation.getWidth()+10,200)
				.addListener(new ControlListener() {
					
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						//System.out.println("Lap Start/Stop");
						sendLapStartStop();
					}
				});
		
		collisionWarning = cp5.addButton("Collision Warning")
				.setPosition(lapButton.getAbsolutePosition().x+lapButton.getWidth()+10,200)
				.addListener(new ControlListener() {
					
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						//System.out.println("Collision Warning!");
						sendCollisionWarning();
					}
				});
		
		checkArduinoButton = cp5.addButton("Check Arduino Connection")
				.setPosition(collisionWarning.getAbsolutePosition().x+lapButton.getWidth()+10, 300)
				.addListener(new ControlListener() {

					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						checkArduinoConnection=true;
					}
				});
				

		carColorListBox = cp5.addListBox("Car Color")
				.setPosition(10, 50)
				.setSize(120, 120)
				.setItemHeight(15)
				.setBarHeight(15)
				.setColorBackground(color(255, 128))
				.setColorActive(color(0))
				.setColorForeground(color(255, 100,0))
				;
		
		carShapeListBox = cp5.addListBox("Car Shape")
				.setPosition(200, 50)
				.setSize(120, 120)
				.setItemHeight(15)
				.setBarHeight(15)
				.setColorBackground(color(255, 128))
				.setColorActive(color(0))
				.setColorForeground(color(255, 100,0))
				;


		for(LicenseColor color : LicenseColor.values())
		{
			ListBoxItem lbi = carColorListBox.addItem(color.toString(), color.ordinal());
			lbi.setColorBackground(0xffff0000);
		}
		
		for(LicenseShape color : LicenseShape.values())
		{
			ListBoxItem lbi = carShapeListBox.addItem(color.toString(), color.ordinal());
			lbi.setColorBackground(0xffff0000);
		}

	}


	public void sendEcho(NetAddress destination) 
	{
		WozControlMessage echo = new WozControlMessage(WozControlMessage.ECHO, myCurrentIP, myCurrentPort, "");
		localHost.send(echo.generateOscMessage(),destination);
	}


	private void sendCollisionWarning() 
	{
		//create WoZCommand for Collision Warning
		WoZCommand collisionWarning = new WoZCommand(currentColor,currentShape,WoZCommand.COLLISION_WARNING,"");
		localHost.send(collisionWarning.generateOscMessage(), wozManagerAddress);
		
		if(enableLocalXbee)
		{
			//send xbee message
		}
	}

	private void sendLapStartStop() 
	{
		//send OSC message
		WoZCommand lapStartStop = new WoZCommand(currentColor,currentShape,WoZCommand.LAP_STARTSTOP,"");
		localHost.send(lapStartStop.generateOscMessage(), wozManagerAddress);
		
		if(enableLocalXbee)
		{
			//send xbee message
		}		
		
	}

	private void sendLaneViolation() 
	{
		//send OSC message
		WoZCommand laneViolation = new WoZCommand(currentColor,currentShape,WoZCommand.LANE_VIOLATION,"");
		localHost.send(laneViolation.generateOscMessage(), wozManagerAddress);
		
		if(enableLocalXbee)
		{
			//send xbee message
		}
		
	}

	public void draw() 
	{
		background(0);
		
		textSize(16);
		text(currentColor+" "+currentShape,10,300);
		
		text((connectedToWoZManager)?"Connected To Manager":"No Manager Connection",10,400);
		
		text((connectedToArduino)?"Connected To Arduino":"No Arduino Connection",10,500);
		
		if(Math.abs(System.currentTimeMillis()-lastManagerEcho)>10000)
		{
			connectedToWoZManager=false;
			sendEcho(wozManagerAddress);
		}
		
		if(checkArduinoConnection==true || (Math.abs(System.currentTimeMillis()-lastArduinoEcho)>ARDUINO_TIMEOUT))
		{
			checkArduinoConnection=false;
			connectedToArduino=false;
			sendArduinoHeartbeat(wozManagerAddress);
		}
		
		else
		{
			connectedToArduino=true;
		}
	}

	private static final long ALLOWABLE_REQUESTS_INTERVAL=1000;
	private static long lastHeartBeatRequest=0;
	
	/**
	 * Sends a request to the manager to see when the last Arduino heartbeat was. Will only
	 * occur once every ALLOWABLE_REQUESTS_INTERVAL
	 * @param addr Destination of the Manager to send the heartbeat request
	 */
	private void sendArduinoHeartbeat(NetAddress addr) 
	{	
		long delta = (System.currentTimeMillis()-lastHeartBeatRequest);
		
		if(delta<ALLOWABLE_REQUESTS_INTERVAL)
		{
			return;
		}
		
		WozControlMessage echo = new WozControlMessage(WozControlMessage.HEARTBEAT, myCurrentIP, myCurrentPort, currentColor+","+currentShape);
		localHost.send(echo.generateOscMessage(),addr);
		
		lastHeartBeatRequest=System.currentTimeMillis();
	}


	public void controlEvent(ControlEvent theEvent) 
	{

		if(theEvent.isGroup())
		{
			if(theEvent.getName().equals("Car Color"))
			{
				int index =(int) theEvent.getValue();
				
				LicenseColor color = LicenseColor.values()[index];
				setColor(color);
				connectedToArduino=false;
			}
			else if(theEvent.getName().equals("Car Shape"))
			{
				int index = (int) theEvent.getValue();
				
				LicenseShape shape = LicenseShape.values()[index];
				setShape(shape);
				connectedToArduino=false;
			}
		}
	}

	private void setShape(LicenseShape shape) 
	{
		currentShape=shape;
		
	}

	private void setColor(LicenseColor color) 
	{
		currentColor=color;
		
	}

	public static void main(String[] args) 
	{
		PApplet.main(new String[] {WozClient.class.getName() });
	}

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
	public void oscStatus(OscStatus theStatus) {
		// TODO Auto-generated method stub
		
	}
}
