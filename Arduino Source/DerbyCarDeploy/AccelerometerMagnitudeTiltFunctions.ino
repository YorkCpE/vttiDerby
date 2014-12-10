/*
  Accelerometer MagnitudeTiltFunctions
  Provides function for finding the magnitude of the total acceleration 
  and tilt in each axis using the acceleration in each axis as inputs.

 */

float RAD2DEGREES=57.2985;

// Inputs: Acceleration in the X, Y, and Z directions in g's
// Returns magnitude of total acceleration
float getMagnitude(float X, float Y, float Z)
{
  float mag=sqrt(sq(X)+sq(Y)+sq(Z));
  return mag;
}

// Inputs: X, Y and Z acceleration in g's
// Returns X rotation in degrees measured from horizontal
float getXRotation(float X, float Y, float Z)
{ 
  float tilt = atan2(X, (sqrt(sq(Y) + sq(Z)) ) ) * RAD2DEGREES;
  return tilt;
}

// Inputs: X, Y and Z acceleration in g's
// Returns Y rotation in degrees measured from horizontal
float getYRotation(float X, float Y, float Z)
{  
  float tilt = atan2(Y, (sqrt(sq(X) + sq(Z)) ) ) * RAD2DEGREES; 
  return tilt;
}

// Inputs: X,  Y, and Z acceleration in g's
// Returns Z rotation in degrees measured from vertical
float getZRotation(float X, float Y, float Z)
{
  float tilt=atan2((sqrt(sq(X) + sq(Y)) ),Z)*RAD2DEGREES;
  return tilt;  
}
