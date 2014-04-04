#include <SoftwareSerial.h>

SoftwareSerial mySerial(2, 3); // RX, TX
void setup()
{
  mySerial.begin(9600);
  Serial.begin(9600);
  
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }
  
  Serial.println("Hello, I'm a VTTI Derby Car");
}


const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;

int counter=0;
byte readArray[4];
void loop()
{
  while(mySerial.available()>0)
  {
     readArray[counter%4]=mySerial.read(); 
     counter++;
     
     boolean validPacket=(readArray[0]^readArray[1]^readArray[2]^readArray[3])==0xff;
     
     if(validPacket)
     {
        //process this packet 
      
       Serial.print(readArray[0],HEX);
       Serial.print(",");
       Serial.print(readArray[1],HEX);
       Serial.print(",");
       Serial.print(readArray[2],HEX);
       Serial.print(",");
       Serial.println(readArray[3],HEX);
       
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
       
       //this is needed because otherwise the command may execute multiple times
       //depending on what is received later (mainly because a failure of my message encoding
       //but hey, at least we're not dealing with full Xbee frames
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


