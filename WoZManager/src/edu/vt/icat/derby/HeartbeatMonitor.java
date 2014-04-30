package edu.vt.icat.derby;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;

/**
 * This class is responsible for sending a "heartbeat" to each Arduino at various intervals. The response time of these
 * heartbeats is used to determine if an Arduino is live. This class shares a HashMap with the WozManager to exchange this information
 * @author Jason Forsyth
 *
 */
public class HeartbeatMonitor extends Thread
{
	//Hashmap shared with the WoZManager
	private ConcurrentHashMap<DerbyCar, Long> currentState;
	
	//List of all known cars
	private List<DerbyCar> cars;

	//counter of successfully sent packets
	private HashMap<DerbyCar, Integer> successfulPackets;
	
	//counter of unsuccessfully sent packets
	private HashMap<DerbyCar, Integer> unsuccessfulPackets;
	
	public HeartbeatMonitor(List<DerbyCar> allCars, ConcurrentHashMap<DerbyCar, Long> carCheckin) 
	{
		cars=allCars;
		currentState=carCheckin;
		successfulPackets=new HashMap<DerbyCar, Integer>();
		unsuccessfulPackets=new HashMap<DerbyCar,Integer>();
		
		//add each car to the HashMaps
		for(DerbyCar c: allCars)
		{
			successfulPackets.put(c, 0);
			unsuccessfulPackets.put(c, 0);
		}
	}

	@Override
	public void run()
	{
		//grab an instance of the Xbee manager to send messages
		XbeeManager xbee = XbeeManager.getInstance();
		while(true)
		{
			//send a heartbeat to each car
			for(DerbyCar car : cars)
			{

				//create a WozCommand to the send to the Arduinos
				WoZCommand isOnline = new WoZCommand(car.getColor(), car.getShape(), WoZCommand.IS_ONLINE, null);
				
				//find the MyAddress of the Arduino
				byte[] myAddr = DerbyCar.lookupMYAddress(isOnline.getTarget());
				
				//generate a payload
				int[] payload = isOnline.generateXbeePayload();

				//create an address usable by the Xbee API
				XBeeAddress16 addr = new XBeeAddress16(myAddr[0],myAddr[1]);
				
				//create a transmission request with the address and the payload
				//TxRequest16 txRequest = new TxRequest16(addr, payload);
				TxRequest16 txRequest = new TxRequest16(addr, new Random().nextInt(32), payload);

				//send a synchronous request and wait for the response. Will time out after 500 ms
				TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronousRequest(txRequest,500);

				boolean success=false;
				
				if(response==null)
				{
					//some error occurred
				}
				else
				{
					//packet was successfully delivered
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
				
				//update our counters
				if(success)
				{
					int value = successfulPackets.get(car);
					successfulPackets.put(car, value+1);
				}
				else
				{
					int value = unsuccessfulPackets.get(car);
					unsuccessfulPackets.put(car, value+1);
				}

				//sleep the thread for 100ms to not overload the Xbee network
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
