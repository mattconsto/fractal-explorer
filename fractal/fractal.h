#pragma once

#include <float.h>
#include <math.h>

#include "complex.h"

typedef struct {
	int   width;
	int   height;

	float start;
	float stop;
	float top;
	float bottom;

	int   iterations;
	float threshold;
	int   smooth;

	float seedr;
	float seedi;

	int   selected;
	int   order;
	int   inverse;
	int   orbit;
	int   region;
} fractal_state;

float generate_fractal(fractal_state state, int x, int y);
