#include <SoftwareSerial.h>

//pins for the RGB LED for each warning
const int redPin=A2; //PWM pin
const int groundPin=A3;
const int greenPin=A4;
const int bluePin=A5;

//pin for the attached speaker
const int speakerPin=12;


SoftwareSerial mySoftSerial(2,3);

void setup()
{
  Serial.begin(9600);
  mySoftSerial.begin(9600);

  //setup LED pins
  pinMode(groundPin,OUTPUT);
  pinMode(bluePin,OUTPUT);
  pinMode(greenPin,OUTPUT);
  pinMode(redPin,OUTPUT);

  digitalWrite(groundPin,LOW);
  digitalWrite(bluePin,LOW);
  digitalWrite(greenPin,LOW);
  digitalWrite(redPin,LOW);


  //setup speaker
  setupBuzzer(speakerPin);

  playTone(4186,100);
  delay(50);
  playTone(3951,100);
  delay(50);
  playTone(3520,100);
  delay(50);
  playTone(2093,100);


  setupLCD();
}

const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;
const byte HEARTBEAT=0xD;
const byte SYSTEM_CHECK=0xE;

//add in byte commands for starting/stopping race.
const byte PRE_RACE=0x1A;
const byte RACE=0x1B;
const byte POST_RACE=0x1C;

enum RaceState {PRE,ACTIVE,POST};
//variables to hold the number of violations
int numLaneViolations=0;
int numLapsCompleted=0;
int numCollisions=0;
boolean raceActive=true;

//true to falsh a white LED for the heartbeat
const boolean SHOW_HEART_BEAT=false;

int counter=0;
byte readArray[4];

void loop()
{
  updateLCD(numLapsCompleted,numLaneViolations,numCollisions);

  //while(Serial.available()>0)
  while(mySoftSerial.available()>0)
  {
    //readArray[counter%4]=Serial.read(); 
    readArray[counter%4]=mySoftSerial.read();
    counter++;

    //determine if this packet is valid
    boolean validPacket=(readArray[0]^readArray[1]^readArray[2]^readArray[3])==0xff;

    if(validPacket)
    {
      //process this packet
      byte commandByte=readArray[0];
      byte args[2]={
        readArray[1],readArray[2]      };

      if(commandByte==LANE_VIOLATION)
      {
        executeLaneViolation();
        if(raceActive){
          numLaneViolations++;
        }
      }
      else if(commandByte==COLLISION_WARNING)
      {
        executeCollisionWarning(); 
        if(raceActive){
          numCollisions++;
        }
      }
      else if(commandByte==LAP_STARTSTOP)
      { 
        executeLapStartStop();
        if(raceActive){
          numLapsCompleted++;
        }
      }
      else if(commandByte==HEARTBEAT)
      {

        if(SHOW_HEART_BEAT==true)
        {
          digitalWrite(redPin,HIGH);
          digitalWrite(greenPin,HIGH);
          digitalWrite(bluePin,HIGH);

          delay(50);

          digitalWrite(redPin,LOW);
          digitalWrite(greenPin,LOW);
          digitalWrite(bluePin,LOW);

        }
      }
      else if(commandByte==SYSTEM_CHECK)
      {
        executeSystemCheck(); 

      }
      else if(commandByte==PRE_RACE)
      {
        numLaneViolations=numLapsCompleted=numCollisions=0;
        raceActive=false;
      }
      else if(commandByte==RACE)
      {
         raceActive=true; 
      }
      else if(commandByte==POST_RACE)
      {
        raceActive=false; 
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

  digitalWrite(redPin,LOW);
  digitalWrite(greenPin,HIGH);
  digitalWrite(bluePin,LOW);

  StopTone();
  playTone(1046,750);
  StopTone();  

  delay(50);

  digitalWrite(greenPin,LOW);
  playTone(786,750);

}

void executeCollisionWarning()
{

  digitalWrite(greenPin,LOW);
  digitalWrite(redPin,HIGH);
  digitalWrite(bluePin,LOW);
  StopTone();
  playTone(555,500);
  StopTone();

  delay(50);

  digitalWrite(redPin,LOW);
  playTone(0,500);

}

void executeLapStartStop()
{
  digitalWrite(bluePin,HIGH);
  digitalWrite(greenPin,LOW);
  digitalWrite(redPin,LOW);
  StopTone();
  playTone(783,150);
  StopTone();

  delay(50);

  digitalWrite(bluePin,LOW);
  playTone(1046,150);

}

void executeSystemCheck()
{
  digitalWrite(redPin,HIGH);
  digitalWrite(greenPin,HIGH);
  digitalWrite(bluePin,HIGH);
  StopTone();
  playTone(4186,100);
  StopTone();
  delay(50);
  digitalWrite(redPin,LOW);
  digitalWrite(greenPin,LOW);
  digitalWrite(bluePin,LOW);
  playTone(4186,100);
}

