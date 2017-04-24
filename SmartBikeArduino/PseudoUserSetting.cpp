/****************************************************************************
 Module
   PseudoUserSetting.c

 Revision
   1.0.1

 Description
   This is a service that simulates user setting under the
   Gen2 Events and Services Framework.

 Notes

****************************************************************************/
/*----------------------------- Include Files -----------------------------*/
/* include header files for the framework and this service
*/
#include "ES_Configure.h"
#include "./Framework/ES_Framework.h"
#include "./Framework/ES_DeferRecall.h"
#include "LogicFlow.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)

// light states
#define OFF 0
#define ON 1
// light modes
#define BLINK 0
#define SOLID 1

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/


/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];

/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitPseudoUserSetting

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any
     other required initialization for this service
 Notes

****************************************************************************/
bool InitPseudoUserSetting ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/

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
     PostPseudoUserSetting

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostPseudoUserSetting( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunPseudoUserSetting

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes

****************************************************************************/
ES_Event RunPseudoUserSetting( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  switch (ThisEvent.EventType){
    case ES_INIT :
    break;

    case ES_NEW_KEY:
      if (ThisEvent.EventParam == 'b') {
        setLightMode(BLINK);
      } else if (ThisEvent.EventParam == 's') {
        setLightMode(SOLID);
      } else if (ThisEvent.EventParam == 'n') {
        setLightState(ON);
      } else if (ThisEvent.EventParam == 'f') {
        setLightState(OFF);
      }
    break;

    default: break;
  }
  return ReturnEvent;
}

/***************************************************************************
 private functions
 ***************************************************************************/


/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/
