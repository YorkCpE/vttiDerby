package edu.vt.icat.derby;

import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;

/**
 * Contains largely static information about the Derby Cars. Is used to find their Xbee network address.
 * @author Jason Forsyth
 *
 */
public class XbeeID 
{
	private byte[] serialNumber={0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
	private byte[] MYaddress={0x0,0x0};
	private String nodeIdentifier="";
	

	
	public XbeeID(LicenseColor c, LicenseShape s) 
	{
		byte upperBits = 0x0;
		byte lowerBits = 0x0;
		
		switch (s) 
		{
		case Triangle:
			upperBits=0x0;
			break;
		case Square:
			upperBits=0x10;
			break;
		case Circle:
			upperBits=0x20;
			break;
		default: 
			upperBits=0x0;
			break;
		}
		
		switch(c)
		{
		case Red:
			lowerBits=0x09;
			break;
		case Blue:
			lowerBits=0xA;
			break;
		case Green:
			lowerBits=0xB;
			break;
		case Yellow:
			lowerBits=0xC;
			break;
		case Purple:
			lowerBits=0xD;
			break;
		case Black:
			lowerBits=0xE;
			break;
		case Orange:
			lowerBits=0xF;
			break;
		default:
			lowerBits=0x0;
			break;
		}
		
		
		String identified=c.toString()+s.toString();
		setNodeIdentifier(identified.toUpperCase());
		
		MYaddress= new byte[]{0x0,(byte) (upperBits^lowerBits)};
	}
	
	private boolean setNodeIdentifier(String ni)
	{
		if(ni.getBytes().length>20)
		{
			System.out.println("Error: the name "+ni+" is greater than 20 bytes and cannot be used.");
			return false;
		}
		
		nodeIdentifier=ni;
		
		return true;
	}
	
	public byte[] getSerialNumber()
	{
		return serialNumber;
	}
	
	public byte[] getMYAddress()
	{
		
		return MYaddress;
	}
	
	public String getNodeIdentifier()
	{
		return nodeIdentifier;
	}
}
