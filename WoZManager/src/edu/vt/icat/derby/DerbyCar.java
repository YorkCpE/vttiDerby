package edu.vt.icat.derby;
/**
 * Static class used to hold information about the derby cars. Should not be used to identify a car individually (e.g. a particular instance of Green Square) 
 * but should be used as a container to hold general information about the cars (e.g. Green Square cars have the MY address... 0xxxx).
 * @author Jason Forsyth
 *
 */
public class DerbyCar
{
	
	//list of license shapes in the derby
	static public enum LicenseShape {Triangle,Square,Circle};
	
	//list of license colors in the derby;
	static public enum LicenseColor {Red, Blue, Green, Yellow, Purple, Black, Orange};
	
	//field to hold xbee address information
	private XbeeID xbeeID;
	
	//color of the license plate
	private LicenseColor myColor;
	
	//shape of the license plate
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
	
	/**
	 * Returns the Node Identifier for the car on the network
	 * @return
	 */
	public String getXbeeName()
	{
		return myColor.toString().toLowerCase()+myShape.toString().toLowerCase();
	}
	
	/**
	 * Return the 16-bit MY address for this car
	 * @return
	 */
	public byte[] getXbeeMYAddress()
	{
		return xbeeID.getMYAddress();
	}

	/**
	 * Returns the MY address of some particular car.
	 * @param target
	 * @return
	 */
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

	/**
	 * Returns the ArduinoName used by the WoZClients over OSC to indicate which car they're talking about
	 * @return
	 */
	public String getArduinoName() 
	{
		return myColor+","+myShape;
	}
}
