#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>

#include <stdarg.h>
#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <float.h>
#include <math.h>

#include "ports.h"
#include "lcd.h"
#include "fractal.h"
#include "hsv2rgb2hsv.h"
#include "init.h"

typedef enum {No_Direction, North_Direction, East_Direction, South_Direction, West_Direction} direction;
volatile direction arrow_direction = No_Direction;
volatile int       rotation_delta = 0;
volatile int       cancel_fractal = 0;
volatile long      presses        = 0;

fractal_state state = {
	.width			= 0,       // Pixel width, Auto Filled
	.height			= 0,       // Pixel height, Auto Filled

	.start			= -2.0,    // Range of the complex plane
	.stop				=  2.0,
	.top				= -1.6,
	.bottom			=  1.6,

	.iterations	= 25,      // Number of iterations to calculate
	.threshold	= 2.0,     // Threshold
	.smooth			= 1,       // Fake decimal iteration count to produce smooth shading

	.seedr			= FLT_MAX, // Set to a value other than FLT_MAX to view a julia set
	.seedi			= FLT_MAX, // Ditto

	.selected		= 0,       // Which fractal we are vieing (0-4) auto filled
	.order			= 2,       // Order of exponentiation, negatives allowed and pretty
	.inverse		= 0,       // Do we inverse the base (0-1)
	.orbit			= 0,       // Orbit traps (0-2)
	.region			= 0,       // Region splits (0-2)
};
// 5 6 5
uint16_t colour_hue(float iterations) {
	if(iterations >= 0) {
		hsv input = {iterations / 10.0 * 360.0, 1.0, 1.0};
		rgb color = hsv2rgb(input);
		return ((int) (color.r * 0x1f) << 11) | ((int) (color.g * 0x3f) << 5) | ((int) (color.b * 0x1f) << 0);
	} else {
		return BLACK;
	}
}

void draw_fractal(fractal_state state) {
	char buffer[4];

	fill_rectangle((rectangle) {0, state.width, 8, state.height + 8}, colour_hue(0)); // Skip the first row for header
	char * fractals[] = {"Matt's Mandlebrot Explorer  ", "Matt's Burning Ship Explorer", "Matt's Tricorn Explorer     ", "Matt's Nova Explorer        ", "Matt's Circle Explorer      "};
	display_string_xy(fractals[state.selected], 0, 0);
	display_string_xy(" 0% ", 50 * 6, 0);
	lcd_on();

	long progress_current = 0;
	long progress_final   = ceil(state.height / 8.0) * 8.0 * ceil(state.width / 8.0) * 8.0 - 32;

	for(int i = 1; i <= 7 && !cancel_fractal; i++) {
		for(int y = 0; y < ceil(state.height / 8.0) && !cancel_fractal; y++) {
			for(int x = 0; x < ceil(state.width / 8.0) && !cancel_fractal; x++) {
				sprintf(buffer, "%3d", (int) (((float) (progress_current)/(float) (progress_final))*100.0));
				display_string_xy(buffer, 49 * 6, 0);

				switch(i) {
					case 1:
						fill_rectangle((rectangle) {x*8, x*8+7, (y+1)*8, (y+1)*8+7}, colour_hue(generate_fractal(state, x*8, y*8))); // Skip the first row for header
						progress_current += 1;
						break;
					case 2:
						fill_rectangle((rectangle) {x*8+4, x*8+7, (y+1)*8, (y+1)*8+7}, colour_hue(generate_fractal(state, x*8+4, y*8)));
						progress_current += 1;
						break;
					case 3:
						fill_rectangle((rectangle) {x*8, x*8+3, (y+1)*8+4, (y+1)*8+7}, colour_hue(generate_fractal(state, x*8, y*8+4)));
						fill_rectangle((rectangle) {x*8+4, x*8+7, (y+1)*8+4, (y+1)*8+7}, colour_hue(generate_fractal(state, x*8+4, y*8+4)));
						progress_current += 2;
						break;
					case 4:
						for(int j = 0; j <= 4; j += 4)
							for(int k = 2; k <= 6; k += 2)
								fill_rectangle((rectangle) {x*8+k, x*8+k+1, (y+1)*8+j, (y+1)*8+j+3}, colour_hue(generate_fractal(state, x*8+k, y*8+j)));
						progress_current += 4;
						break;
					case 5:
						for(int j = 2; j <= 6; j += 4)
							for(int k = 0; k <= 6; k += 2)
								fill_rectangle((rectangle) {x*8+k, x*8+k+1, (y+1)*8+j, (y+1)*8+j+1}, colour_hue(generate_fractal(state, x*8+k, y*8+j)));
						progress_current += 8;
						break;
					case 6:
						for(int j = 0; j <= 6; j += 2)
							for(int k = 1; k <= 7; k += 2)
								fill_rectangle((rectangle) {x*8+k, x*8+k, (y+1)*8+j, (y+1)*8+j+1}, colour_hue(generate_fractal(state, x*8+k, y*8+j)));
						progress_current += 16;
						break;
					case 7:
						for(int j = 1; j <= 7; j += 2)
							for(int k = 0; k <= 7; k += 1)
								fill_rectangle((rectangle) {x*8+k, x*8+k, (y+1)*8+j, (y+1)*8+j}, colour_hue(generate_fractal(state, x*8+k, y*8+j)));
						progress_current += 32;
						break;
				}
			}
		}
	}

}

