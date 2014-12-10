//pins for the Speed LED
const int speedBluePin=2;
const int speedGreenPin=3; 
const int speedGroundPin=4; //PWM pin
const int speedRedPin=5; //PWM pin

//pins for the RGB LED for each warning
const int redPin=6; //PWM pin
const int groundPin=7;
const int greenPin=8;
const int bluePin=9;

//pin for the attached speaker
const int speakerPin=12;

void setup()
{
  Serial.begin(9600);

  //setup LED pins
  pinMode(groundPin,OUTPUT);
  pinMode(bluePin,OUTPUT);
  pinMode(greenPin,OUTPUT);
  pinMode(redPin,OUTPUT);
  
  pinMode(speedGroundPin,OUTPUT);
  pinMode(speedBluePin,OUTPUT);
  pinMode(speedGreenPin,OUTPUT);
  pinMode(speedRedPin,OUTPUT);

  //setup speed pins
  digitalWrite(speedGreenPin,LOW);
  digitalWrite(speedGroundPin,LOW);
  digitalWrite(speedRedPin,LOW);
  digitalWrite(speedGreenPin,LOW);  
  
  //setup speaker
  setupBuzzer(speakerPin);
  
  playTone(4186,100);
  delay(50);
  playTone(3951,100);
  delay(50);
  playTone(3520,100);
  delay(50);
  playTone(2093,100);
  
}

const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;
const byte HEARTBEAT=0xD;
const byte SYSTEM_CHECK=0xE;

//true to falsh a white LED for the heartbeat
const boolean SHOW_HEART_BEAT=true;

int counter=0;
byte readArray[4];

void loop()
{
  while(Serial.available()>0)
  {
    readArray[counter%4]=Serial.read(); 
    counter++;

    //determine if this packet is valid
    boolean validPacket=(readArray[0]^readArray[1]^readArray[2]^readArray[3])==0xff;

    if(validPacket)
    {
      //process this packet
      byte commandByte=0xC;//readArray[0];
      byte args[2]={readArray[1],readArray[2]};

      if(commandByte==LANE_VIOLATION)
      {
        executeLaneViolation();
      }
      else if(commandByte==COLLISION_WARNING)
      {
        executeCollisionWarning(); 
      }
      else if(commandByte==LAP_STARTSTOP)
      { 
        executeLapStartStop();
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

  digitalWrite(redPin,HIGH);
  digitalWrite(greenPin,LOW);
  digitalWrite(bluePin,LOW);
  digitalWrite(speedRedPin,HIGH);
  digitalWrite(speedGreenPin,LOW);
  digitalWrite(speedBluePin,LOW);
  StopTone();
  playTone(1046,750);
  StopTone();  

  delay(50);

  digitalWrite(redPin,LOW);
  digitalWrite(speedRedPin,LOW);
  playTone(786,750);

}

void executeCollisionWarning()
{

  digitalWrite(greenPin,HIGH);
  digitalWrite(redPin,LOW);
  digitalWrite(bluePin,LOW);
  digitalWrite(speedGreenPin,HIGH);
  digitalWrite(speedRedPin,LOW);
  digitalWrite(speedBluePin,LOW);
  StopTone();
  playTone(555,500);
  StopTone();

  delay(50);

  digitalWrite(greenPin,LOW);
  digitalWrite(speedGreenPin,LOW);
  playTone(0,500);

}

void executeLapStartStop()
{
  digitalWrite(bluePin,HIGH);
  digitalWrite(greenPin,LOW);
  digitalWrite(redPin,LOW);
  digitalWrite(speedBluePin,HIGH);
  digitalWrite(speedGreenPin,LOW);
  digitalWrite(speedRedPin,LOW);
  StopTone();
  playTone(783,150);
  StopTone();
  
  delay(50);

  digitalWrite(bluePin,LOW);
  digitalWrite(speedBluePin,LOW);
  playTone(1046,150);

}

void executeSystemCheck()
{
  digitalWrite(redPin,HIGH);
  digitalWrite(greenPin,HIGH);
  digitalWrite(bluePin,HIGH);
  digitalWrite(speedRedPin,HIGH);
  digitalWrite(speedGreenPin,HIGH);
  digitalWrite(speedBluePin,HIGH);
  StopTone();
  playTone(4186,100);
  StopTone();
  delay(50);
  digitalWrite(redPin,LOW);
  digitalWrite(greenPin,LOW);
  digitalWrite(bluePin,LOW);
  digitalWrite(speedRedPin,LOW);
  digitalWrite(speedGreenPin,LOW);
  digitalWrite(speedBluePin,LOW);
  playTone(4186,100);
}
