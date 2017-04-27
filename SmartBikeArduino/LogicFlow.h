/****************************************************************************

  Header file for LogicFlow
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef LogicFlow_H
#define LogicFlow_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// State definitions for use with the query function
typedef enum { Start, LEDBlinking, LEDSolid, NoLED } LogicStatus_t ;

// Public Function Prototypes
bool InitLogicFlow ( uint8_t Priority );
bool PostLogicFlow ( ES_Event ThisEvent );
ES_Event RunLogicFlow ( ES_Event ThisEvent );
void setLightMode(int newLightMode);
void setLightState(int newLightState);
void resetSM(void);

#endif /* LogicFlow_H */
