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
	int orange = orange = color(0, 0, 255);
	int yellow = yellow = color(255, 128, 0);
	int purple = purple = color(128, 0, 128);
	int black = black = color(0, 0, 0);

	// instantiating the circles
	CircleButton red_circle = new CircleButton(150, 150, 50, red, red);
	CircleButton blue_circle = new CircleButton(215, 150, 50, blue, red);
	CircleButton green_circle = new CircleButton(300, 150, 50, green, red);
	CircleButton orange_circle = new CircleButton(375, 150, 50, orange, red);
	CircleButton yellow_circle = new CircleButton(450, 150, 50, yellow, red);
	CircleButton purple_circle = new CircleButton(525, 150, 50, purple, red);
	CircleButton black_circle = new CircleButton(600, 150, 50, black, red);

	//instantiating the squares
	RectButton red_square = new RectButton(150, 150, 50, red, blue);



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

		background(255);

		update(mouseX, mouseY);

		if (control == 1)
		{ // draw 7 rectangle buttons

			red_square.display();
		}
		else if (control == 2)
		{ // draw 7 triangle buttons
			//triangle(300, 300, 50, 50);
		}
		else if (control == 3)
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
			if (mousePressed)
			{
				if (red_square.pressed())
					background(0);
				/*
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
				 */
			}

			if (locked == false)
				red_square.update();
			else 
				locked = false;

			break;

		case 2:

			break;

		case 3:
			if (mousePressed)
			{
				if (blue_circle.pressed())
					background(0);
				else if (blue_circle.pressed())
					background(0);
				else if (green_circle.pressed())
					background(0);
				else if (orange_circle.pressed())
					background(0);
				else if (yellow_circle.pressed())
					background(0);
				else if (purple_circle.pressed())
					background(0);
				else if (black_circle.pressed())
					background(0);
			}

			if (locked == false)
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
				background(100);

				control = 1;
			}
			if (theEvent.getGroup().getValue() == 2.0)
			{
				print("d1-2.0\n");
				background(200);

				control = 2;
			}
			if (theEvent.getGroup().getValue() == 3.0)
			{
				print("d1-3.0\n");
				background(300);

				control = 3;
			}
		}
	}

	void customize(DropdownList ddl) {
		// a convenience function to customize a DropdownList
		ddl.setBackgroundColor(color(190));
		ddl.setItemHeight(20);
		ddl.setBarHeight(15);
		ddl.captionLabel().set("shapes");
		ddl.captionLabel().style().marginTop = 3;
		ddl.captionLabel().style().marginLeft = 3;
		ddl.valueLabel().style().marginTop = 3;
		ddl.addItem("Squares", 1);
		ddl.addItem("Triangles", 2);
		ddl.addItem("Circles", 3);
	}

	class Button

	{

		int x, y;

		int size;

		int basecolor, highlightcolor;

		int currentcolor;

		boolean over = false;

		boolean pressed = false;   



		void update() 

		{
			if (over()) {

				currentcolor = highlightcolor;
			} 

			else {

				currentcolor = basecolor;
			}
		}



		boolean pressed() 

		{

			if (over) {

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



		boolean overCircle(int x, int y, int diameter) 

		{

			float disX = x - mouseX;

			float disY = y - mouseY;

			if (sqrt(sq(disX) + sq(disY)) < diameter/2 ) {

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



		@Override
		boolean over() 

		{

			if ( overCircle(x, y, size) ) {

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



		@Override
		boolean over() 

		{

			if ( overRect(x, y, size, size) ) {

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

			rect(x, y, size, size);
		}
	}

	public static void main(String[] args) 
	{
		PApplet.main(new String[] {PitStop.class.getName() });

	}

}
