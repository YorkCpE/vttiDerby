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

const byte LANE_VIOLATION='1';
const byte COLLISION_WARNING='2';
const byte LAP_STARTSTOP='3';
const byte HEARTBEAT='4';
const byte SYSTEM_CHECK='5';

//true to falsh a white LED for the heartbeat
const boolean SHOW_HEART_BEAT=true;

void displayMenu()
{
  Serial.println("---- Please select one of the following options ----");
  Serial.println("1) Lane Violation\n2) Collision Warning\n3) Lap StartStop\n4) Heart Beat\n5) System Check");
  while(Serial.available()==0){}

}
void loop()
{
  displayMenu();  

  while(Serial.available()>0)
  {
    char commandByte = Serial.read();

    if(commandByte==LANE_VIOLATION)
    {
      Serial.println("Performing Lane Violation!");
      executeLaneViolation();
    }
    else if(commandByte==COLLISION_WARNING)
    {
      Serial.println("Performing Collision Warning!");
      executeCollisionWarning(); 
    }
    else if(commandByte==LAP_STARTSTOP)
    { 
      Serial.println("Performing Lap Start/Stop!");
      executeLapStartStop();
    }
    else if(commandByte==HEARTBEAT)
    {

      Serial.println("Performing Heart Beat!");
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
      Serial.println("Performing System Check!");
      executeSystemCheck(); 

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

