#include <SoftwareSerial.h>

SoftwareSerial mySerial(2, 3); // RX, TX

const int greenPin=4;
const int groundPin=5;
const int redPin=6;
const int bluePin=7;

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
  pinMode(greenPin,OUTPUT);
  pinMode(redPin,OUTPUT);

  digitalWrite(groundPin,LOW);
  digitalWrite(bluePin,LOW);
  digitalWrite(greenPin,LOW);
  digitalWrite(redPin,LOW);

  Serial.println("Hello, I'm a VTTI Derby Car");
}


const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;
const byte HEARTBEAT=0xD;
const byte SYSTEM_CHECK=0xE;

const boolean LED_DEBUG=true;
const boolean SERIAL_DEBUG=true;

int counter=0;
byte readArray[4];

void loop()
{
  while(mySerial.available()>0)
  {
    readArray[counter%4]=mySerial.read(); 
    counter++;

    //determine if this packet is valid
    boolean validPacket=(readArray[0]^readArray[1]^readArray[2]^readArray[3])==0xff;

    if(validPacket)
    {
      //process this packet 

        if(SERIAL_DEBUG)
      {
        Serial.print(readArray[0],HEX);
        Serial.print(",");
        Serial.print(readArray[1],HEX);
        Serial.print(",");
        Serial.print(readArray[2],HEX);
        Serial.print(",");
        Serial.println(readArray[3],HEX);
      }

      byte commandByte=readArray[0];
      byte args[2]={
        readArray[1],readArray[2]            };

      if(commandByte==LANE_VIOLATION)
      {
        executeLaneViolation();

        if(SERIAL_DEBUG==true)
        {
          Serial.println("Lane Violation"); 
        }
      }
      else if(commandByte==COLLISION_WARNING)
      {
        executeCollisionWarning(); 
        if(SERIAL_DEBUG==true)
        {
          Serial.println("Collision Warning");
        }
      }
      else if(commandByte==LAP_STARTSTOP)
      { 
        executeLapStartStop();
        if(SERIAL_DEBUG==true)
        {
          Serial.println("Lap Start/Stop");
        }
      }
      else if(commandByte==HEARTBEAT)
      {
        if(SERIAL_DEBUG==true)
        {
          Serial.println("Heartbeat"); 
        }
        
        if(LED_DEBUG==true)
        {
           digitalWrite(redPin,HIGH);
           digitalWrite(greenPin,HIGH);
           digitalWrite(bluePin,LOW);
           
           delay(50);
           
           digitalWrite(redPin,LOW);
           digitalWrite(greenPin,LOW);
          
        }
      }
      else if(commandByte==SYSTEM_CHECK)
      {
        executeSystemCheck(); 
        if(SERIAL_DEBUG==true)
        {
         Serial.println("System Check"); 
        }
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
  if(LED_DEBUG==true)
  {
    digitalWrite(redPin,HIGH);
    digitalWrite(greenPin,LOW);
    digitalWrite(bluePin,LOW);

    delay(50);

    digitalWrite(redPin,LOW);
  }
}

void executeCollisionWarning()
{
  if(LED_DEBUG==true)
  {
    digitalWrite(greenPin,HIGH);
    digitalWrite(redPin,LOW);
    digitalWrite(bluePin,LOW);

    delay(50);

    digitalWrite(greenPin,LOW);
  }
}

void executeLapStartStop()
{
  if(LED_DEBUG==true)
  {
    digitalWrite(bluePin,HIGH);
    digitalWrite(greenPin,LOW);
    digitalWrite(greenPin,LOW);

    delay(50);

    digitalWrite(bluePin,LOW);
  }
}

void executeSystemCheck()
{


}




