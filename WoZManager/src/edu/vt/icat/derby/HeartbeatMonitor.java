package edu.vt.icat.derby;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;

public class HeartbeatMonitor extends Thread
{
	private ConcurrentHashMap<DerbyCar, Long> currentState;
	private List<DerbyCar> cars;

	public HeartbeatMonitor(List<DerbyCar> allCars, ConcurrentHashMap<DerbyCar, Long> carCheckin) 
	{
		cars=allCars;
		currentState=carCheckin;
	}

	@Override
	public void run()
	{
		XbeeManager xbee = XbeeManager.getInstance();
		while(true)
		{
			for(DerbyCar car : cars)
			{

				WoZCommand echo = new WoZCommand(car.getColor(), car.getShape(), WoZCommand.IS_ONLINE, null);
				byte[] myAddr = DerbyCar.lookupMYAddress(echo.getTarget());
				int[] payload = echo.generateXbeePayload();

				XBeeAddress16 addr = new XBeeAddress16(myAddr[0],myAddr[1]);
				TxRequest16 txRequest = new TxRequest16(addr, payload);

				TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronousRequest(txRequest,500);

				//some error occurred
				if(response==null)
				{

				}
				else
				{
					if (response.isSuccess()) 
					{
						currentState.put(car, System.currentTimeMillis());
					} 
					else 
					{
						// packet was not delivered
						System.out.println("Heart Beat Packet was not delivered.  status: " + response.getStatus());
					}
				}

				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
