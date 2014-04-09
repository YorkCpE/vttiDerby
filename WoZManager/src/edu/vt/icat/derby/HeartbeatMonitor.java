package edu.vt.icat.derby;

import java.util.HashMap;
import java.util.List;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;

public class HeartbeatMonitor extends Thread
{

	private List<DerbyCar> allCars;
	private HashMap<String, Boolean> currentState;

	public HeartbeatMonitor(List<DerbyCar> cars) 
	{
		allCars = cars;
		currentState = new HashMap<String, Boolean>();
	}

	public synchronized Boolean isOnline(String arduinoTarget)
	{
		return currentState.get(arduinoTarget);
	}

	@Override
	public void run()
	{
		XbeeManager xbee = XbeeManager.getInstance();
		while(true)
		{
			for(DerbyCar car : allCars)
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
					currentState.put(car.getArduinoName(),false);
				}
				else
				{
					if (response.isSuccess()) 
					{
						currentState.put(car.getArduinoName(),true);
					} 
					else 
					{
						// packet was not delivered
						System.out.println("Heart Beat Packet was not delivered.  status: " + response.getStatus());
						currentState.put(car.getArduinoName(),false);
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
