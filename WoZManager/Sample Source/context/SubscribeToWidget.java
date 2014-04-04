package edu.vt.ece.context;

public class SubscribeToWidget 
{

	public SubscribeToWidget()
	{
		NetworkClient client = new NetworkClient("subscriber");
		client.registerWithDiscover();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String widgetName = "Photocell";
		client.subscribeToWidget(widgetName);
		client.plug(this, "recvWidgetUpdates", MessageTypes.WIDGET_UPDATE);
	}
	
	public void recvWidgetUpdates(String widgetName, byte[] data)
	{
		String s = new String(data);
		System.out.println("### Client Received Widget Update from " +widgetName+" "+s);
	}
	
	public static void main(String[] args) 
	{
		new SubscribeToWidget();

	}

}
