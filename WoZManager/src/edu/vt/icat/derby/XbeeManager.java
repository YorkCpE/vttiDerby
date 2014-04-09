package edu.vt.icat.derby;

import processing.serial.Serial;

import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.wpan.TxRequest16;

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
	Checksum */

public class XbeeManager {

	private static XbeeManager instance=null;

	private static XBee xbee;

	private final int[] nodeIdentifier={'M','A','N','A','G','E','R'};

	private XbeeManager() 
	{
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
			xbee.close();
		}




	}
	/**
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

	public synchronized static XbeeManager getInstance()
	{
		if(instance==null)
		{
			instance = new XbeeManager();
		}

		return instance;
	}
	
	public synchronized void sendAsyncRequest(TxRequest16 txRequest) 
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
	
	public synchronized XBeeResponse sendSynchronousRequest(TxRequest16 txRequest16, int timeout)
	{
		if(!xbee.isConnected())
		{
			return null;
		}
		
		try {
			return xbee.sendSynchronous(txRequest16,timeout);
		} catch (XBeeTimeoutException e) {
			e.printStackTrace();
		} catch (XBeeException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @param xbee
	 * @param commandResponse
	 * @return
	 */
	public synchronized int[] sendATCommand(String command, int[] args, int timeout) 
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
}
