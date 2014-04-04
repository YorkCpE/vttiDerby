package edu.vt.icat.derby;

import oscP5.OscMessage;

public class OSCUtils 
{
	public static void printUnknownMessage(OscMessage theMessage)
	{
		if(theMessage.isPlugged())
		{
			return;
		}
		
		String addrPattern = theMessage.addrPattern();
		String typeTag = theMessage.typetag();
		
		/* print the address pattern and the typetag of the received OscMessage */
		System.out.print("### Server: Unhandled OSC Message");
		System.out.print(" addrpattern: " + addrPattern);
		System.out.println(" typetag: " + typeTag);
		
		
		int numElements = theMessage.typetag().length();
		for(int i=0;i<numElements;i++)
		{
			switch(theMessage.typetag().charAt(i))
			{
			case 's':
				String s=theMessage.get(i).stringValue();
				System.out.println(s);
				break;
				
			case 'f':
				System.out.println(theMessage.get(i).floatValue());
				break;
			
			case 'i':
				System.out.println(theMessage.get(i).intValue());
				break;
				
			default:
				System.out.println("Unknown message type...");
				break;
			}
		}
	}
}
