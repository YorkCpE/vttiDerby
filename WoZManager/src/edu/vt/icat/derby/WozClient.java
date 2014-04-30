package edu.vt.icat.derby;

import java.util.Date;

import netP5.NetAddress;
import netP5.NetInfo;
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
/**
 * WoZClient is used by the "Wozers" to issue commands to each DerbyCar. This class utilizes Processing (www.processing.org) for graphics, 
 * and OscP5 (http://www.sojamo.de/libraries/oscP5/) to send Open Sound Control Messages.
 * @author Jason Forsyth
 *
 */
public class WozClient extends PApplet implements ControlListener,OscEventListener{

	private static final long serialVersionUID = 7449473169738178331L;

	//controller for GUI objects
	private ControlP5 cp5;

	//OSC host
	private OscP5 localHost = null;

	//list boxes for the color and shape of the car
	private ListBox carColorListBox;
	private ListBox carShapeListBox;

	//variables to hold the current color and shape
	private LicenseShape currentShape;
	private LicenseColor currentColor;

	//onscreen buttons
	private Button lapButton;
	private Button laneViolation;
	private Button collisionWarning;

	//state tracking booleans
	private boolean enableLocalXbee=false;
	private boolean connectedToWoZManager=false;
	private boolean connectedToArduino=false;

	//network address of the WoZ Manager
	private NetAddress wozManagerAddress;

	//last received Echo from the Manager (in milliseconds)
	private long lastManagerEcho=0;

	//last received Echo from our current Arduino (in milliseconds)
	private long lastArduinoEcho=0;

	//default port to listen for OSC messages
	public static final int DEFAULT_CLIENT_PORT=3847;

	//my IP address
	private String myCurrentIP="";
	
	//wozmanager hostname
	private String managerHostName=WozManager.DefaultHostName;
	
	//WoZManger listening port
	private int managerListeningPort=WozManager.MANAGER_DEFAULT_LISTENING_PORT;

	//my current port to listen on
	private int myCurrentPort;

	private Button checkArduinoButton;
	private boolean checkArduinoConnection=false;

	//maximum time allowed between Arduino echos, will be considered
	//disconnected if violated
	private static final int ARDUINO_TIMEOUT=10000;
	
	//Start/Stop timestamp to send laptime
	private Date startStopDate = null;

	private Button managerDHCP;
	
	private boolean registredWithManager=false;

	public WozClient() 
	{
		//establish connection with WoZManager
		localHost = new OscP5(this, DEFAULT_CLIENT_PORT);
		myCurrentIP=NetInfo.lan();
		myCurrentPort=DEFAULT_CLIENT_PORT;

		//plug the echo and echo ack commands so we don't have to directly process these messages
		localHost.plug(this, "receiveEcho", WozControlMessage.ECHO);
		localHost.plug(this, "receiveEchoAck", WozControlMessage.ECHO_ACK);
		localHost.plug(this, "receiveHeartBeatAck", WozControlMessage.HEARTBEAT_ACK);
		localHost.plug(this, "receiveRegistrationAck", WozControlMessage.REGISTRATION_ACK);

		//sleep so everything has time to setup
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//find address for WoZManager
		wozManagerAddress = new NetAddress(managerHostName,managerListeningPort);

		//send an echo to the WoZManager
		//sendEcho(wozManagerAddress);
		attemptManagerRegistration();

		//set our default shape and color
		currentShape = LicenseShape.Circle;
		currentColor = LicenseColor.Black;
	}

	/**
	 * Method called when a heartbeat from an Arduino is forwarded from the WoZManager
	 * @param sourceIP Source IP of the sending OSC client
	 * @param port Source Port of the sending OSC client
	 * @param args Message arguments. {LicenseColor, LicenseShape, CheckInTime}
	 */
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

