//pins for the RGB LED for each warning
const int redPin=6; //PWM pin
const int groundPin=7;
const int greenPin=8;
const int bluePin=9;

//pin for the attached speaker
const int speakerPin=12;

void playTone(int freq, int duration)
{
  //if there's no duration, assume the note will play until stopped
  if(duration==0)
  {
    tone(speakerPin,freq);
  }

  //play the note for a certain duration
  else 
  {
    tone(speakerPin,freq);
    delay(duration);
    noTone(speakerPin);
  }

}

void setup()
{
  Serial.begin(9600);

  //setup LED pins
  pinMode(groundPin,OUTPUT);
  pinMode(bluePin,OUTPUT);
  pinMode(greenPin,OUTPUT);
  pinMode(redPin,OUTPUT);

  //setup speaker
  pinMode(speakerPin,OUTPUT);

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

  noTone(speakerPin);
  playTone(1046,750);
  noTone(speakerPin);  

  delay(50);

  digitalWrite(redPin,LOW);
  playTone(786,750);

}

void executeCollisionWarning()
{

  digitalWrite(greenPin,HIGH);
  digitalWrite(redPin,LOW);
  digitalWrite(bluePin,LOW);
  noTone(speakerPin);
  playTone(555,500);
  noTone(speakerPin);

  delay(50);

  digitalWrite(greenPin,LOW);
  playTone(0,500);

}

void executeLapStartStop()
{
  digitalWrite(bluePin,HIGH);
  digitalWrite(greenPin,LOW);
  digitalWrite(redPin,LOW);
  noTone(speakerPin);
  playTone(783,150);
  noTone(speakerPin);

  delay(50);

  digitalWrite(bluePin,LOW);
  playTone(1046,150);

}

void executeSystemCheck()
{
  digitalWrite(redPin,HIGH);
  digitalWrite(greenPin,HIGH);
  digitalWrite(bluePin,HIGH);

  noTone(speakerPin);
  playTone(4186,100);
  noTone(speakerPin);
  delay(50);
  digitalWrite(redPin,LOW);
  digitalWrite(greenPin,LOW);
  digitalWrite(bluePin,LOW);

  playTone(4186,100);
}

