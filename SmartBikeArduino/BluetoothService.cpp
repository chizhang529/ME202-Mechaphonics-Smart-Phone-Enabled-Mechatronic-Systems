/*********************************************************************
 This is a default setup for Adafruit's nRF51822 based Bluefruit LE modules

 MIT license, check LICENSE for more information
 All text above, and the splash screen below must be included in
 any redistribution
*********************************************************************/

#include <Arduino.h>
#include <SPI.h>
#if not defined (_VARIANT_ARDUINO_DUE_X_) && not defined (_VARIANT_ARDUINO_ZERO_)
  #include <SoftwareSerial.h>
#endif

#include <Adafruit_ATParser.h>
#include <Adafruit_BLE.h>
#include <Adafruit_BLEBattery.h>
#include <Adafruit_BLEEddystone.h>
#include <Adafruit_BLEGatt.h>
#include <Adafruit_BLEMIDI.h>
#include <Adafruit_BluefruitLE_SPI.h>
#include <Adafruit_BluefruitLE_UART.h>

#include "Adafruit_BLE.h"
#include "Adafruit_BluefruitLE_SPI.h"
#include "Adafruit_BluefruitLE_UART.h"

#include "BluefruitConfig.h"

/*=========================================================================
    APPLICATION SETTINGS

    FACTORYRESET_ENABLE       Perform a factory reset when running this sketch
   
                              Enabling this will put your Bluefruit LE module
                              in a 'known good' state and clear any config
                              data set in previous sketches or projects, so
                              running this at least once is a good idea.
   
                              When deploying your project, however, you will
                              want to disable factory reset by setting this
                              value to 0.  If you are making changes to your
                              Bluefruit LE device via AT commands, and those
                              changes aren't persisting across resets, this
                              is the reason why.  Factory reset will erase
                              the non-volatile memory where config data is
                              stored, setting it back to factory default
                              values.
       
                              Some sketches that require you to bond to a
                              central device (HID mouse, keyboard, etc.)
                              won't work at all with this feature enabled
                              since the factory reset will clear all of the
                              bonding data stored on the chip, meaning the
                              central device won't be able to reconnect.
    MINIMUM_FIRMWARE_VERSION  Minimum firmware version to have some new features
    MODE_LED_BEHAVIOUR        LED activity, valid options are
                              "DISABLE" or "MODE" or "BLEUART" or
                              "HWUART"  or "SPI"  or "MANUAL"
    -----------------------------------------------------------------------*/
    #define FACTORYRESET_ENABLE         1
    #define MINIMUM_FIRMWARE_VERSION    "0.6.6"
    #define MODE_LED_BEHAVIOUR          "MODE"
/*=========================================================================*/

// Create the bluefruit object, software serial

SoftwareSerial bluefruitSS = SoftwareSerial(BLUEFRUIT_SWUART_TXD_PIN, BLUEFRUIT_SWUART_RXD_PIN);

Adafruit_BluefruitLE_UART ble(bluefruitSS, BLUEFRUIT_UART_MODE_PIN,
                      BLUEFRUIT_UART_CTS_PIN, BLUEFRUIT_UART_RTS_PIN);

/*===================Service Starts HERE============================*/
/****************************************************************************
 Module
   BluetoothService.c

 Revision
   1.0.1

 Description
   This is bluetooth connection service under the
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
#include "BluetoothService.h"
#include "LogicFlow.h"

/*----------------------------- Module Defines ----------------------------*/
// these times assume a 10.24mS/tick timing
#define TEN_MILLI_SEC 1
#define ONE_SEC 98
#define HALF_SEC (ONE_SEC/2)
#define QUARTER_SEC (ONE_SEC/4)
#define ONE_EIGHTH_SEC (ONE_SEC/8)
#define ONE_TENTH_SEC (ONE_SEC/10)
#define TWO_SEC (ONE_SEC*2)
#define FIVE_SEC (ONE_SEC*5)
// connection status
#define DISCONNECTED 0
#define CONNECTED 1
// light modes
#define BLINK 0
#define SOLID 1
// light state
#define OFF 0 // which means AUTO mode
#define ON 1

