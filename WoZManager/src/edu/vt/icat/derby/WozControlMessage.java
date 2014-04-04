package edu.vt.icat.derby;

import oscP5.OscMessage;

public class WozControlMessage 
{
	public static final String ECHO="/echo";
	public static final String ECHO_ACK="/echoAck";
	
	public static final String HARDWAREUPDATE="/hardwareUpdate";
	
	private String messageType;
	private String sourceIP;
	private int sourcePort;
	private String args;
	
	public WozControlMessage(String _messageType, String _sourceIP, int _sourcePort, String _args)
	{
		messageType=_messageType;
		sourceIP=_sourceIP;
		sourcePort=_sourcePort;
		args=_args;
	}
	
	public OscMessage generateOscMessage()
	{
		OscMessage newMessage = new OscMessage(messageType);
		newMessage.add(sourceIP);
		newMessage.add(sourcePort);
		newMessage.add(args);
		
		return newMessage;
	}
}
