/****************************************************************************
 Module
   LightSensorService.c

 Revision
   1.0.1

 Description
   This is light sensor service under the
   Gen2 Events and Services Framework.

 Notes

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "LightSensorService.h"
#include "LEDService.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)

// light parameters
#define BRIGHT 0
#define DARK 1
#define LIGHT_THRESHOLD 300

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/


/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
// define lightValue to store the value of light sensor (0-1023)
static int lightValue;
// define analog pi for light sensor
static int lightSensorPin;
// current light condition
static int lightCondition;

/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitLightSensorService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any
     other required initialization for this service
 Notes

****************************************************************************/
bool InitLightSensorService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
 // set A0 on Nano as light sensor pin
 lightSensorPin = A0;
 // clear light value
 lightValue = 0;
 // default: it is not dark
 lightCondition = BRIGHT;

  // post the initial transition event
  ThisEvent.EventType = ES_INIT;
  if (ES_PostToService( MyPriority, ThisEvent) == true) {
    return true;
  } else {
    return false;
  }
}

/****************************************************************************
 Function
     PostLightSensorService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostLightSensorService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunLightSensorService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes

****************************************************************************/
ES_Event RunLightSensorService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch (ThisEvent.EventType){
    case ES_INIT :
	    ES_Timer_InitTimer(LIGHT_SENSOR_TIMER, ONE_EIGHTH_SEC);
    break;

    case ES_TIMEOUT :
      ES_Timer_InitTimer(LIGHT_SENSOR_TIMER, ONE_EIGHTH_SEC);
      // check light condition
	    lightValue = analogRead(lightSensorPin);

      if (lightValue > LIGHT_THRESHOLD){ // light is LOW (dark)
        lightCondition = DARK;
	    } else { // light is HIGH (bright)
        lightCondition = BRIGHT;
	    }
      
    break;

    default: break;
  }
  return ReturnEvent;
}

int getLightCondition(void) {
  return lightCondition;
}

/***************************************************************************
 private functions
 ***************************************************************************/


/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/
