package edu.vt.icat.derby;

import controlP5.CheckBox;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.DropdownList;
import processing.core.PApplet;

// another comment

/**
 * PitStop GUI
 * @author Nate Hughes njh2986@vt.edu
 *
 */

public class PitStop extends PApplet implements ControlListener
{

	private static final long serialVersionUID = 7253625949765492271L;

	int control;

	boolean locked = false;


	ControlP5 cp5;

	CheckBox checkbox;

	DropdownList d1;

	int myColorBackground;
	int cnt = 0;

	int background = color(204, 102, 0);

	int red = color(255, 0, 0);
	int blue = color(0, 0, 255);
	int green = color(0, 255, 0);
	int yellow = color(255, 255, 0);
	int orange = color(255, 128, 0);
	int purple = color(128, 0, 128);
	int black = color(0, 0, 0);

	// instantiating the circles
	CircleButton red_circle = new CircleButton(175, 175, 50, red, red);
	CircleButton blue_circle = new CircleButton(250, 175, 50, blue, blue);
	CircleButton green_circle = new CircleButton(325, 175, 50, green, green);
	CircleButton orange_circle = new CircleButton(400, 175, 50, orange, orange);
	CircleButton yellow_circle = new CircleButton(475, 175, 50, yellow, yellow);
	CircleButton purple_circle = new CircleButton(550, 175, 50, purple, purple);
	CircleButton black_circle = new CircleButton(625, 175, 50, black, black);

	//instantiating the squares
	RectButton red_square = new RectButton(150, 150, 50, red, red);
	RectButton blue_square = new RectButton(225, 150, 50, blue, blue);
	RectButton green_square = new RectButton(300, 150, 50, green, green);
	RectButton orange_square = new RectButton(375, 150, 50, orange, orange);
	RectButton yellow_square = new RectButton(450, 150, 50, yellow, yellow);
	RectButton purple_square = new RectButton(525, 150, 50, purple, purple);
	RectButton black_square = new RectButton(600, 150, 50, black, black);

	//instantiating the triangles
	TriButton red_tri = new TriButton(150, 200, 175, 150, 200, 200, red, red);
	TriButton blue_tri = new TriButton(225, 200, 250, 150, 275, 200, blue, blue);
	TriButton green_tri = new TriButton(300, 200, 325, 150, 350, 200, green, green);
	TriButton orange_tri = new TriButton(375, 200, 400, 150, 425, 200, orange, orange);
	TriButton yellow_tri = new TriButton(450, 200, 475, 150, 500, 200, yellow, yellow);
	TriButton purple_tri = new TriButton(525, 200, 550, 150, 575, 200, purple, purple);
	TriButton black_tri = new TriButton(600, 200, 625, 150, 650, 200, black, black);



	@Override
	public void setup() 
	{

		size(800, 500);
		//colorMode(HSB, width, 100, width);
		noStroke(); 
		background(000);
		smooth();

		cp5 = new ControlP5(this);
		// create a DropdownList
		d1 = cp5.addDropdownList("myList-d1")
				.setPosition(10, 25)
				;

		customize(d1); // customize the first list
	}

	@Override
	public void keyPressed() {
		if (key==' ') {
			checkbox.deactivateAll();
		} 
		else {
			for (int i=0;i<6;i++) {
				// check if key 0-5 have been pressed and toggle
				// the checkbox item accordingly.
				if (keyCode==(48 + i)) { 
					// the index of checkbox items start at 0
					checkbox.toggle(i);
					println("toggle "+checkbox.getItem(i).name());
					// also see 
					// checkbox.activate(index);
					// checkbox.deactivate(index);
				}
			}
		}

		if (key=='1') {
			// set the height of a pulldown menu, should always be a multiple of itemHeight
			d1.setHeight(210);
		} 
		else if (key=='2') {
			// set the height of a pulldown menu, should always be a multiple of itemHeight
			d1.setHeight(120);
		}
		else if (key=='3') {
			// set the height of a pulldown menu item, should always be a fraction of the pulldown menu
			d1.setItemHeight(30);
		} 
		else if (key=='4') {
			// set the height of a pulldown menu item, should always be a fraction of the pulldown menu
			d1.setItemHeight(12);
			d1.setBackgroundColor(color(255));
		} 
		else if (key=='5') {
			// add new items to the pulldown menu
			int n = (int)(random(100000));
			d1.addItem("item "+n, n);
		} 
		else if (key=='6') {
			// remove items from the pulldown menu  by name
			d1.removeItem("item "+cnt);
			cnt++;
		}
		else if (key=='7') {
			d1.clear();
		}
	}

