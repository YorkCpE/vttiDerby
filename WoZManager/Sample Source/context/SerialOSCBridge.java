package edu.vt.ece.context;

import edu.vt.ece.context.util.CustomSerial;
import edu.vt.ece.context.util.SerialListener;

public class SerialOSCBridge implements SerialListener{

	private static final long serialVersionUID = -2642965952195806366L;
	
	CustomSerial mySerialPort = null;
	NetworkClient client = null;
	
	String widgetName="Photocell";
	String widgetDescription="Returns the raw light reading from a sensor";
	
	public SerialOSCBridge()
	{
		client = new NetworkClient("Arduino Bridge");
		client.registerWithDiscover();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		client.registerWidget(widgetName, widgetDescription);
		
		mySerialPort = new CustomSerial(this,"/dev/ttyUSB0");
		mySerialPort.bufferUntil('\n');	
	}
	
	public static void main(String[] args) 
	{
		new SerialOSCBridge();
	}

	@Override
	public void serialEvent(CustomSerial myPort) 
	{
		int bytesAvailable = myPort.available();
		byte[] byteArray = new byte[bytesAvailable];
		myPort.readBytes(byteArray);
	
		client.updateDiscoverWidgetData(widgetName, byteArray);
	}
}
