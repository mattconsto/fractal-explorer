#include "fractal.h"

float generate_fractal(fractal_state state, int x, int y) {
	complex past = complex_new(
		state.start + (state.stop   - state.start) * x / state.width,
		state.top   + (state.bottom - state.top  ) * y / state.height
	);

	/* Using FLT_MAX to indicate that there is no seed */
	complex base = state.seedr == FLT_MAX ? past : complex_new(state.seedr, state.seedi);

	if(state.inverse) {
		base = complex_inv(base);
		past = complex_inv(past);
	}

	const float t = state.threshold * state.threshold;

	/* For orbit traps */
	int   distancelength = state.region == 1 ? 5 : (state.region == 2 ? 4 : 1);
	float distance[5];

	for(int i = 0; i < distancelength; i++) distance[i] = FLT_MAX;

	for(int i = 1; i < state.iterations; i++) {
		/* Square complex1, then add complex2 */
		complex current;

		switch(state.selected) {
			default:
			case 0: /* Mandlebrot */
				current = complex_add(complex_pow(past, state.order), base);
				break;
			case 1: /* Burning Ship */
				current = complex_add(complex_pow(complex_abs(past), state.order), base);
				break;
			case 2: /* Tricorn */
				current = complex_add(complex_pow(complex_new(past.r, 0 - past.i), state.order), base);
				break;
			case 3: /* Nova */
				current = complex_add(
					complex_div(
						complex_mul(
							complex_new(1, 0),
							complex_sub(complex_pow(past, state.order), complex_new(1, 0))
						),
						complex_mul(complex_new(state.order, 0), complex_pow(past, state.order - 1))
					),
					base
				);
				break;
			case 4: /* Circle */
				current = complex_pow(past, state.order);
				break;
		}

		/* Modulus */
		float modulus = complex_mod2(current);

		if(state.orbit) {
			if(state.orbit == 1) {
				if(current.r*current.r < distance[i % distancelength]) distance[i % distancelength] = current.r*current.r;
				if(current.i*current.i < distance[i % distancelength]) distance[i % distancelength] = current.i*current.i;
			} else {
				if(complex_mod2(current) < distance[i % distancelength])
					distance[i % distancelength] = complex_mod2(current);
			}

			int pointer;
			if(state.region == 1) {
				pointer = (current.i > 0 ? 1 : 0) + (current.r > 0 ? 2 : 0);
			} else {
				pointer = i % distancelength;
			}

			if(i == state.iterations - 1) {
				if(distance[pointer] < 1) {
					return sqrt(distance[pointer]) * state.iterations;
				} else {
					return 0;
				}
				break;
			}
		} else {
			if(modulus > t) {
				if(state.smooth) {
					float k = (t - complex_mod2(past)) / fabs(complex_mod2(past) - complex_mod2(current));
					if(k < 0) k = 0.0;
					return i + k - 1;
				} else {
					return i - 1;
				}
			} else if(i == state.iterations - 1) {
				/* Fallback if we don't reach anything. We need to fill in everything otherwise we
				   start to see weird patterns as variables don't really have a default here */
				return -1;
			}
		}

		/* Copy the new to the old, we will need it later, but by another name */
		past = current;
	}

	return 0;
}