	@Override
	public void draw() {

		background(128);

		update(mouseX, mouseY);

		if(control == 1)
		  { // draw 7 rectangle buttons
		  
		      red_square.display();
		      blue_square.display();
		      green_square.display();
		      orange_square.display();
		      yellow_square.display();
		      purple_square.display();
		      black_square.display();
		  }
		  else if(control == 2)
		  { // draw 7 triangle buttons
		      red_tri.display();
		      blue_tri.display();
		      green_tri.display();
		      orange_tri.display();
		      yellow_tri.display();
		      purple_tri.display();
		      black_tri.display();
		  }
		  else if(control == 3)
		  { // draw 7 circle buttons
		  
		    red_circle.display();
		    blue_circle.display();
		    green_circle.display();
		    orange_circle.display();
		    yellow_circle.display();
		    purple_circle.display();
		    black_circle.display();
		  }
	}

	void update(int x, int y)

	{
		/*
  if(locked == false) {

   blue_circle.update();

   } 

   else {

   locked = false;

   }
		 */
		switch(control)
		{
		case 1:
	        
	        if(mousePressed)
	        {
	          if(red_square.pressed())
	           background(0);
	          else if(blue_square.pressed())
	           background(0);
	         else if(green_square.pressed())
	           background(0);
	         else if(orange_square.pressed())
	           background(0);
	         else if(yellow_square.pressed())
	           background(0);
	         else if(purple_square.pressed())
	           background(0);
	         else if(black_square.pressed())
	           background(0);
	        }
	        
	        if(locked == false)
	        {
	          red_square.update();
	          blue_square.update();
	          green_square.update();
	          orange_square.update();
	          yellow_square.update();
	          purple_square.update();
	          black_square.update();
	        }
	        else
	          locked = false;
	        
	       break;
	       
	      case 2:
	      
	        if(mousePressed)
	        {
	          if(red_tri.pressed())
	           background(0);
	          else if(blue_tri.pressed())
	           background(0);
	         else if(green_tri.pressed())
	           background(0);
	         else if(orange_tri.pressed())
	           background(0);
	         else if(yellow_tri.pressed())
	           background(0);
	         else if(purple_tri.pressed())
	           background(0);
	         else if(black_tri.pressed())
	           background(0);
	        }
	        
	        if(locked == false)
	        {
	          red_tri.update();
	          blue_tri.update();
	          green_tri.update();
	          orange_tri.update();
	          yellow_tri.update();
	          purple_tri.update();
	          black_tri.update();
	        }
	        else
	          locked = false;
	      
	        break;      
	      
	      case 3:
	      
	        if(mousePressed)
	        {
	          if(red_circle.pressed())
	           background(0);
	          else if(blue_circle.pressed())
	           background(0);
	         else if(green_circle.pressed())
	           background(0);
	         else if(orange_circle.pressed())
	           background(0);
	         else if(yellow_circle.pressed())
	           background(0);
	         else if(purple_circle.pressed())
	           background(0);
	         else if(black_circle.pressed())
	           background(0);
	        }
	        
	        if(locked == false)
	        {
	          red_circle.update();
	          blue_circle.update();
	          green_circle.update();
	          orange_circle.update();
	          yellow_circle.update();
	          purple_circle.update();
	          black_circle.update();
	        }
	        else
	          locked = false;
	      
	        break;
		}
	}

	@Override
	public void controlEvent(ControlEvent theEvent) {
		if (theEvent.isFrom(d1)) 
		{

			//println("event from group : "+theEvent.getGroup().getValue()+" from "+theEvent.getGroup());
			if (theEvent.getGroup().getValue() == 1.0)
			{
				print("d1-1.0\n");

				control = 1;
			}
			if (theEvent.getGroup().getValue() == 2.0)
			{
				print("d1-2.0\n");

				control = 2;
			}
			if (theEvent.getGroup().getValue() == 3.0)
			{
				print("d1-3.0\n");

				control = 3;
			}
		}
	}

