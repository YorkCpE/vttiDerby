#include <SoftwareSerial.h>

SoftwareSerial mySerial(2, 3); // RX, TX
void setup()
{
  mySerial.begin(9600);
  Serial.begin(9600);
  
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
}


const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;

int counter=0;
byte readArray[4];
void loop()
{
  while(mySerial.available())
  {
     readArray[counter%4]=mySerial.read(); 
     counter++;
     
     boolean validPacket=(readArray[0]^readArray[1]^readArray[2]==readArray[3]);
     
     if(validPacket)
     {
        //process this packet 
       
       byte commandByte=readArray[0];
       byte args[2]={readArray[1],readArray[2]};
       
       if(commandByte==LANE_VIOLATION)
       {
          executeLaneViolation();
          Serial.println("Lane Violation"); 
       }
       else if(commandByte==COLLISION_WARNING)
       {
          executeCollisionWarning(); 
          Serial.println("Collision Warning");
       }
       else if(commandByte==LAP_STARTSTOP)
       { 
          executeLapStartStop();
          Serial.println("Lap Start/Stop");
       }
       
       readArray[0]=0x0;
       readArray[1]=0x0;
       readArray[2]=0x0;
       readArray[3]=0x0;
     }
  }
}

void executeLaneViolation()
{
  
}

void executeCollisionWarning()
{
  
}

void executeLapStartStop()
{
  
  
}


