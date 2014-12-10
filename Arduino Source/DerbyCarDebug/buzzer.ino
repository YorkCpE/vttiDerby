
int buzzerPin;


void setupBuzzer(int pin)
{  
  buzzerPin=pin;
  //setup Buzzer, set the GROUND_PIN as a ground
  pinMode(buzzerPin,OUTPUT);


}

void playTone(int freq,int duration)
{ 
  //if there's no duration, assume the note will play until stopped
  if(duration==0)
  {
    tone(buzzerPin,freq);
  }

  //play the note for a certain duration
  else 
  {
    tone(buzzerPin,freq);
    delay(duration);
    noTone(buzzerPin);
  }
}

//stop play any tones
void StopTone()
{
  noTone(buzzerPin); 
}