	void customize(DropdownList ddl) {
		// a convenience function to customize a DropdownList
		ddl.setBackgroundColor(color(190));
		ddl.setItemHeight(20);
		ddl.setBarHeight(15);
		ddl.captionLabel().set("Shapes");
		ddl.captionLabel().style().marginTop = 3;
		ddl.captionLabel().style().marginLeft = 3;
		ddl.valueLabel().style().marginTop = 3;
		ddl.addItem("Squares", 1);
		ddl.addItem("Triangles", 2);
		ddl.addItem("Circles", 3);
	}

	class Button

	{

	  int x, y, x1, x2, x3, y1, y2, y3;

	  int size;

	  int basecolor, highlightcolor;

	  int currentcolor;

	  boolean over = false;

	  boolean pressed = false;   



	  void update() 

	  {
	    if(over()) {

	      currentcolor = highlightcolor;

	    } 

	    else {

	      currentcolor = basecolor;

	    }

	  }



	  boolean pressed() 

	  {

	    if(over) {

	      locked = true;

	      return true;

	    } 

	    else {

	      locked = false;

	      return false;

	    }    

	  }



	  boolean over() 

	  { 

	    return true; 

	  }



	  boolean overRect(int x, int y, int width, int height) 

	  {

	    if (mouseX >= x && mouseX <= x+width && 

	      mouseY >= y && mouseY <= y+height) {

	      return true;

	    } 

	    else {

	      return false;

	    }

	  }
	  
	  boolean overTri(int x1, int x2, int x3, int y1, int y2, int y3) 
	  {
	    
	    int base = x3 - x1;
	    int _height = y1 - y2;

	    if (mouseX >= x1 && mouseX <= x3 && mouseY >= y2 && mouseY <= y1 && abs(mouseX - x2 )<=abs((mouseY - y2)))
	      {

	      return true;

	    } 

	    else {

	      return false;

	    }

	  }



	  boolean overCircle(int x, int y, int diameter) 

	  {

	    float disX = x - mouseX;

	    float disY = y - mouseY;

	    if(sqrt(sq(disX) + sq(disY)) < diameter/2 ) {

	      return true;

	    } 

	    else {

	      return false;

	    }

	  }



	}



	class CircleButton extends Button

	{ 

	  CircleButton(int ix, int iy, int isize, int icolor, int ihighlight) 

	  {

	    x = ix;

	    y = iy;

	    size = isize;

	    basecolor = icolor;

	    highlightcolor = ihighlight;

	    currentcolor = basecolor;

	  }



	  boolean over() 

	  {

	    if( overCircle(x, y, size) ) {

	      over = true;

	      return true;

	    } 

	    else {

	      over = false;

	      return false;

	    }

	  }



	  void display() 

	  {

	    stroke(255);

	    fill(currentcolor);

	    ellipse(x, y, size, size);

	  }

	}



	class RectButton extends Button

	{

	  RectButton(int ix, int iy, int isize, int icolor, int ihighlight) 

	  {

	    x = ix;

	    y = iy;

	    size = isize;

	    basecolor = icolor;

	    highlightcolor = ihighlight;

	    currentcolor = basecolor;

	  }



	  boolean over() 

	  {

	    if( overRect(x, y, size, size) ) 
	    {

	      over = true;

	      return true;

	    } 

	    else 
	    {

	      over = false;

	      return false;

	    }

	  }



	  void display() 

	  {

	    stroke(255);

	    fill(currentcolor);

	    rect(x, y, size, size);

	  }

	}

	class TriButton extends Button

	{

	  TriButton(int ix1, int iy1, int ix2, int iy2, int ix3, int iy3, int icolor, int ihighlight) 

	  {

	    x1 = ix1;

	    y1 = iy1;
	    
	    x2 = ix2;

	    y2 = iy2;
	    
	    x3 = ix3;

	    y3 = iy3;

	    basecolor = icolor;

	    highlightcolor = ihighlight;

	    currentcolor = basecolor;

	  }



	  boolean over() 

	  {

	    if( overTri(x1, x2, x3, y1, y2, y3) ) {

	      over = true;

	      return true;

	    } 

	    else {

	      over = false;

	      return false;

	    }

	  }



	  void display() 

	  {

	    stroke(255);

	    fill(currentcolor);

	    triangle(x1, y1, x2, y2, x3, y3);

	  }

	}

	public static void main(String[] args) 
	{
		PApplet.main(new String[] {PitStop.class.getName() });

	}

}
