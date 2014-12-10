/*
 *  Sebastian A. Molina-Cadena
 *  Project 4 Game 
 */
 
float xVoltage;
float yVoltage;
float zVoltage;
float xAcceleration;
float yAcceleration;
float zAcceleration;

float Vf;
float sensitivity = 0;


// the loop routine runs over and over again forever:
void speedLoop(float s) {
    sensitivity=s;
    // read the input on analog pin 0 and converting to voltage:
    xVoltage = AnalogtoVoltage(analogRead(0));
    //converting to g values
    xAcceleration = ConvertVoltagetoAcceleration(xVoltage);
    // read the input on analog pin 1 and converting to voltage:
    yVoltage = AnalogtoVoltage(analogRead(1));
    //converting to g values
    yAcceleration = ConvertVoltagetoAcceleration(yVoltage);
    // read the input on analog pin 2 and converting to voltage:
    zVoltage = AnalogtoVoltage(analogRead(2));
    //converting to g values
    zAcceleration = ConvertVoltagetoAcceleration(zVoltage);
    
    //Calculating the speed magnitude
    Vf = abs(getMagnitude(xAcceleration, yAcceleration, 0))*9.8*10;
   
    Serial.print("  Speed: ");
    Serial.print(Vf*sensitivity);
    Serial.print("\n");
    
    
    //Turning on leds according to the total magnitude
    ledsOn(Vf*sensitivity);
}

// This method return a float value that contains the voltage between (0 - 3.3V) from A0
float AnalogtoVoltage(int analogReading) {
  return analogReading * (3.3 / 1024.0);
}

// This method convert the voltage value to acceleration in g terms
float ConvertVoltagetoAcceleration(float voltage) {
    return (voltage - 1.65)/0.8;
}

void ledsOn(float magnitude) {
  if(magnitude < 60)
  {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,0);
  }
  else if(magnitude >= 60 && magnitude < 70) {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,42);
       
  }
  else if(magnitude >= 70 && magnitude < 80) {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,84);
   
  }
  else if(magnitude >= 80 && magnitude < 90) {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,126);
 
  }
  else if(magnitude >= 90 && magnitude < 100) {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,168);
  
  }
  else if(magnitude >= 100 && magnitude < 110) {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,210);

  }
  else if(magnitude >= 110) {
    digitalWrite(speedRedPin,LOW);
    digitalWrite(speedBluePin,LOW);
    analogWrite(speedGreenPin,252);
   
  }
}



