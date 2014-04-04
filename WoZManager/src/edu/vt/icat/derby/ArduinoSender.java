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
	
	/*private int[] generateNodeIdentifier(String arduinoTarget)
	{
		int[] NI = new int[arduinoTarget.length()-1];
		
		int j=0;
		for(int i=0;i<NI.length;i++)
		{
			char c = arduinoTarget.charAt(j);
			if(c==',')
			{
				j++;
			}
			NI[i]=arduinoTarget.charAt(j);
			j++;
		}
		
		return NI;
	}*/

	@Override
	public void run()
	{
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
			
			WozManager.sendTxRequest(txRequest);
		}
	}
}
