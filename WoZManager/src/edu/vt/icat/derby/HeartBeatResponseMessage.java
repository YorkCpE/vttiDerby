package edu.vt.icat.derby;

/**
 * Wrapper class for the heart beat responses
 * @author Jason Forsyth
 *
 */
public class HeartBeatResponseMessage
{
	private String ip;
	private int port;
	private DerbyCar car;

	public HeartBeatResponseMessage(String sourceIP, int sourcePort, DerbyCar _car)
	{
		ip=sourceIP;
		port=sourcePort;
		car=_car;
	}

	public String getDesintationIP()
	{
		return ip;
	}

	public int getDesinationPort()
	{
		return port;
	}
	public DerbyCar getCar()
	{

		return car;
	}
}
