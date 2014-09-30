#include <XBee.h>
#include <pt.h>


//pins for control buttons
const int LaneViolationPin=2; 
const int CollisionWarningPin=3;
const int LapPin=4;
const int SystemCheckPin=5;

//my address
const byte TriangleRed=0x09;
const byte TriangleBlue=0X0A;
const byte TriangleGreen=0x0B;
const byte TriangleYellow=0x0C;

//command codes
const byte LANE_VIOLATION=0xA;
const byte COLLISION_WARNING=0xB;
const byte LAP_STARTSTOP=0xC;
const byte HEARTBEAT=0xD;
const byte SYSTEM_CHECK=0xE;


const byte commandBytes[]={
  LANE_VIOLATION,COLLISION_WARNING,LAP_STARTSTOP,SYSTEM_CHECK};
const byte buttonPins[]={
  LaneViolationPin,CollisionWarningPin,LapPin,SystemCheckPin};

int buttonStates[]={
  0,0,0,0};
int lastDebounceTimes[]={
  0,0,0,0};

const byte myVehicle=TriangleRed;

XBee xbee = XBee();

// allocate two bytes for to hold a 10-bit analog reading
uint8_t payload[] = { 
  0, 0, 0, 0 };

// with Series 1 you can use either 16-bit or 64-bit addressing

TxStatusResponse txStatus = TxStatusResponse();

void setup()
{
  Serial.begin(57600);

  //setup button pins
  pinMode(LaneViolationPin,INPUT);
  pinMode(CollisionWarningPin,INPUT);
  pinMode(LapPin,INPUT);
  pinMode(SystemCheckPin,INPUT);
}

boolean packetSent=false;
void sendCommandByte(byte commandByte, byte vehicle)
{
  byte argBytes[]={
    0x0,0x0    };

  byte checkSum=(byte) (commandByte^argBytes[0]^argBytes[1]^0xff);

  byte payload[]={
    commandByte,argBytes[0],argBytes[1],checkSum    };

  Tx16Request tx = Tx16Request(vehicle, payload, sizeof(payload));  

  xbee.send(tx);

  packetSent=true;
}

const long debounceDelay=50;
boolean debouncePin(int pin)
{
  int* lastButtonState=&buttonStates[pin%4];
  int* lastDebounceTime=&lastDebounceTimes[pin%4];
  int buttonState=0;

  // read the state of the switch into a local variable:
  int reading = digitalRead(pin);

  // check to see if you just pressed the button 
  // (i.e. the input went from LOW to HIGH),  and you've waited 
  // long enough since the last press to ignore any noise:  

  // If the switch changed, due to noise or pressing:
  if (reading != *lastButtonState) {
    // reset the debouncing timer
    *lastDebounceTime = millis();
  } 

  if ((millis() - *lastDebounceTime) > debounceDelay) {
    // whatever the reading is at, it's been there for longer
    // than the debounce delay, so take it as the actual current state:

    // if the button state has changed:
    if (reading != buttonState) 
    {
      buttonState = reading;

      // only toggle the LED if the new button state is HIGH
      if (buttonState == HIGH) 
      {
        return true;
      }
    }
  }

  return false;
}
void loop()
{

  for(int i=0;i<4;i++)
  {
    if(debouncePin(buttonPins[i]))
    {
      sendCommandByte(commandBytes[i],myVehicle); 
    }
  }

  // after sending a tx request, we expect a status response
  // wait up to 5 seconds for the status response
  if(packetSent)
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
        } 
        else 
        {
          // the remote XBee did not receive our packet. is it powered on?
        }
      }      
    } 
    else if (xbee.getResponse().isError()) 
    {

    } 
    else 
    {
      // local XBee did not provide a timely TX Status Response.  Radio is not configured properly or connected
    }
  }

  delay(10);
}










