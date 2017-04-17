#include "ports.h"

inline int ports_bit(int input, int bit) {
  return (input >> bit) & 1;
}

inline void ports_set_high  (volatile uint8_t * port, int pin) {*port |=  (1 << pin);}
inline void ports_set_low   (volatile uint8_t * port, int pin) {*port &= ~(1 << pin);}
inline void ports_set_toggle(volatile uint8_t * port, int pin) {*port ^=  (1 << pin);}

inline void ports_set_set(volatile uint8_t * port, int pin, int boolean) {
  if(boolean) ports_set_high(port, pin); else ports_set_low(port, pin);
}

// LED Pin B7
inline void ports_set_led_high  () {ports_set_high  (&PINB, PB7);}
inline void ports_set_led_low   () {ports_set_low   (&PINB, PB7);}
inline void ports_set_led_toggle() {ports_set_toggle(&PINB, PB7);}

inline void ports_set_led_set(int boolean) {
  if(boolean) ports_set_high(&PINB, PB7); else ports_set_low (&PINB, PB7);
}

// LCH Pin C6
inline void ports_set_lch_high  () {ports_set_high  (&PINC, PC6);}
inline void ports_set_lch_low   () {ports_set_low   (&PINC, PC6);}
inline void ports_set_lch_toggle() {ports_set_toggle(&PINC, PC6);}

inline void ports_set_lch_set(int boolean){
    if(boolean) ports_set_high(&PINC, PC6); else ports_set_low(&PINC, PC6);
}

// RCH Pin B5
inline void ports_set_rch_high  () {ports_set_high  (&PINB, PB5);}
inline void ports_set_rch_low   () {ports_set_low   (&PINB, PB5);}
inline void ports_set_rch_toggle() {ports_set_toggle(&PINB, PB5);}

inline void ports_set_rch_set(int boolean) {
      if(boolean) ports_set_high(&PINB, PB5); else ports_set_low(&PINB, PB5);
}

inline int  ports_get_centre    () {return !ports_bit(PINE, PE7);}
inline int  ports_get_north     () {return !ports_bit(PINC, PC2);}
inline int  ports_get_east      () {return !ports_bit(PINC, PC3);}
inline int  ports_get_south     () {return !ports_bit(PINC, PC4);}
inline int  ports_get_west      () {return !ports_bit(PINC, PC5);}
inline int  ports_get_rotary    () {return (PINE >> 4) & 3;}

inline int  ports_get_header_d  () {return PIND;}
inline int  ports_get_header_f  () {return PINF;}
