/****************************************************************************

  Header file for Bluetooth Service
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef BluetoothService_H
#define BluetoothService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// State definitions for use with the query function
typedef enum { Initializing, Connected, Disconnected } BLEState_t;

// Public Function Prototypes
bool InitBluetoothService( uint8_t Priority );
bool PostBluetoothService( ES_Event ThisEvent );
ES_Event RunBluetoothService( ES_Event ThisEvent );
int getConnectionStatus(void);
bool checkBLEdata(void);

#endif /* BluetoothService_H */
