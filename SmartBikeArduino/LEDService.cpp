/****************************************************************************
 Module
   LEDService.c

 Revision
   1.0.1

 Description
   This is LED Service under the
   Gen2 Events and Services Framework.

 Notes

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "LEDService.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)
// light modes
#define BLINK 0
#define SOLID 1
// LED pin number
#define BLED 3

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/
static void LEDInit(void);

/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
// intensity (0 - 255, but 0 - 100 makes light brightness vary a lot)
static int intensity;
// Current Light Mode
static LightMode_t CurrentLightMode;
/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitLEDService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any
     other required initialization for this service
 Notes

****************************************************************************/
bool InitLEDService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
  // initialize LED drive for testing/debug output
  LEDInit();
  // Set current light mode to off
  CurrentLightMode = Off;
  // Set intensity to 255 as default value (highest)
  intensity = 255;

  // post the initial transition event
  ThisEvent.EventType = ES_INIT;
  if (ES_PostToService( MyPriority, ThisEvent) == true)
  {
      return true;
  } else {
      return false;
  }
}

/****************************************************************************
 Function
     PostLEDService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostLEDService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunLEDService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes

****************************************************************************/
ES_Event RunLEDService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch (CurrentLightMode) {
    case Solid:
      // PWM output with intensity 255
      analogWrite(BLED, intensity);
    break;

    case BlinkON:
      if (ThisEvent.EventType == ES_TIMEOUT) {
        analogWrite(BLED, 0);
        CurrentLightMode = BlinkOFF;
        ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
      }
    break;

    case BlinkOFF:
      if (ThisEvent.EventType == ES_TIMEOUT) {
        analogWrite(BLED, intensity);
        CurrentLightMode = BlinkON;
        ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
      }
    break;

    case Off:
      analogWrite(BLED, 0);
    break;
  }

  return ReturnEvent;
}

void LEDOn(int lightMode)
{
  if (lightMode == SOLID) {
    CurrentLightMode = Solid;
    // PWM output with intensity 255
  	analogWrite(BLED, intensity);
  } else {
    // PWM output with intensity 255
  	analogWrite(BLED, intensity);
    CurrentLightMode = BlinkON;
    ES_Timer_InitTimer(BLINK_TIMER, ONE_EIGHTH_SEC);
  }
}

void LEDOff(void)
{
  CurrentLightMode = Off;
	// PWM output to pin 3, with intensity as 0
	analogWrite(BLED, 0);
}

/***************************************************************************
 private functions
 ***************************************************************************/
static void LEDInit(void)
{
	// Set pin 3 as digital output
	pinMode(BLED, OUTPUT);
	// Turn off all of the LEDs
	LEDOff();
}
/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/
