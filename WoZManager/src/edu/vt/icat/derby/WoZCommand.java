package edu.vt.icat.derby;

import oscP5.OscMessage;
import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;

public class WoZCommand 
{
	public static final String LANE_VIOLATION="/laneViolation";
	public static final String COLLISION_WARNING="/collisionWarning";
	public static final String LAP_STARTSTOP="/lapStartStop";
	
	private String target;
	private String command;
	private String args;
	
	private static final byte LANE_VIOLATION_BYTE=0xA;
	private static final byte COLLISION_WARNING_BYTE=0xB;
	private static final byte LAP_STARTSTOP_BYTE=0xC;
	
	public WoZCommand(LicenseColor color, LicenseShape shape, String _command, String _args) 
	{
		target=color+","+shape;
		command=_command;
		args=_args;
	}

	public OscMessage generateOscMessage() 
	{
		OscMessage newMessage = new OscMessage(command);
		newMessage.add(target);
		newMessage.add(args);
		
		return newMessage;
	}

	public String getTarget()
	{
		return target;
	}
	
	public int[] generateXbeePayload()
	{	
		byte commandByte=0x0;
		if(command.equals(LANE_VIOLATION))
		{
			commandByte=LANE_VIOLATION_BYTE;
		}
		else if(command.equals(COLLISION_WARNING))
		{
			commandByte=COLLISION_WARNING_BYTE;
		}
		else if(command.equals(LAP_STARTSTOP))
		{
			commandByte=LAP_STARTSTOP_BYTE;
		}
		
		byte[] argBytes={0x0,0x0};
		
		if(args.getBytes().length==1)
		{
			byte b = args.getBytes()[0];
			argBytes= new byte[]{0x0,b};
		}
		else if(args.getBytes().length>=2)
		{
			byte a = args.getBytes()[0];
			byte b = args.getBytes()[1];
			argBytes= new byte[]{a,b};
		}
		
		byte checkSum=(byte) (commandByte^argBytes[0]^argBytes[1]^0xff);
		int[] payload={commandByte,argBytes[0],argBytes[1],checkSum};
		return payload;
	}

	public String getCommand() 
	{
		return this.command;
	}
}
