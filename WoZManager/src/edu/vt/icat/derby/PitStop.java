package edu.vt.icat.derby;

import netP5.NetAddress;
import oscP5.OscP5;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.DropdownList;
import edu.vt.icat.derby.DerbyCar.LicenseColor;
import edu.vt.icat.derby.DerbyCar.LicenseShape;
import processing.core.PApplet;

// another comment

/**
 * PitStop GUI
 * @author Nate Hughes njh2986@vt.edu
 * @author Jason Forsyth jforsyth@vt.edu
 */

public class PitStop extends PApplet implements ControlListener
{

	private static final long serialVersionUID = 7253625949765492271L;

	//int control;

	//boolean locked = false;

	ControlP5 cp5;

	//CheckBox checkbox;

	DropdownList d1;

	//int myColorBackground;
	//int cnt = 0;

	//int background = color(204, 102, 0);

	int colorRed = color(255, 0, 0);
	int colorBlue = color(0, 0, 255);
	int colorGreen = color(0, 255, 0);
	int colorOrange = color(255, 165, 0);
	int colorYellow = color(255, 255, 0);
	int colorPurple = color(128, 0, 128);
	int colorBlack = color(0, 0, 0);
	int colorGrey = color(128,128,128);

	// instantiating the circles
	CircleButton red_circle = new CircleButton(150, 150, 50, colorRed, colorGrey);
	CircleButton blue_circle = new CircleButton(215, 150, 50, colorBlue, colorGrey);
	CircleButton green_circle = new CircleButton(300, 150, 50, colorGreen, colorGrey);
	CircleButton orange_circle = new CircleButton(375, 150, 50, colorOrange, colorGrey);
	CircleButton yellow_circle = new CircleButton(450, 150, 50, colorYellow, colorGrey);
	CircleButton purple_circle = new CircleButton(525, 150, 50, colorPurple, colorGrey);
	CircleButton black_circle = new CircleButton(600, 150, 50, colorBlack, colorGrey);

	//put all the circle buttons in an array
	Button[] circleButtons={red_circle,blue_circle, green_circle, orange_circle, yellow_circle, purple_circle, black_circle};

	//instantiating the squares
	RectButton red_square = new RectButton(150, 150, 50, colorRed, colorGrey);
	RectButton blue_square = new RectButton(215, 150, 50, colorBlue, colorGrey);
	RectButton green_square = new RectButton(300, 150, 50, colorGreen, colorGrey);
	RectButton orange_square = new RectButton(375, 150, 50, colorOrange, colorGrey);
	RectButton yellow_square = new RectButton(450, 150, 50, colorYellow, colorGrey);
	RectButton purple_square = new RectButton(525, 150, 50, colorPurple, colorGrey);
	RectButton black_square = new RectButton(600, 150, 50, colorBlack, colorGrey);

	//put all the rectangle buttons into an array
	Button[] rectangleButtons={red_square, blue_square, green_square, orange_square, yellow_square, purple_square, black_square};
	
	//instantiate the triangle buttons
	TriangleButton red_triangle = new TriangleButton(150, 150, 60, colorRed, colorGrey);
	TriangleButton blue_triangle = new TriangleButton(215, 150, 60, colorBlue, colorGrey);
	TriangleButton green_triangle = new TriangleButton(300, 150, 60, colorGreen, colorGrey);
	TriangleButton orange_triangle = new TriangleButton(375, 150, 60, colorOrange, colorGrey);
	TriangleButton yellow_triangle = new TriangleButton(450, 150, 60, colorYellow, colorGrey);
	TriangleButton purple_triangle = new TriangleButton(525, 150, 60, colorPurple, colorGrey);
	TriangleButton black_triangle = new TriangleButton(600, 150, 60, colorBlack, colorGrey);

	//put all the triangle buttons into an array
	Button[] triangleButtons={red_triangle, blue_triangle, green_triangle, orange_triangle, yellow_triangle, purple_triangle, black_triangle};
	
	Button[] currentButtons = {};

	private boolean locked=false;
	
	
	private NetAddress managerAddress;
	
	public PitStop()
	{
		managerAddress = new NetAddress(WozManager.DefaultHostName, WozManager.MANAGER_DEFAULT_LISTENING_PORT);
	}
	
	@Override
	public void setup() 
	{

		size(800, 500);
		noStroke(); 
		background(000);
		smooth();

		cp5 = new ControlP5(this);
		// create a DropdownList
		d1 = cp5.addDropdownList("myList-d1")
				.setPosition(10, 25)
				;

		// a convenience function to customize a DropdownList
		d1.setBackgroundColor(color(190));
		d1.setItemHeight(20);
		d1.setBarHeight(15);
		d1.addItem("Squares", 1);
		d1.addItem("Triangles", 2);
		d1.addItem("Circles", 3);
		
		cp5.addTextfield("Host IP")
	     .setPosition((float).75*width,(float).75*height)
	     .setSize(100,40)
	     .setFocus(true)
	     .setFont(createFont("arial",20))
	     .setColor(color(255,0,0))
	     .addListener(new ControlListener() {
			
			@Override
			public void controlEvent(ControlEvent arg0) 
			{
				String newHostIP=arg0.getStringValue();
				System.out.println("Host IP Set to "+newHostIP);
				
				managerAddress = new NetAddress(newHostIP, WozManager.MANAGER_DEFAULT_LISTENING_PORT);
			}
		})
	     ;
	}

