package edu.vt.ece.context;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;

public class FSRWidget implements WidgetInterface {

	NetworkClient client;
	String msg="";
	
	public FSRWidget() {
		client = new NetworkClient("Register FSR");
		client.plug(this, "respondToWidgetPoll", MessageTypes.WIDGET_POLL);
		
		client.registerWithDiscover();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		client.registerWidget("FSR", "Mesasure force");
		client.plug(this, "respondToWidgetPoll", MessageTypes.WIDGET_POLL);

		int i = 0;
		while (true) {

			msg = "hello " + i;
			client.updateDiscoverWidgetData("FSR", msg.getBytes());

			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}

			i++;
		}
	}

	public static void main(String[] args) {
		new FSRWidget();

	}

	@Override
	public void respondToWidgetPoll(String ip, int port, String widgetName) 
	{
		NetAddress host = new NetAddress(ip, port);

		OscMessage pollMessage = new OscMessage(MessageTypes.WIDGET_POLL_RESPONSE);
		
		byte[] array=msg.getBytes();
		
		pollMessage.add(array);
		
		OscP5.flush(pollMessage, host);
	}

}
