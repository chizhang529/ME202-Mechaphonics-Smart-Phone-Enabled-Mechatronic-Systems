/****************************************************************************

  Header file for AccelerometerService
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef AccelerometerService_H
#define AccelerometerService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// Public Function Prototypes

bool InitAccelerometerService( uint8_t Priority );
bool PostAccelerometerService( ES_Event ThisEvent );
ES_Event RunAccelerometerService( ES_Event ThisEvent );
int getMovement(void);

#endif /* AccelerometerService_H */
