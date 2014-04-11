package edu.vt.icat.derby;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;

public class HeartbeatMonitor extends Thread
{
	private ConcurrentHashMap<DerbyCar, Long> currentState;
	private List<DerbyCar> cars;

	private HashMap<DerbyCar, Integer> successfulPackets;
	private HashMap<DerbyCar, Integer> unsuccessfulPackets;
	
	public HeartbeatMonitor(List<DerbyCar> allCars, ConcurrentHashMap<DerbyCar, Long> carCheckin) 
	{
		cars=allCars;
		currentState=carCheckin;
		successfulPackets=new HashMap<DerbyCar, Integer>();
		unsuccessfulPackets=new HashMap<DerbyCar,Integer>();
		
		for(DerbyCar c: allCars)
		{
			successfulPackets.put(c, 0);
			unsuccessfulPackets.put(c, 0);
		}
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

				boolean success=false;
				//some error occurred
				if(response==null)
				{

				}
				else
				{
					if (response.isSuccess()) 
					{
						currentState.put(car, System.currentTimeMillis());
						success=true;
					} 
					else 
					{
						// packet was not delivered
					}
				}
				
				if(success)
				{
					int value = successfulPackets.get(car);
					successfulPackets.put(car, value+1);
				}
				else
				{
					int value = unsuccessfulPackets.get(car);
					unsuccessfulPackets.put(car, value+1);
					
					/*if(car.getColor()==LicenseColor.Green && car.getShape()==LicenseShape.Square)
					{
						int good=successfulPackets.get(car);
						int bad=unsuccessfulPackets.get(car);
						
						double receiveRate = (double)(good)/(double)(good+bad);
						
						System.out.println("Green Square Receive Rate "+receiveRate);
					}*/
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
