package edu.vt.icat.derby;

import java.util.concurrent.LinkedBlockingQueue;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.wpan.TxRequest16;


public class ArduinoSender extends Thread 
{

	private LinkedBlockingQueue<WoZCommand> myQueue = null;

	public ArduinoSender(LinkedBlockingQueue<WoZCommand> xbeeQueue) 
	{
		myQueue=xbeeQueue;
	}
	
	@Override
	public void run()
	{
		XbeeManager xbee = XbeeManager.getInstance();
		
		while(true)
		{
			WoZCommand wozCommand = null;
			try {
				wozCommand = myQueue.take();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			byte[] myAddr = DerbyCar.lookupMYAddress(wozCommand.getTarget());
			int[] payload = wozCommand.generateXbeePayload();
			
			XBeeAddress16 addr = new XBeeAddress16(myAddr[0],myAddr[1]);
			TxRequest16 txRequest = new TxRequest16(addr, payload);
			
			xbee.sendAsyncRequest(txRequest);
		}
	}
}
