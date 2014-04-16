package edu.vt.icat.derby;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

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