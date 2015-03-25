//LCD screen includes
#include <LiquidCrystal.h>
#include <LCDKeypad.h>
LiquidCrystal lcd(8, 13, 9, 4, 5, 6, 7);

//vars for LCD screen
int adc_key_val[5] ={
  50, 200, 400, 600, 800 };
int NUM_KEYS = 5;
int adc_key_in;
int key=-1;
int oldkey=-1;

char msgs[5][16] = {
  "Right Key OK ",
  "Up Key OK    ",               
  "Down Key OK  ",
  "Left Key OK  ",
  "Select Key OK" };

//recognized buttons
const int NO_BUTTON=-1;
const int LEFT_BUTTON=0;
const int RIGHT_BUTTON=1;
const int UP_BUTTON=2;
const int DOWN_BUTTON=3;
const int SELECT_BUTTON=4;

const int buttons[]={
  RIGHT_BUTTON,UP_BUTTON,DOWN_BUTTON,LEFT_BUTTON,SELECT_BUTTON};


void setupLCD()
{
  lcd.begin(16, 2);

  /*  lcd.clear();
   lcd.setCursor(0,0);
   lcd.print("     hello! ");
   lcd.print("     welcome!");
   lcd.setCursor(0,1);
   lcd.print("ICAT   ICAT");
   lcd.print("Cardboard Derby");
   delay(1000);
   
   lcd.setCursor(0,0);
   for (char k=0;k<26;k++)
   {
   lcd.scrollDisplayLeft();
   delay(400);
   }*/
  lcd.clear();
  lcd.setCursor(0,0); 
  lcd.print(" Ready to Race? "); 
}

int currentLaps=-1;
int currentViolations=-1;
int currentCollisions=-1;
void updateLCD(int laps, int violations, int collisions)
{
  boolean updateRequired=false;
  
  if(currentLaps!=laps)
  {
     currentLaps=laps;
     updateRequired=true;
  }
  
  if(currentViolations!=violations)
  {
     currentViolations=violations; 
     updateRequired=true;
  }
  
  if(currentCollisions!=collisions)
  {
     currentCollisions=collisions;
     updateRequired=true;
  }
  
  if(updateRequired)
  {
     lcd.clear(); 
     
     String upper="";
     upper= upper+"Lap: "+currentLaps+"  Lane: "+currentViolations;
     
     String lower="";
     lower=lower+"Crash: "+currentCollisions;

     lcd.setCursor(0,0);
     lcd.print(upper.c_str());
  
     lcd.setCursor(0,1);
     lcd.print(lower.c_str());
  }
}