	@Override
	public void draw() 
	{
		background(255);

		for(Button b : currentButtons)
		{
			if(mousePressed==true)
			{
				if(b.over() & !locked)
				{
					locked=true;
					background(0);
					
					//println("Button!");
					b.execute();
				}
			}
			
			b.update();
			
			b.display();
		}
	}
	
	public void mouseReleased()
	{
		locked=false;
	}

	@Override
	public void controlEvent(ControlEvent theEvent) 
	{
		if (theEvent.isFrom(d1)) 
		{
			if (theEvent.getGroup().getValue() == 1.0)
			{
				currentButtons=rectangleButtons;
			}
			if (theEvent.getGroup().getValue() == 2.0)
			{
				currentButtons=triangleButtons;
			}
			if (theEvent.getGroup().getValue() == 3.0)
			{
				currentButtons=circleButtons;
			}
		}
	}

	abstract class Button
	{
		protected int buttonX;

		protected int buttonY;

		protected int buttonSize;

		protected int basecolor, highlightcolor;

		protected int currentcolor;
		
		protected LicenseShape licenseShape;
		protected LicenseColor licenesColor;

		//protected boolean over = false;

		//protected boolean pressed = false;   

		public Button(int ix, int iy, int isize, int icolor, int ihighlight)
		{
			buttonX=ix;
			buttonY=iy;
			buttonSize=isize;
			basecolor=icolor;
			highlightcolor=ihighlight;
			
			if(basecolor==colorRed)
			{
				licenesColor=LicenseColor.Red;
			}
			else if(basecolor == colorBlack)
			{
				licenesColor=LicenseColor.Black;
			}
			else if(basecolor == colorBlue)
			{
				licenesColor=LicenseColor.Blue;
			}
			else if(basecolor == colorGreen)
			{
				licenesColor=LicenseColor.Green;
			}
			else if(basecolor == colorOrange)
			{
				licenesColor=LicenseColor.Orange;
			}
			else if(basecolor == colorPurple)
			{
				licenesColor=LicenseColor.Purple;
			}
			else if(basecolor == colorYellow)
			{
				licenesColor=LicenseColor.Yellow;
			}
		}

		public void update() 
		{
			if (over()) 
			{
				currentcolor = highlightcolor;
			} 
			else 
			{
				currentcolor = basecolor;
			}
		}
		
		public void execute()
		{
			WoZCommand systemCheckCommand = new WoZCommand(licenesColor, licenseShape, WoZCommand.SYSTEM_CHECK, "");
			OscP5.flush(systemCheckCommand.generateOscMessage(), managerAddress);
		}
		public abstract boolean over(); 
		public abstract void display();
	}



	class CircleButton extends Button 
	{ 
		public CircleButton(int ix, int iy, int isize, int icolor, int ihighlight) 
		{
			super(ix, iy, isize, icolor, ihighlight);
			licenseShape=LicenseShape.Circle;
		}

		@Override
		public boolean over() 
		{
			float disX = buttonX - mouseX;

			float disY = buttonY - mouseY;

			if (sqrt(sq(disX) + sq(disY)) < buttonSize/2 ) 
			{
				return true;
			} 

			else
			{
				return false;
			}
		}

		@Override
		public void display() 
		{
			stroke(255);
			fill(currentcolor);
			ellipse(buttonX, buttonY, buttonSize, buttonSize);
		}
	}

	class TriangleButton extends Button
	{
		private int x1,y1,x2,y2,x3,y3;
		private int l=1;
		
		public TriangleButton(int ix, int iy, int isize, int icolor, int ihighlight) 
		{
			super(ix, iy, isize, icolor, ihighlight);
			
			licenseShape=LicenseShape.Triangle;
			
			l=isize;
			
			x1=ix;
			y1=iy;
			
			x2=x1+l/2;
			y2=y1+l/2;
			
			x3=x1+l;
			y3=y1;
			
			
		}

		@Override
		public boolean over() 
		{
			if(mouseX>x1 && mouseX<x3 && mouseY<y2)
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		@Override
		public void display() 
		{
			fill(currentcolor);
			triangle(x1, y1, x2, y2, x3, y3);
			return;
		}

	}

	class RectButton extends Button
	{

		public RectButton(int ix, int iy, int isize, int icolor, int ihighlight) 
		{
			super(ix, iy, isize, icolor, ihighlight);
			
			licenseShape=LicenseShape.Square;
		}



		@Override
		public boolean over() 
		{
			if(mouseX>buttonX && mouseX<buttonX+buttonSize && mouseY>buttonY && mouseY<buttonY+buttonSize)
			{
				return true;
			}
			else
			{
				return false;
			}
			
		}

		@Override
		public void display() 
		{
			stroke(255);
			fill(currentcolor);
			rect(buttonX, buttonY, buttonSize, buttonSize);
		}
	}

	public static void main(String[] args) 
	{
		PApplet.main(new String[] {PitStop.class.getName() });
	}
}
