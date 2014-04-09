package edu.vt.icat.derby;

public class DerbyCar 
{
	
	static public enum LicenseShape {Triangle,Circle,Square};
	static public enum LicenseColor {Red, Blue, Green, Orange, Yellow, Purple, Black};
	
	private XbeeID xbeeID;
	private LicenseColor myColor;
	private LicenseShape myShape;
	
	public DerbyCar(LicenseColor c, LicenseShape s) 
	{
		myColor=c;
		myShape=s;
		xbeeID=new XbeeID(c,s);
	}
	
	public DerbyCar(String color, String shape)
	{
		this(LicenseColor.valueOf(color),LicenseShape.valueOf(shape));
	}
	
	public String getXbeeName()
	{
		return myColor.toString().toLowerCase()+myShape.toString().toLowerCase();
	}
	
	public byte[] getXbeeMYAddress()
	{
		return xbeeID.getMYAddress();
	}

	public static byte[] lookupMYAddress(String target) 
	{
		String[] splits = target.split(",");
		
		String color=splits[0];
		String shape=splits[1];
		
		XbeeID xbeeInfo = new DerbyCar(color, shape).getXBeeInfo();
		
		return xbeeInfo.getMYAddress();
	}

	private XbeeID getXBeeInfo() 
	{
		return this.xbeeID;
	}
	
	public LicenseColor getColor()
	{
		
		return this.myColor;
	}
	
	public LicenseShape getShape()
	{
		
		return this.myShape;
	}

	public String getArduinoName() 
	{
		return myColor+","+myShape;
	}
}
