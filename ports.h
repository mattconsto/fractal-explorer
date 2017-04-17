#pragma once

#include <avr/io.h>

int  ports_bit(int input, int bit);

void ports_set_high  (volatile uint8_t * port, int pin);
void ports_set_low   (volatile uint8_t * port, int pin);
void ports_set_toggle(volatile uint8_t * port, int pin);
void ports_set_set   (volatile uint8_t * port, int pin, int boolean);

void ports_set_led_high  ();
void ports_set_led_low   ();
void ports_set_led_toggle();
void ports_set_led_set   (int boolean);

void ports_set_lch_high  ();
void ports_set_lch_low   ();
void ports_set_lch_toggle();
void ports_set_lch_set   (int boolean);

void ports_set_rch_high  ();
void ports_set_rch_low   ();
void ports_set_rch_toggle();
void ports_set_rch_set   (int boolean);

int  ports_get_centre    ();
int  ports_get_north     ();
int  ports_get_east      ();
int  ports_get_south     ();
int  ports_get_west      ();
int  ports_get_rotary    ();

int  ports_get_header_d  ();
int  ports_get_header_f  ();
