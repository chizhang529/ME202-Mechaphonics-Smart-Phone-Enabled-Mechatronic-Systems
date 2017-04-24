/****************************************************************************

  Header file for LightSensorService
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef LightSensorService_H
#define LightSensorService_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// Public Function Prototypes

bool InitLightSensorService ( uint8_t Priority );
bool PostLightSensorService( ES_Event ThisEvent );
ES_Event RunLightSensorService( ES_Event ThisEvent );
int getLightCondition(void);

#endif /* LightSensorService_H */