int main(void) {
	init_all();
	ports_set_led_low();

	// Store selected fractal in eeprom, so we have a different one each time
  // static uint32_t EEMEM eeprom_selected;
  // uint32_t eeprom_selected_value = eeprom_read_dword(&eeprom_selected);
  // if (eeprom_selected_value == 0xffffffUL) eeprom_selected_value = 0;
  // srand(eeprom_selected_value);
  // eeprom_write_dword(&eeprom_selected, (eeprom_selected_value + 1) % 5);

	state.width		 = LCDHEIGHT;
	state.height	 = LCDWIDTH - 8;
	// state.selected = eeprom_selected_value % 5;

	display_color(WHITE, BLACK);

	while(1) {
		cancel_fractal = 0;

		draw_fractal(state);

		// Pause until a button is pressed
		while(!cancel_fractal);

		float delta = 0;
		float delta_limit = 0.2;
		float delta_height = state.top   - state.bottom;
		float delta_width  = state.start - state.stop;

		switch(arrow_direction) {
			case North_Direction:
				delta = delta_height * delta_limit;
				state.top		+= delta;
				state.bottom	+= delta;
				break;
			case East_Direction:
				delta = delta_width * delta_limit;
				state.start	-= delta;
				state.stop		-= delta;
				break;
			case South_Direction:
				delta = delta_height * delta_limit;
				state.top		-= delta;
				state.bottom	-= delta;
				break;
			case West_Direction:
				delta = delta_width * delta_limit;
				state.start	+= delta;
				state.stop		+= delta;
				break;
			case No_Direction:
				break;
		}

		delta = 0;
		delta_limit = 0.01;
		state.start  += delta_width  * rotation_delta * delta_limit;
		state.stop   -= delta_width  * rotation_delta * delta_limit;
		state.top    += delta_height * rotation_delta * delta_limit;
		state.bottom -= delta_height * rotation_delta * delta_limit;
		rotation_delta = 0;
	}

	return 0;
}

ISR(TIMER0_COMPA_vect) {
	static int last;
	int wheel = PINE;
	int new = 0;
	if(wheel & _BV(PE4)) new = 3;
	if(wheel & _BV(PE5)) new ^= 1;		   	/* convert gray to binary */
	int diff = last - new;					/* difference last - new  */
	if( diff & 1 ){							/* bit 0 = value (1) */
		last = new;		       				/* store new as next last  */
		rotation_delta += (diff & 2) - 1;	/* bit 1 = direction (+/-) */
	}

	switch((PINC & 0x3c) ^ 0x3c) {
		case 4:		arrow_direction = North_Direction;	break;
		case 8:		arrow_direction = East_Direction;		break;
		case 16:	arrow_direction = South_Direction;	break;
		case 32:	arrow_direction = West_Direction;		break;
		default:	arrow_direction = No_Direction;			break;
	}

	if(rotation_delta || arrow_direction) cancel_fractal = 1;

	if(ports_get_centre()) {
		cancel_fractal = 1;
		presses += 1;
		state.selected = presses % 5;
		state.inverse  = (presses / 5) % 2;
		state.orbit    = (presses / 10) % 2;
		state.region   = (presses / 15) % 2;
		_delay_ms(250);
	}
}
