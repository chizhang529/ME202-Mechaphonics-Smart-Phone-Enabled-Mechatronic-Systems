/****************************************************************************
 Module
   LogicFlow.c

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
#include "AccelerometerService.h"
#include "LightSensorService.h"
#include "LEDService.h"
#include "BluetoothService.h"
#include "LogicFlow.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)
// light conditions
#define BRIGHT 0
#define DARK 1
// light modes
#define BLINK 0
#define SOLID 1
// Bluetooth connection
#define DISCONNECTED 0
#define CONNECTED 1
// light state
#define OFF 0
#define ON 1
// movement
#define STATIC 0
#define MOVING 1

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/
static void statusPrint(void);
static int getLightMode(void);
static int getLightState(void);

/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
// current logic state
static LogicStatus_t currentStatus;
// control parameters
static int lightCondition;
static int lightMode;
static int isBLEConnected;
static int isLightON;
static int isMoving;

/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitLogicFlow

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any
     other required initialization for this service
 Notes

****************************************************************************/
bool InitLogicFlow ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;
  /********************************************
   in here you write your initialization code
   *******************************************/
  currentStatus = Start;
  // initalize all control vars
  // light conditions
  lightCondition = BRIGHT;
  // light modes
  lightMode = SOLID;
  // Bluetooth connection
  isBLEConnected = DISCONNECTED;
  // light state
  isLightON = OFF;
  // movement
  isMoving = STATIC;
  // initilize logic flow update timer
  ES_Timer_InitTimer(LOGIC_TIMER, HALF_SEC);

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
     PostLogicFlow

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostLogicFlow( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunLogicFlow

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes

****************************************************************************/
ES_Event RunLogicFlow( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  if (ThisEvent.EventType == ES_TIMEOUT) {
    // update control parameters
    lightCondition = getLightCondition(); //LightSensorService
    lightMode = getLightMode();
    isBLEConnected = getConnectionStatus(); //BluetoothService
    isLightON = getLightState();
    isMoving = getMovement(); //AccelerometerService
    statusPrint();
    // restart timer
    ES_Timer_InitTimer(LOGIC_TIMER, HALF_SEC);
  }

  switch (currentStatus) {
    case Start:
      if ((isBLEConnected && (lightMode == BLINK) && (isLightON || (lightCondition == DARK))) || (!isBLEConnected && isMoving)) {
        // LED blink
        if (!isBLEConnected) lightMode = BLINK;
        LEDOn(lightMode);
        currentStatus = LEDBlinking;
      }

      if (isBLEConnected && (lightMode == SOLID) && (isLightON || (lightCondition == DARK))) {
        // LED solid light
        LEDOn(lightMode);
        currentStatus = LEDSolid;
      }

      if ((!isBLEConnected && !isMoving) || (isBLEConnected && (lightCondition == BRIGHT))) {
        // turn off LED
        LEDOff();
        currentStatus = NoLED;
      }
    break;

    case LEDBlinking:
      if (isBLEConnected && (lightMode == SOLID) && (isLightON || (lightCondition == DARK))) {
        // LED solid light
        LEDOn(lightMode);
        currentStatus = LEDSolid;
      }

      if ((!isBLEConnected && !isMoving) || (isBLEConnected && (lightCondition == BRIGHT))) {
        if (!isLightON) {
          // turn off LED
          LEDOff();
          currentStatus = NoLED;
        }
      }
    break;

    case LEDSolid:
      if ((isBLEConnected && (lightMode == BLINK) && (isLightON || (lightCondition == DARK))) || (!isBLEConnected && isMoving)) {
        // LED blink
        if (!isBLEConnected) lightMode = BLINK;
        LEDOn(lightMode);
        currentStatus = LEDBlinking;
      }

      if ((!isBLEConnected && !isMoving) || (isBLEConnected && (lightCondition == BRIGHT))) {
        if (!isLightON) {// turn off LED
          LEDOff();
          currentStatus = NoLED;
        }
      }
    break;

    case NoLED:
      if ((isBLEConnected && (lightMode == BLINK) && (isLightON || (lightCondition == DARK))) || (!isBLEConnected && isMoving)) {
        // LED blink
        if (!isBLEConnected) lightMode = BLINK;
        LEDOn(lightMode);
        currentStatus = LEDBlinking;
      }

      if (isBLEConnected && (lightMode == SOLID) && (isLightON || (lightCondition == DARK))) {
        // LED solid light
        LEDOn(lightMode);
        currentStatus = LEDSolid;
      }
    break;
  }

  return ReturnEvent;
}

void setLightMode(int newLightMode) {
  lightMode = newLightMode;
}

void setLightState(int newLightState) {
  isLightON = newLightState;
}

/***************************************************************************
 private functions
 ***************************************************************************/
 static int getLightMode(void) {
   return lightMode;
 }

 static int getLightState(void) {
   return isLightON;
 }

static void statusPrint(void) {
  Serial.println();
  Serial.println( F("****** Parameters *******") );
  Serial.print( F("Light Condition: ") );  Serial.println(lightCondition);
  Serial.print( F("Light Mode: ")); Serial.println(lightMode);
  Serial.print( F("BLE Connection: ")); Serial.println(isBLEConnected);
  Serial.print( F("Light ON: ")); Serial.println(isLightON);
  Serial.print( F("Movement: ")); Serial.println(isMoving);
  Serial.println();
  Serial.print( F("Logic Status: "));
  if (currentStatus == Start) Serial.println(F("Start"));
  else if (currentStatus == LEDBlinking) Serial.println(F("BlinkingLED"));
  else if (currentStatus == LEDSolid) Serial.println(F("SolidLED"));
  else if (currentStatus == NoLED) Serial.println(F("LEDOff"));
  Serial.println( F("*************************") );
}

/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/
