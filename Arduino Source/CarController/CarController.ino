#include <SoftwareSerial.h>

#include <XBee.h>

//LCD screen includes
#include <LiquidCrystal.h>
#include <LCDKeypad.h>
LiquidCrystal lcd(8, 13, 9, 4, 5, 6, 7);

//vars for LCD screen
int adc_key_val[5] ={
  50, 200, 400, 600, 800 };
int NUM_KEYS = 5;
int adc_key_in;
int key=-1;
int oldkey=-1;

char msgs[5][16] = {
  "Right Key OK ",
  "Up Key OK    ",               
  "Down Key OK  ",
  "Left Key OK  ",
  "Select Key OK" };

//recognized buttons
const int NO_BUTTON=-1;
const int LEFT_BUTTON=0;
const int RIGHT_BUTTON=1;
const int UP_BUTTON=2;
const int DOWN_BUTTON=3;
const int SELECT_BUTTON=4;

const int buttons[]={
  RIGHT_BUTTON,UP_BUTTON,DOWN_BUTTON,LEFT_BUTTON,SELECT_BUTTON};



//my address
const byte TriangleRed=0x09;
const byte TriangleBlue=0X0A;
const byte TriangleGreen=0x0B;
const byte TriangleYellow=0x0C;
const byte TrianglePurple=0x0D;
const byte TriangleBlack=0x0E;
const byte TriangleOrange=0x0F;

const byte ManagerAddress=0x01;

//assign the vehicle I am controlling
const byte myVehicle=0x2f;
String myCar="Orange Circle";

//command codes
const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;
const byte HEARTBEAT=0xD;
const byte SYSTEM_CHECK=0xE;
const byte RACE_START=0x1A;
const byte RACE_STOP=0x1B;
const byte NO_OP=0x1C;

XBee xbee = XBee();

// allocate two bytes for to hold a 10-bit analog reading
uint8_t payload[] = { 
  0, 0, 0, 0 };

// with Series 1 you can use either 16-bit or 64-bit addressing

TxStatusResponse txStatus = TxStatusResponse();

SoftwareSerial mySerial(2,3);
void setupLCD()
{
  lcd.begin(16, 2);
  
  lcd.clear();
  lcd.setCursor(0,0); 
  
  lcd.print(myCar.c_str()); 
}
void setup()
{
  Serial.begin(9600);
  mySerial.begin(9600);
  
  
  xbee.setSerial(mySerial);
  //xbee.setSerial(Serial);

  setupLCD();
}

const boolean DEBUG=true;

byte lastCommand=0x0;
boolean packetSent=false;
void sendCommandByte(byte commandByte, byte vehicle)
{
  if(commandByte==NO_OP)
  {
    return; 
  }

  if(DEBUG)
    Serial.println("Sending Command ");

  if(DEBUG){
    if(commandByte==LANE_VIOLATION)
    {
      Serial.println("Lane Violation"); 
    }
    else if(commandByte==COLLISION_WARNING)
    {
      Serial.println("Collision Warning");
    }
    else if(commandByte==LAP_STARTSTOP)
    {
      Serial.println("Lap Start/Stop");
    }
    else if(commandByte==HEARTBEAT)
    {
      Serial.println("Heart Beat");
    }
    else if(commandByte==0xE)
    {
      Serial.println("System Check");
    }
  }

  byte argBytes[]={
    0x0,0x0              };

  byte checkSum=(byte) (commandByte^argBytes[0]^argBytes[1]^0xff);

  byte payload[]={
    commandByte,argBytes[0],argBytes[1],checkSum              };

  Tx16Request toArduino = Tx16Request(myVehicle, payload, sizeof(payload));  

  lastCommand=commandByte;

  xbee.send(toArduino);
  
  packetSent=true;
  
}

// Convert ADC value to key number
int get_key(unsigned int input)
{
  int k;
  for (k = 0; k < NUM_KEYS; k++)
  {
    if (input < adc_key_val[k])
    {
      return k;
    }
  }   
  if (k >= NUM_KEYS)k = -1;  // No valid key pressed
  return k;
}

int getButtonPress()
{
  adc_key_in = analogRead(0);    // read the value from the sensor 
  key = get_key(adc_key_in);  // convert into key press
  if (key != oldkey)   // if keypress is detected
  {
    delay(50);  // wait for debounce time
    adc_key_in = analogRead(0);    // read the value from the sensor 
    key = get_key(adc_key_in);    // convert into key press
    if (key != oldkey)    
    {   
      /*if(DEBUG)
       {
       lcd.setCursor(0, 1);
       }*/

      oldkey = key;
      if (key >=0)
      {
        /*if(DEBUG)
         {
         lcd.print(msgs[key]);              
         }*/

        return buttons[key];
      }
    }
  }

  return NO_BUTTON;
}

void loop()
{

  int buttonPressed=getButtonPress();

  if(buttonPressed>=0 && buttonPressed<NUM_KEYS)
  {
    lcd.setCursor(0, 1);
    switch(buttonPressed)
    {
    case LEFT_BUTTON:
      lcd.print(" Lane Violation ");
      sendCommandByte(LANE_VIOLATION,myVehicle);
      break;
    case RIGHT_BUTTON:
      lcd.print("    New Lap!    ");
      sendCommandByte(LAP_STARTSTOP,myVehicle);
      break;
    case UP_BUTTON:
      lcd.print("    Collision!   ");
      sendCommandByte(COLLISION_WARNING,myVehicle);
      break;
    case DOWN_BUTTON:
      //lcd.print("Down Button     ");
      break;
    case SELECT_BUTTON:
      lcd.print("   System Check!  "); 
      sendCommandByte(SYSTEM_CHECK,myVehicle);
      break;
    default:
      break;
    }
  }

  // after sending a tx request, we expect a status response
  // wait up to 5 seconds for the status response
  if(packetSent && !DEBUG)
  {
    packetSent=false;
    if (xbee.readPacket(5000)) 
    {
      // got a response!
      // should be a znet tx status            	
      if (xbee.getResponse().getApiId() == TX_STATUS_RESPONSE) 
      {
        xbee.getResponse().getZBTxStatusResponse(txStatus);

        // get the delivery status, the fifth byte
        if (txStatus.getStatus() == SUCCESS) 
        {
          // success.  time to celebrate
          if(DEBUG)
          {
            Serial.println("Success!");
          }
        } 
        else 
        {
          // the remote XBee did not receive our packet. is it powered on?
          if(DEBUG)
          {
            Serial.println("Did not receive!");
          }
        }
      }      
    } 
    else if (xbee.getResponse().isError()) 
    {
      Serial.println("Error!");
    } 
    else 
    {
      // local XBee did not provide a timely TX Status Response.  Radio is not configured properly or connected
      Serial.println("Bad Error!");
    }
  }

  delay(10);
}