/*---------------------------- Module Functions ---------------------------*/
/* prototypes for private functions for this service.They should be functions
   relevant to the behaviour of this service
*/


/*---------------------------- Module Variables ---------------------------*/
// with the introduction of Gen2, we need a module level Priority variable
static uint8_t MyPriority;
// add a deferral queue for up to 3 pending deferrals +1 to allow for overhead
static ES_Event DeferralQueue[3+1];
static BLEState_t CurrentBLEState;
static int isConnected;

/*------------------------------ Module Code ------------------------------*/
/****************************************************************************
 Function
     InitBluetoothService

 Parameters
     uint8_t : the priorty of this service

 Returns
     bool, false if error in initialization, true otherwise

 Description
     Saves away the priority, and does any
     other required initialization for this service
 Notes

****************************************************************************/
bool InitBluetoothService ( uint8_t Priority )
{
  ES_Event ThisEvent;
  MyPriority = Priority;

  /**********************************
   initialize hardware on BLE module
   *********************************/
  // set Baud rate for serial output
  Serial.begin(115200);

  /* Initialise the module */
  Serial.print(F("Initialising the Bluefruit LE module..."));

  if ( !ble.begin(VERBOSE_MODE) )
  {
    Serial.println( F("Couldn't find Bluefruit, make sure it's in CoMmanD mode & check wiring?") );
  }
  Serial.println( F("One moment please...") );

  /* Disable command echo from Bluefruit */
  ble.echo(false);

  Serial.println();
  Serial.println(F("Please use Smart Bike app to connect in UART mode..."));
  Serial.println();

  ble.verbose(false);  // debug info is a little annoying after this point!
  // Set module to DATA mode
  ble.setMode(BLUEFRUIT_MODE_DATA);

  CurrentBLEState = Initializing;
  isConnected = DISCONNECTED;
  // check BLE connection status every 0.1s
  ES_Timer_InitTimer(BLE_CHECK_TIMER, ONE_TENTH_SEC);

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
     PostBluetoothService

 Parameters
     EF_Event ThisEvent ,the event to post to the queue

 Returns
     bool false if the Enqueue operation failed, true otherwise

 Description
     Posts an event to this state machine's queue
 Notes

****************************************************************************/
bool PostBluetoothService( ES_Event ThisEvent )
{
  return ES_PostToService( MyPriority, ThisEvent);
}

/****************************************************************************
 Function
    RunBluetoothService

 Parameters
   ES_Event : the event to process

 Returns
   ES_Event, ES_NO_EVENT if no error ES_ERROR otherwise

 Description
   add your description here
 Notes

****************************************************************************/
ES_Event RunBluetoothService( ES_Event ThisEvent )
{
  ES_Event ReturnEvent;
  BLEState_t LastBLEState;
  ReturnEvent.EventType = ES_NO_EVENT; // assume no errors

  if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == BLE_CHECK_TIMER) {
    if (ble.isConnected()) {
      LastBLEState = CurrentBLEState;
      CurrentBLEState = Connected;
      if (LastBLEState == Disconnected) {
        setLightMode(OFF);
        setLightMode(SOLID);
        // report data to Android app via BLE at f = 1Hz
        ES_Timer_InitTimer(ACCELEROMETER_REPORT_TIMER, TWO_SEC);
      }
    } else {
      LastBLEState = CurrentBLEState;
      CurrentBLEState = Disconnected;
      if (LastBLEState == Connected) resetSM();
    }
    ES_Timer_InitTimer(BLE_CHECK_TIMER, ONE_TENTH_SEC);
  }

  switch (CurrentBLEState) {
    case Initializing:
      Serial.println( F("BLE ready to work...") );
    break;

    case Connected:
//      Serial.println(F("**********************************"));
//      Serial.println( F("        BLE Connected!          ") );
//      Serial.println( F("Bluefruit ready in DATA mode!") );
//      Serial.println(F("**********************************"));
      // set current connection status
      isConnected = CONNECTED;

      if (ThisEvent.EventType == ES_BLE_DATA) {
        int cmdIndex = ThisEvent.EventParam;

        if (cmdIndex == 0) {
          setLightState(ON);
        } else if (cmdIndex == 1) {
          setLightState(OFF);
        } else if (cmdIndex == 2) {
          setLightMode(SOLID);
        } else if (cmdIndex == 3) {
          setLightMode(BLINK);
        } else if (cmdIndex == 4) {
          resetSM();
        } else if (cmdIndex == 5) {
          setLightMode(OFF);
          setLightMode(SOLID);
        }
      }

      if (ThisEvent.EventType == ES_TIMEOUT && ThisEvent.EventParam == ACCELEROMETER_REPORT_TIMER) {
        // get accel data, scale by 1000 and report it
        float accel_x = getAccelXData();
        float accel_y = getAccelYData();
        float accel_z = getAccelZData();

        String ax_str = String(accel_x, 2);
        String ay_str = String(accel_y, 2);
        String az_str = String(accel_z, 2);

        String accel_str = ax_str + "!" + ay_str + "!" + az_str + "@";
        char accel_char[BUFSIZE+1];
        accel_str.toCharArray(accel_char, BUFSIZE+1);
        ble.print(accel_char);
        ES_Timer_InitTimer(ACCELEROMETER_REPORT_TIMER, TWO_SEC);
      }
    break;

    case Disconnected:
//      Serial.println();
//      Serial.println(F("**********************************"));
//      Serial.println( F("FAILURE: Bluetooth Disconnected!") );
//      Serial.println(F("**********************************"));
      // set current connection status
      isConnected = DISCONNECTED;
    break;

    default: break;
  }

  return ReturnEvent;
}

