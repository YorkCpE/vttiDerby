#include <XBee.h>

#include <XBee.h>

/*
This example is for Series 2 XBee
 Receives a ZB RX packet and sets a PWM value based on packet data.
 Error led is flashed if an unexpected packet is received
 */

XBee xbee = XBee();
XBeeResponse response = XBeeResponse();
ZBRxResponse rx = ZBRxResponse();
ModemStatusResponse msr = ModemStatusResponse();


void setup()
{
  Serial.begin(9600);
  xbee.begin(Serial);

}


void loop()
{
  xbee.readPacket();

  if (xbee.getResponse().isAvailable()) 
  {
    // got something

    if (xbee.getResponse().getApiId() == ZB_RX_RESPONSE) 
    {
      // got a zb rx packet

      // now fill our zb rx class
      xbee.getResponse().getZBRxResponse(rx);

      if (rx.getOption() == ZB_PACKET_ACKNOWLEDGED) 
      {
        // the sender got an ACK
      } 
      else 
      {
        // we got it (obviously) but sender didn't get an ACK
      }
    } 
    else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) 
    {
      xbee.getResponse().getModemStatusResponse(msr);
      // the local XBee sends this response on certain events, like association/dissociation

      if (msr.getStatus() == ASSOCIATED) 
      {
        // yay this is great.  flash led
      } 
      else if (msr.getStatus() == DISASSOCIATED) 
      {
        // this is awful.. flash led to show our discontent
      } 
      else 
      {
        // another status
      }
    } 
    else 
    {
      // not something we were expecting
    }
  } 
  else if (xbee.getResponse().isError()) {
    //nss.print("Error reading packet.  Error code: ");  
    //nss.println(xbee.getResponse().getErrorCode());
  }

}

