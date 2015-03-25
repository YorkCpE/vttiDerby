package edu.vt.icat.derby;

import java.util.HashMap;

import processing.core.PApplet;
import processing.serial.Serial;

public class SimpleScoreboard extends PApplet
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6991403723295048750L;

	private class CarStatus
	{
		public int currentLap=0;
		public int laneViolations=0;
		public int collisions=0;
	}

	private HashMap<DerbyCar,CarStatus> carMap;
	private HashMap<Byte[],DerbyCar> myAddressMap;
	private Serial mySerial;

	public void setup()
	{
		size(800,600);
		background(0);
	}

	private int counter=0;
	private byte[] messageBuffer={0x0,0x0,0x0,0x0};
	public void draw()
	{	
		while(mySerial.available()>0)
		{
			messageBuffer[counter%4]=(byte) mySerial.read();
			counter++;
			
			//sound be car MY value, message code, 0x0, CHECKSUM
			boolean validPacket=(messageBuffer[0]^messageBuffer[1]^messageBuffer[2]^messageBuffer[3])==0xff;
			
			if(validPacket)
			{
				System.out.println("Received packet "+messageBuffer[0]);
			}
		}
	}

	public SimpleScoreboard() 
	{
		String[] serials = Serial.list();
		System.out.println("Opening Port "+serials[0]);
		mySerial = new Serial(this,serials[0],57600);
	}

	public static void main(String[] args) 
	{
		PApplet.main(new String[] {SimpleScoreboard.class.getName() });
	}
}