		//System.out.println("Last heart from "+color+","+shape+" "+(double)((System.currentTimeMillis()-lastCheckin)/(double)1000)+" seconds ago");
	}
	/**
	 * Called when an echo message is received. Should respond with EchoAsk to hostname and port.
	 * @param sourceIP Hostname of device sending Echo
	 * @param sourcePort Port number of device sending Echo
	 */
	public void receiveEcho(String sourceIP, int port, String args)
	{

	}

	/**
	 * Received an EchoAck from hostname at port. Do not need to respond. Assumes echo requests are from the WoZManager
	 * @param hostname
	 * @param port
	 */
	public void receiveEchoAck(String sourceIP, int port, String args)
	{


		connectedToWoZManager=true;

		lastManagerEcho=System.currentTimeMillis();
	}


	/**
	 * Processing setup() loop.
	 */
	public void setup()
	{
		//initialize the screen size
		size(400,600);
		noStroke();

		//initialize the GUI manager
		cp5 = new ControlP5(this);

		//setup the Lane Violation button
		laneViolation = cp5.addButton("Lane Violation")
				.setPosition(10,200)
				.addListener(new ControlListener() {

					//function gets called when the button is pressed
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						sendLaneViolation();
					}
				});

		//setup the lap button
		lapButton = cp5.addButton("Lap")
				.setPosition(laneViolation.getAbsolutePosition().x+laneViolation.getWidth()+10,200)
				.addListener(new ControlListener() {

					//function gets called when the button is pressed
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						sendLapStartStop();
					}
				});

		//setup the collision warning button
		collisionWarning = cp5.addButton("Collision Warning")
				.setPosition(lapButton.getAbsolutePosition().x+lapButton.getWidth()+10,200)
				.addListener(new ControlListener() {

					//function gets called when the button is pressed
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						sendCollisionWarning();
					}
				});

		//setup the check arduino button
		checkArduinoButton = cp5.addButton("Check Arduino Connection")
				.setPosition(collisionWarning.getAbsolutePosition().x+lapButton.getWidth()+10, 300)
				.addListener(new ControlListener() {

					//function gets called when the button is pressed
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						checkArduinoConnection=true;
						lastArduinoEcho=-1;
					}
				});


		//create the listbox that holds all the license plate colors
		carColorListBox = cp5.addListBox("Car Color")
				.setPosition(10, 50)
				.setSize(120, 120)
				.setItemHeight(15)
				.setBarHeight(15)
				.setColorBackground(color(255, 128))
				.setColorActive(color(0))
				.setColorForeground(color(255, 100,0))
				;

		//create the listbox that holds all the license plate shapes
		carShapeListBox = cp5.addListBox("Car Shape")
				.setPosition(200, 50)
				.setSize(120, 120)
				.setItemHeight(15)
				.setBarHeight(15)
				.setColorBackground(color(255, 128))
				.setColorActive(color(0))
				.setColorForeground(color(255, 100,0))
				;
		

		managerDHCP = cp5.addButton("DHCP")
				.setPosition(checkArduinoButton.getAbsolutePosition().x,350)
				.addListener(new ControlListener() {
					
					@Override
					public void controlEvent(ControlEvent arg0) 
					{
						attemptManagerRegistration();
					}
				});

		//add all the colors to the listbox
		for(LicenseColor color : LicenseColor.values())
		{
			ListBoxItem lbi = carColorListBox.addItem(color.toString(), color.ordinal());
			lbi.setColorBackground(0xffff0000);
		}

		//add all the shapes to the listbox
		for(LicenseShape shape : LicenseShape.values())
		{
			ListBoxItem lbi = carShapeListBox.addItem(shape.toString(), shape.ordinal());
			lbi.setColorBackground(0xffff0000);
		}
		
		cp5.addTextfield("Host IP")
	     .setPosition(10,525)
	     .setSize(100,40)
	     .setFocus(true)
	     .setFont(createFont("arial",20))
	     .setColor(color(255,0,0))
	     .addListener(new ControlListener() {
			
			@Override
			public void controlEvent(ControlEvent arg0) 
			{
				String newHostIP=arg0.getStringValue();
				System.out.println("Host IP Set to "+newHostIP);
				
				wozManagerAddress = new NetAddress(newHostIP, managerListeningPort);
			}
		})
	     ;

	}
	
	private long lastAttempTime=System.currentTimeMillis();
	private void attemptManagerRegistration()
	{
		if(Math.abs(lastAttempTime-System.currentTimeMillis())<5000)
		{
			return;
		}
		
		lastAttempTime=System.currentTimeMillis();
		
		connectedToWoZManager=false;
		
		WozControlMessage registration = new WozControlMessage(WozControlMessage.REGISTRATION, myCurrentIP, myCurrentPort, "");
		
		OscP5.flush(registration.generateOscMessage(), new NetAddress("255.255.255.255", WozManager.MANAGER_DEFAULT_LISTENING_PORT));
	}
	
	public void receiveRegistrationAck(String managerIP, int managerPort, String args)
	{
		NetAddress manager = new NetAddress(managerIP, managerPort);
		
		if(!manager.isvalid())
		{
			return;
		}
		
		wozManagerAddress=manager;
		connectedToWoZManager=true;
		lastManagerEcho=System.currentTimeMillis();
		registredWithManager=true;
		
		System.out.println("Client Registered with Manager");
	}

	/**
	 * Send an Echo over OSC to some destination
	 * @param destination
	 */
	public void sendEcho(NetAddress destination) 
	{
		WozControlMessage echo = new WozControlMessage(WozControlMessage.ECHO, myCurrentIP, myCurrentPort, "");
		localHost.send(echo.generateOscMessage(),destination);
	}


	/**
	 * Send a collision warning to an Arduino via the WoZManager
	 */
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

	/**
	 * Send a Lap Start/Stop message to an Arduino via the WoZManager
	 */
	private void sendLapStartStop() 
	{
		//Get lap time, send -1 if first lap
		long laptime = -1;
		Date now = new Date();
		if(startStopDate != null){
			
			
			laptime = Math.abs(startStopDate.getTime() - now.getTime()) / 1000;
			startStopDate=now;
			
		}
		else
		{
			startStopDate=new Date();
		}
		
		//send OSC message
		WoZCommand lapStartStop = new WoZCommand(currentColor,currentShape,WoZCommand.LAP_STARTSTOP,String.valueOf(laptime));
		localHost.send(lapStartStop.generateOscMessage(), wozManagerAddress);
		
		if(enableLocalXbee)
		{
			//send xbee message
		}		

	}

	/**
	 * Send a lane violation to an Arduino via the WoZManager
	 */
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

	/**
	 * The draw() loop is repeatedly called by the Processing library
	 */
	public void draw() 
	{

		//set the background to black
		background(0);

		//make the default text size 16
		textSize(16);

		//determine if we've heard from the Manager lately
		long lastManagerDelta=Math.abs(System.currentTimeMillis()-lastManagerEcho);
		if(lastManagerDelta>10000)
		{
			connectedToWoZManager=false;
			if(registredWithManager==false)
			{
				attemptManagerRegistration();
			}
			else
			{
				sendEcho(wozManagerAddress);
			}
		}

		//determine if we've heard from the Arduino lately
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

		//print the current license plate shape and color
		text(currentColor+" "+currentShape,10,300);

		//print whether the WoZClient is connected to the WozManager
		text((connectedToWoZManager)?"Connected To Manager":"No Manager Connection",10,400);
		
		//print out LAN IP
		text("My IP: "+myCurrentIP,10,450);

		//print whether the WoZClient is connected to the current Arduino
		text((connectedToArduino)?"Connected To Arduino":"No Arduino Connection",10,500);
		
		
		if(keyPressed && !locked)
		{
			locked=true;
			if(key=='j')
			{
				sendLaneViolation();
			}
			else if(key=='k')
			{
				sendLapStartStop();
			}
			else if(key=='l')
			{
				sendCollisionWarning();
			}
		}

	}
	
	private boolean locked=false;
	public void keyReleased()
	{
		locked=false;
	}

	/**
	 * Minimum time allowed between Arduino Echo requests. Don't want to overload the network
	 */
	private static final long ALLOWABLE_REQUESTS_INTERVAL=1000;
	
	/**
	 * Time of last heartbeat request being sent
	 */
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

		//send an Echo via the WoZManager
		WozControlMessage echo = new WozControlMessage(WozControlMessage.HEARTBEAT, myCurrentIP, myCurrentPort, currentColor+","+currentShape);
		localHost.send(echo.generateOscMessage(),addr);

		lastHeartBeatRequest=System.currentTimeMillis();
	}


	/**
	 * Respond to a ControlEvent generated by the GUI. Method is required by the 
	 * ControlListener interface
	 */
	@Override
	public void controlEvent(ControlEvent theEvent) 
	{

		if(theEvent.isGroup())
		{
			//something happened in the Car Color Listbox
			if(theEvent.getName().equals("Car Color"))
			{
				int index =(int) theEvent.getValue();

				LicenseColor color = LicenseColor.values()[index];
				setColor(color);
				connectedToArduino=false;
			}
			
			//something happened in the Car Shape Listbox
			else if(theEvent.getName().equals("Car Shape"))
			{
				int index = (int) theEvent.getValue();

				LicenseShape shape = LicenseShape.values()[index];
				setShape(shape);
				connectedToArduino=false;
			}
		}
	}

	/**
	 * Setter for current License Shape
	 * @param shape New Shape to assign
	 */
	private void setShape(LicenseShape shape) 
	{
		currentShape=shape;
		startStopDate=null;
	}

	/**
	 * Setter for License Color
	 * @param color New Color to assign
	 */
	private void setColor(LicenseColor color) 
	{
		currentColor=color;
		startStopDate=null;
	}

	/**
	 * Main function that starts the Processing sketch
	 * @param args
	 */
	public static void main(String[] args) 
	{
		PApplet.main(new String[] {WozClient.class.getName() });
	}

	/**
	 * Called if an unhandled OSC message is received
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

	/**
	 * Called if an unhandled OSC status is received
	 */
	@Override
	public void oscStatus(OscStatus theStatus) {

	}
}
