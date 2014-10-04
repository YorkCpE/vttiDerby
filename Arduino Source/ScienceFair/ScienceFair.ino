#include <SoftwareSerial.h>

#include <XBee.h>

//pins for control buttons
const int LaneViolationPin=0; 
const int CollisionWarningPin=1;
const int LapPin=2;
const int SystemCheckPin=3;

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

const byte myVehicle=TriangleBlue;

XBee xbee = XBee();

// allocate two bytes for to hold a 10-bit analog reading
uint8_t payload[] = { 
  0, 0, 0, 0 };

// with Series 1 you can use either 16-bit or 64-bit addressing

TxStatusResponse txStatus = TxStatusResponse();

SoftwareSerial mySerial(2,3);

void setup()
{
  Serial.begin(9600);
  mySerial.begin(9600);

  //xbee.setSerial(mySerial);
  xbee.setSerial(Serial);

  //setup button pins
  pinMode(LaneViolationPin,INPUT);
  pinMode(CollisionWarningPin,INPUT);
  pinMode(LapPin,INPUT);
  pinMode(SystemCheckPin,INPUT);
}

const boolean DEBUG=false;

boolean packetSent=false;
void sendCommandByte(byte commandByte, byte vehicle)
{
  if(DEBUG)
    Serial.println("Sending Command ");
  
  if(DEBUG){
  if(commandByte==0xA)
  {
     Serial.println("Lane Violation"); 
  }
  else if(commandByte==0xB)
  {
    Serial.println("Collision Warning");
  }
  else if(commandByte==0xC)
  {
    Serial.println("Lap Start/Stop");
  }
  else if(commandByte==0xD)
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

  Tx16Request tx = Tx16Request(myVehicle, payload, sizeof(payload));  

  xbee.send(tx);
}


long last = millis();
void loop()
{
  /*for(int i=0;i<4;i++)
  {
    if(analogRead(i)>1000)
    {
      sendCommandByte(commandBytes[i],myVehicle); 
      packetSent=true;
    }
  }*/
  
  if(millis()-last>5000)
  {
     sendCommandByte(SYSTEM_CHECK,myVehicle); 
     packetSent=true;
     last=millis();
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
          Serial.println("Success!");
        } 
        else 
        {
          // the remote XBee did not receive our packet. is it powered on?
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










