#pragma once

#include <math.h>

typedef struct {
	float r;
	float i;
} complex;

complex complex_new (float r, float i);
complex complex_add (complex a, complex b);
complex complex_sub (complex a, complex b);
complex complex_abs (complex c);
complex complex_pow (complex c, int n);
float   complex_mod2(complex c);
complex complex_mul (complex a, complex b);
complex complex_div (complex a, complex b);
complex complex_inv (complex c);
