#include "complex.h"

complex complex_new(float r, float i) {
	return (complex) {r, i};
}

complex complex_add(complex a, complex b) {
	return complex_new(a.r + b.r, a.i + b.i);
}

complex complex_sub(complex a, complex b) {
	return complex_new(a.r - b.r, a.i - b.i);
}

complex complex_abs(complex c) {
	return complex_new(fabs(c.r), fabs(c.i));
}

complex complex_pow(complex c, int n) {
	switch (n) {
			case 0:  return complex_new(1, 0);

			case 1:  return complex_new(c.r, c.i);
			case 2:  return complex_new(c.r*c.r - c.i*c.i, 2*c.r*c.i);
			case 3:  return complex_new(c.r*c.r*c.r - 3*c.i*c.i*c.r, 3*c.i*c.r*c.r - c.i*c.i*c.i);
			case 5:  return complex_new(c.r*c.r*c.r*c.r*c.r - 10*c.i*c.i*c.r*c.r*c.r + 5*c.i*c.i*c.i*c.i*c.r, 5*c.i*c.r*c.r*c.r*c.r - 10*c.i*c.i*c.i*c.r*c.r + c.i*c.i*c.i*c.i*c.i);
			case 7:  return complex_new(c.r*c.r*c.r*c.r*c.r*c.r*c.r - 21*c.i*c.i*c.r*c.r*c.r*c.r*c.r + 35*c.i*c.i*c.i*c.i*c.r*c.r*c.r - 7*c.i*c.i*c.i*c.i*c.i*c.i*c.r, 7*c.i*c.r*c.r*c.r*c.r*c.r*c.r - 35*c.i*c.i*c.i*c.r*c.r*c.r*c.r + 21*c.i*c.i*c.i*c.i*c.i*c.r*c.r - c.i*c.i*c.i*c.i*c.i*c.i*c.i);

			case 4:  return complex_pow(complex_pow(c, 2), 2);
			case 6:  return complex_pow(complex_pow(c, 3), 2);
			case 8:  return complex_pow(complex_pow(c, 4), 2);
			case 9:  return complex_pow(complex_pow(c, 3), 3);
			case 10: return complex_pow(complex_pow(c, 5), 2);

			default: {
				float rn = powf(sqrtf(powf(c.r, 2) + powf(c.i, 2)), n);
				float th = atanf(c.i / c.r);

				return complex_new(rn * cosf(n * th), rn * sinf(n * th));
			}
		}
}

float complex_mod2(complex c) {
	return c.r*c.r + c.i*c.i;
}

complex complex_mul(complex a, complex b) {
	return complex_new(a.r*b.r - a.i*b.i, a.r*b.i + a.i*b.r);
}

complex complex_div(complex a, complex b) {
	float temp = b.r*b.r + b.i*b.i;
	return complex_new((a.r*b.r + a.i*b.i)/temp, (a.i*b.r - a.r*b.i)/temp);
}

complex complex_inv(complex c) {
	return complex_div(complex_new(1, 0), c);
}
