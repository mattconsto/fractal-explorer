#include "init.h"

void init_clock() {
  /* 8MHz clock, no prescaling (DS, p. 48) */
  CLKPR = (1 << CLKPCE);
  CLKPR = 0;
}

void init_ports() {
	// LED
	DDRB  |=  _BV(PB7);
	PORTB &= ~_BV(PB7);

	// Audio
	DDRC  |=  _BV(PC6); // LCH
	PORTC &= ~_BV(PC6);
	DDRB  |=  _BV(PB5); // RCH
	PORTB &= ~_BV(PB5);

	// Rotary
	DDRE  &= ~(_BV(PE4) | _BV(PE5));
	PORTE |=   _BV(PE4) | _BV(PE5);

	// Middle Button
	DDRE  &= ~_BV(PE7);
	PORTE |=  _BV(PE7);

	// Outer Buttons
	DDRC  &= ~(_BV(PC2) | _BV(PC3) | _BV(PC4) | _BV(PC5)); // NESW
	PORTC |=   _BV(PC2) | _BV(PC3) | _BV(PC4) | _BV(PC5);

	// Interrupts (Timer 0)
	TCCR0A  = _BV(WGM01);            // Clear on compare
	TCCR0B  = _BV(CS01) | _BV(CS00); // F_CPU / 64
	OCR0A   = 125;                   // 1ms period @ 8MHz
	TIMSK0 |= _BV(OCIE0A);           // Enable Interrupts
}

void init_headers() {
  // Do nothing as headers are currently unused
}

void init_rand() {
  // Ensure that the random seed is seeded each time
  static uint32_t EEMEM eeprom_rand_seed;
  uint32_t eeprom_rand_seed_value = eeprom_read_dword(&eeprom_rand_seed);
  // Check if it's unwritten EEPROM (first time). Use something funny in that case.
  if (eeprom_rand_seed_value == 0xffffffUL) eeprom_rand_seed_value = 0xDEADBEEFUL;
  srand(eeprom_rand_seed_value);
  eeprom_write_dword(&eeprom_rand_seed, rand());
}

void init_all() {
  init_clock();
  init_ports();
  init_lcd();
  init_rand();
  sei();
}
