#pragma once

#include <avr/interrupt.h>
#include <avr/eeprom.h>
#include <stdlib.h>

#include "lcd.h"

void init_clock();
void init_ports();
void init_headers();
void init_rand();
void init_all();
