package edu.vt.icat.derby;

import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;

public class XbeeID 
{
	private byte[] serialNumber={0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
	private byte[] MYaddress={0x0,0x0};
	private String nodeIdentifier="";
	

	
	public XbeeID(LicenseColor c, LicenseShape s) 
	{
		byte upperByte = 0x0;
		byte lowerByte = 0x0;
		
		switch (s) 
		{
		case Triangle:
			upperByte=0x0;
			break;
		case Square:
			upperByte=0x10;
			break;
		case Circle:
			upperByte=0x20;
			break;
		default: 
			upperByte=0x0;
			break;
		}
		
		switch(c)
		{
		case Red:
			lowerByte=0x09;
			break;
		case Blue:
			lowerByte=0xA;
			break;
		case Green:
			lowerByte=0xB;
			break;
		case Yellow:
			lowerByte=0xC;
			break;
		case Purple:
			lowerByte=0xD;
			break;
		case Black:
			lowerByte=0xE;
			break;
		case Orange:
			lowerByte=0xF;
			break;
		default:
			lowerByte=0x0;
			break;
		}
		
		
		String identified=c.toString()+s.toString();
		setNodeIdentifier(identified.toUpperCase());
		
		MYaddress= new byte[]{upperByte,lowerByte};
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
