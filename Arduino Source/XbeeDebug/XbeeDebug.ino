/*
*
**
Program lights up an LED whenever a byte is received over Xbee. Useful for determining network connectivity.
*/

#include <SoftwareSerial.h>

const int greenPin=4;
const int groundPin=5;
const int redPin=6;
const int bluePin=7;

//soft serial for use by the Xbee
SoftwareSerial mySerial(2, 3); // RX, TX
void setup()
{
  mySerial.begin(9600);
  Serial.begin(9600);

  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }

  //setup LED pins
  pinMode(groundPin,OUTPUT);
  pinMode(bluePin,OUTPUT);
  digitalWrite(groundPin,LOW);
  digitalWrite(bluePin,HIGH);
  
  Serial.println("Hello, I'm a VTTI Derby Car");
}


boolean serialData=false;
long lastData=0;
void loop()
{
  //poll for new data
  while(mySerial.available()>0)
  {
    byte data=mySerial.read();

    serialData=true;    
    
    Serial.println("Received byte!");
    
    lastData=millis();
  }
  
  
  //if received data, light up!
  if(serialData==true)
  {
     digitalWrite(bluePin,HIGH); 
     mySerial.write("!");
     serialData=false;
  }
  
  //if it's been 1000ms since the last byte, turn off the LED
  else
  {
     if(millis()-lastData>1000)
     {
       digitalWrite(bluePin,LOW); 
     }
  }
}

