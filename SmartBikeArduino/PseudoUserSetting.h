/****************************************************************************

  Header file for PseudoUserSetting
  based on the Gen 2 Events and Services Framework

 ****************************************************************************/

#ifndef PseudoUserSetting_H
#define PseudoUserSetting_H

#include "ES_Configure.h"
#include "./Framework/ES_Types.h"

// Public Function Prototypes
bool InitPseudoUserSetting ( uint8_t Priority );
bool PostPseudoUserSetting( ES_Event ThisEvent );
ES_Event RunPseudoUserSetting( ES_Event ThisEvent );

#endif /* PseudoUserSetting_H */
