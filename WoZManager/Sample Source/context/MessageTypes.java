package edu.vt.ece.context;

public class MessageTypes 
{
	static final public String WIDGET_REGISTRATION = "/wregister";
	static final public String WIDGET_REGISTRATION_ACK = "/wAck";
	static final public String WIDGET_UPDATE = "/wUpdate";
	static final public String WIDGET_POLL = "/wPoll";
	static final public String WIDGET_POLL_RESPONSE = "/wPollRes";
	
	static final public String WIDGET_UNREGISTER = "/wuregister";
	static final public String WIDGET_UNREGISTER_ACK = "/wuregisterAck";
	
	static final public String DISCOVER_DHCP = "/discover";
	static final public String DISCOVER_DHCP_ACK = "/discoverAck";
	static final public String SIGN_OFF="/signOffClient";
	
	static final public String WIDGET_SUBSCRIPTION_REQ = "/wSubscribe";
	static final public String WIDGET_SUBSCRIBE_ACK = "/wSubAck";
	
	static final public String SERVICE_REGISTRATION="/wServiceReg";
	static final public String SERVICE_REGISTRATION_ACK="/wServiceRegAck";
	static final public String SERVICE_COMMAND="/ServiceCommand";
	
	static final public String ECHO = "/echo";
	static final public String ECHO_ACK = "/echoAck";
	
	static final public String LOOKUP = "/find";
	static final public String LOOKUP_ACK = "/findAck";
}
