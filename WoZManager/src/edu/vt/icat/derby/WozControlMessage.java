package edu.vt.icat.derby;

import oscP5.OscMessage;

/**
 * WoZControlMessages are sent strictly between the WoZClient and WoZManger to maintain their relationship.
 * @author Jason Forsyth
 *
 */
public class WozControlMessage 
{
	public static final String ECHO="/echo";
	public static final String ECHO_ACK="/echoAck";
	
	public static final String HEARTBEAT="/heartBeat";
	public static final String HEARTBEAT_ACK="/heartBeatAck";
	
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