int getConnectionStatus(void) {
  return isConnected;
}

bool checkBLEdata(void) {
  if (ble.available()) {
        ble.readline(); // Some data was found, its in the buffer

        // light state: ON->0, AUTO->1
        if (strcmp(ble.buffer, "nn") == 0) {
          ES_Event Event2Post;
          Event2Post.EventType = ES_BLE_DATA;
          Event2Post.EventParam = 0;
          PostBluetoothService(Event2Post);
        }

        if (strcmp(ble.buffer, "aa") == 0) {
          ES_Event Event2Post;
          Event2Post.EventType = ES_BLE_DATA;
          Event2Post.EventParam = 1;
          PostBluetoothService(Event2Post);
        }

        // light mode: SOLID->2, BLINK->3
        if (strcmp(ble.buffer, "ss") == 0) {
          ES_Event Event2Post;
          Event2Post.EventType = ES_BLE_DATA;
          Event2Post.EventParam = 2;
          PostBluetoothService(Event2Post);
        }

        if (strcmp(ble.buffer, "bb") == 0) {
          ES_Event Event2Post;
          Event2Post.EventType = ES_BLE_DATA;
          Event2Post.EventParam = 3;
          PostBluetoothService(Event2Post);
        }

        // disconnect and reset
        if (strcmp(ble.buffer, "r") == 0) {
          ES_Event Event2Post;
          Event2Post.EventType = ES_BLE_DATA;
          Event2Post.EventParam = 4;
          PostBluetoothService(Event2Post);
        }

        // connect
        if (strcmp(ble.buffer, "c") == 0) {
          ES_Event Event2Post;
          Event2Post.EventType = ES_BLE_DATA;
          Event2Post.EventParam = 5;
          PostBluetoothService(Event2Post);
        }
        return true;
  }
  return false;
}

/***************************************************************************
 private functions
 ***************************************************************************/

/*------------------------------- Footnotes -------------------------------*/
/*------------------------------ End of file ------------------------------*/
