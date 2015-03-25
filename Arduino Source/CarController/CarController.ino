#include <SoftwareSerial.h>

#include <XBee.h>

//pins for control buttons
const int LaneViolationPin=4; 
const int CollisionWarningPin=5;
const int LapPin=6;
const int SystemCheckPin=7;

//my address
const byte TriangleRed=0x09;
const byte TriangleBlue=0X0A;
const byte TriangleGreen=0x0B;
const byte TriangleYellow=0x0C;
const byte ManagerAddress=0x01;

const byte myVehicle=TriangleRed;

//command codes
const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;
const byte HEARTBEAT=0xD;
const byte SYSTEM_CHECK=0xE;


const byte commandBytes[]={
  LANE_VIOLATION,COLLISION_WARNING,LAP_STARTSTOP,SYSTEM_CHECK};
const int buttonPins[]={
  LaneViolationPin,CollisionWarningPin,LapPin,SystemCheckPin};

XBee xbee = XBee();

// allocate two bytes for to hold a 10-bit analog reading
uint8_t payload[] = { 
  0, 0, 0, 0 };

// with Series 1 you can use either 16-bit or 64-bit addressing

TxStatusResponse txStatus = TxStatusResponse();

void setup()
{
  Serial.begin(9600);

  //xbee.setSerial(mySerial);
  xbee.setSerial(Serial);

  //setup button pins
  pinMode(LaneViolationPin,INPUT);
  pinMode(CollisionWarningPin,INPUT);
  pinMode(LapPin,INPUT);
  pinMode(SystemCheckPin,INPUT);
}

const boolean DEBUG=false;

byte lastCommand=0x0;
boolean packetSent=false;
void sendCommandByte(byte commandByte, byte vehicle)
{
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
  }}
  
  byte argBytes[]={
    0x0,0x0    };

  byte checkSum=(byte) (commandByte^argBytes[0]^argBytes[1]^0xff);

  byte payload[]={
    commandByte,argBytes[0],argBytes[1],checkSum    };

  Tx16Request toArduino = Tx16Request(myVehicle, payload, sizeof(payload));  

  lastCommand=commandByte;
  
  if(!DEBUG)
  {
    xbee.send(toArduino);
  }
}

const int debounceDelay=50;
static int currentButtonState[]={0,0,0,0};
static int lastButtonState[]={0,0,0,0}; 
static int lastDebounceTime[]={0,0,0,0};

boolean buttonPressed(int pin)
{ 
  boolean returnState=false;
  
  int reading=digitalRead(pin);

  if(reading!= lastButtonState[pin%4])
  {
    lastDebounceTime[pin%4]=millis(); 
  }
  
  if ((millis() - lastDebounceTime[pin%4]) > debounceDelay) {
    // whatever the reading is at, it's been there for longer
    // than the debounce delay, so take it as the actual current state:

    // if the button state has changed:
    if (reading != currentButtonState[pin%4]) {
      currentButtonState[pin%4] = reading;

      // only toggle the LED if the new button state is HIGH
      if (currentButtonState[pin%4] == HIGH) 
      {
        if(DEBUG)
        {
           Serial.print("Button ");
           Serial.print(pin);
           Serial.println(" pressed.");
           Serial.flush();
        }
        
        returnState=true;
      }
      else
      {
         returnState=false;
      }
    }
  }
  
  lastButtonState[pin%4]=reading;
  
  return returnState;
}

long last = millis();

void loop()
{
  for(int i=0;i<4;i++)
  {
    if(buttonPressed(buttonPins[i]))
    {
      sendCommandByte(commandBytes[i],myVehicle); 
      packetSent=true;
      last=millis();
    }
  }
  
  /*
  static long last = millis();
  if(millis()-last>5000)
  {
     sendCommandByte(SYSTEM_CHECK,myVehicle); 
     packetSent=true;
     last=millis();
  }*/
 
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
            
            /*byte scoreboardPayload[]={myVehicle,lastCommand,0x0,(myVehicle^lastCommand^0x0^0xff)};            
            Tx16Request toScoreboard = Tx16Request(0x05, scoreboardPayload, sizeof(scoreboardPayload));  
            xbee.send(toScoreboard);
            packetSent=true;*/
          }
        } 
        else 
        {
          // the remote XBee did not receive our packet. is it powered on?
          if(DEBUG)
            Serial.println("Did not receive!");
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










