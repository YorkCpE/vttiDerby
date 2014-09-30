#include <XBee.h>
#include <pt.h>


//pins for control buttons
const int LaneViolationPing=2; 
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

/*
This example is for Series 2 XBee
 Sends a ZB TX request with the value of analogRead(pin5) and checks the status response for success
*/

// create the XBee object
XBee xbee = XBee();

uint8_t payload[] = { 0, 0 };

// SH + SL Address of receiving XBee
XBeeAddress64 addr64 = XBeeAddress64(0x0013a200, 0x403e0f30);
ZBTxRequest zbTx = ZBTxRequest(addr64, payload, sizeof(payload));
ZBTxStatusResponse txStatus = ZBTxStatusResponse();


void setup()
{
  Serial.begin(9600);

  xbee.setSerial(Serial);

  //setup button pins
  pinMode(LaneViolationPing,INPUT);
  pinMode(CollisionWarningPin,INPUT);
  pinMode(LapPin,INPUT);
  pinMode(SystemCheckPin,INPUT);
}

void loop()
{
//    xbee.send(zbTx);

  // after sending a tx request, we expect a status response
  // wait up to half second for the status response
  if (xbee.readPacket(500)) 
  {
    // got a response!

    // should be a znet tx status            	
    if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) 
    {
      xbee.getResponse().getZBTxStatusResponse(txStatus);

      // get the delivery status, the fifth byte
      if (txStatus.getDeliveryStatus() == SUCCESS) 
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
    //nss.print("Error reading packet.  Error code: ");  
    //nss.println(xbee.getResponse().getErrorCode());
  } 
  else 
  {
    // local XBee did not provide a timely TX Status Response -- should not happen
    //flashLed(errorLed, 2, 50);
  }

  delay(10);
}






