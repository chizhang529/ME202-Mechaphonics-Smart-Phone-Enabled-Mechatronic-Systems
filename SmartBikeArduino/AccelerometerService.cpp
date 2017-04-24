/****************************************************************************
 Module
   AccelerometerService.c

 Revision
   1.0.1

 Description
   This is accelerometer service under the
   Gen2 Events and Services Framework.

 Notes

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "AccelerometerService.h"
#include "LEDService.h"
#include <Wire.h> // Must include Wire library for I2C
#include "SFE_MMA8452Q.h" // Includes the SFE_MMA8452Q library


/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define TEN_MILLI_SEC 1
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)
// movement status
#define STATIC 0
#define MOVING 1

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/


/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
static double lastAccelX;
static double accelDiffSum;
// Begin using the library by creating an instance of the MMA8452Q
//  class. We'll call it "accel". That's what we'll reference from
//  here on out.
MMA8452Q accel;
// Accumulate 10 readings from accelerometer before posting to LEDService
static int readCount;
static int movementStatus;


/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitAccelerometerService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any
     other required initialization for this service
 Notes

****************************************************************************/
bool InitAccelerometerService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
  // Default init of accelerometer. This will set the accelerometer up
  //     with a full-scale range of +/-2g, and an output data rate
  //     of 800 Hz (fastest).
   accel.init();
   // initialization of static vars
  lastAccelX = 0;
  accelDiffSum = 0;
  // no readins for now
  readCount = 0;
  // assume no movement
  movementStatus = STATIC;

  // post the initial transition event
  ThisEvent.EventType = ES_INIT;
  if (ES_PostToService( MyPriority, ThisEvent) == true)
  {
      return true;
  }else
  {
      return false;
  }
}

/****************************************************************************
 Function
     PostAccelerometerService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostAccelerometerService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunAccelerometerService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes

****************************************************************************/
ES_Event RunAccelerometerService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch (ThisEvent.EventType){
    case ES_INIT :
      ES_Timer_InitTimer(ACCELEROMETER_SENSOR_TIMER, TEN_MILLI_SEC);
      // In this service, the accelerometer data is updated every 10mS
      Serial.println(F("The accelerometer starts working..."));
    break;

    case ES_TIMEOUT : // update data
      if (ThisEvent.EventParam == ACCELEROMETER_SENSOR_TIMER){
        // restart the timer for the next update
        ES_Timer_InitTimer(ACCELEROMETER_SENSOR_TIMER, TEN_MILLI_SEC);

        if (accel.available()){ // if the sensor is ready
          // read data
          accel.read();

          if (lastAccelX != 0){ // if the sensor is actually moving
            double accelXDiff = abs(accel.cx - lastAccelX);
            accelDiffSum += accelXDiff;
            readCount++;
          }

          if (readCount == 10){
            // Serial.print(F("Raw data from accelerometer: accel.cx = "));
            // Serial.println(accelDiffSum, 5);

            // set movement status
            if (accelDiffSum < 0.3) {
              movementStatus = STATIC;
            } else {
              movementStatus = MOVING;
            }

            // clamp raw data
            if (accelDiffSum > 10.0){
              accelDiffSum = 10.0;
            }

            // // post event to LEDService to change light intensity
            // ES_Event Event2Post;
            // Event2Post.EventType = ES_CHANGE_INTENSITY;
            // Event2Post.EventParam = (int)(accelDiffSum * 10); // the intensity is mapped to 0-100 for PWM output
            // PostLEDService(Event2Post);

            readCount = 0;
            accelDiffSum = 0.0;
          }
          // update lastAccelX
          lastAccelX = accel.cx;
        }
      }
    break;

    default: break;
  }
  return ReturnEvent;
}

int getMovement(void) {
  return movementStatus;
}

/***************************************************************************
 private functions
 ***************************************************************************/


/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/
