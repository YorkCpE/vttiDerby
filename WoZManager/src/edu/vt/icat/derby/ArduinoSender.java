package edu.vt.icat.derby;

import java.util.concurrent.LinkedBlockingQueue;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.wpan.TxRequest16;


/**
 * This class is responsible for sending WoZCommands over the XBee network. It does not listen for acknowledgments of the packet. The class listens to a concurrent
 * queue that is shared with the WoZManager for the commands that it should send
 * @author Jason Forsyth
 *
 */
public class ArduinoSender extends Thread 
{

	/**
	 * Shared Queue with the WoZManager on which to receive commands
	 */
	private LinkedBlockingQueue<WoZCommand> myQueue = null;

	public ArduinoSender(LinkedBlockingQueue<WoZCommand> xbeeQueue) 
	{
		myQueue=xbeeQueue;
	}
	
	@Override
	public void run()
	{
		/**
		 * Grab an instance of the Xbee object so we can send messages over it
		 */
		XbeeManager xbee = XbeeManager.getInstance();
		
		while(true)
		{
			WoZCommand wozCommand = null;
			try {
				//grab a command from the queue, will block until something is available
				wozCommand = myQueue.take();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			//lookup the MY address for that particular car. Each car has a unique MY address
			byte[] myAddr = DerbyCar.lookupMYAddress(wozCommand.getTarget());
			
			//generate the payload (if any) for this message
			int[] payload = wozCommand.generateXbeePayload();
			
			//convert the MY address into something usable by the Xbee API Library
			XBeeAddress16 addr = new XBeeAddress16(myAddr[0],myAddr[1]);
			
			//create a transmit request
			TxRequest16 txRequest = new TxRequest16(addr, payload);
			
			//send the request asynchronously, does not wait for a response
			xbee.sendAsyncRequest(txRequest);
		}
	}
}
