#pragma OPENCL EXTENSION cl_khr_fp64 : enable

double __attribute__((overloadable)) abs(double value) {return value >= 0 ? value : 0 - value;}

struct complex {double r; double i;};

struct complex newComplex(double r, double i) {
	struct complex c;
	c.r = r;
	c.i = i;
	return c;
}

struct complex addComplex(struct complex a, struct complex b) {return newComplex(a.r + b.r, a.i + b.i);}

struct complex subComplex(struct complex a, struct complex b) {return newComplex(a.r - b.r, a.i - b.i);}

struct complex absComplex(struct complex c) {return newComplex(abs(c.r), abs(c.i));}

struct complex powComplex(struct complex c, int n) {
	switch (n) {
			case 0:  return newComplex(1, 0);
			
			case 1:  return newComplex(c.r, c.i);
			case 2:  return newComplex(c.r*c.r - c.i*c.i, 2*c.r*c.i);
			case 3:  return newComplex(c.r*c.r*c.r - 3*c.i*c.i*c.r, 3*c.i*c.r*c.r - c.i*c.i*c.i);
			case 5:  return newComplex(c.r*c.r*c.r*c.r*c.r - 10*c.i*c.i*c.r*c.r*c.r + 5*c.i*c.i*c.i*c.i*c.r, 5*c.i*c.r*c.r*c.r*c.r - 10*c.i*c.i*c.i*c.r*c.r + c.i*c.i*c.i*c.i*c.i);
			case 7:  return newComplex(c.r*c.r*c.r*c.r*c.r*c.r*c.r - 21*c.i*c.i*c.r*c.r*c.r*c.r*c.r + 35*c.i*c.i*c.i*c.i*c.r*c.r*c.r - 7*c.i*c.i*c.i*c.i*c.i*c.i*c.r, 7*c.i*c.r*c.r*c.r*c.r*c.r*c.r - 35*c.i*c.i*c.i*c.r*c.r*c.r*c.r + 21*c.i*c.i*c.i*c.i*c.i*c.r*c.r - c.i*c.i*c.i*c.i*c.i*c.i*c.i);

			case 4:  return powComplex(powComplex(c, 2), 2);
			case 6:  return powComplex(powComplex(c, 3), 2);
			case 8:  return powComplex(powComplex(c, 4), 2);
			case 9:  return powComplex(powComplex(c, 3), 3);
			case 10: return powComplex(powComplex(c, 5), 2);
			
			default: {
				double rn = pown(sqrt(pown(c.r, 2) + pown(c.i, 2)), n);
				double th = atan(c.i / c.r);
				
				return newComplex(rn * cos(n * th), rn * sin(n * th));
			}
		}
}

double mod2Complex(struct complex c) {return c.r*c.r + c.i*c.i;}

struct complex mulComplex(struct complex a, struct complex b) {return newComplex(a.r*b.r - a.i*b.i, a.r*b.i + a.i*b.r);}

struct complex divComplex(struct complex a, struct complex b) {
	double inter = b.r*b.r + b.i*b.i;
	return newComplex((a.r*b.r + a.i*b.i)/inter, (a.i*b.r - a.r*b.i)/inter);
}

struct complex invComplex(struct complex c) {
	double inter = c.r*c.r + c.i*c.i;
	return newComplex(c.r/inter, (0 - c.i)/inter);
}

kernel void fractalKernel(
	global const double *start,
	global const double *stop,
	global const double *top,
	global const double *bottom,

	global const int    *width,
	global const int    *height,
	global const int    *iterations,

	global const double *threshold,

	global const int    *smooth,

	global const double *seedr,
	global const double *seedi,

	global const int    *selected,
	global const int    *order,
	global const int    *inverse,
	global const int    *buddha,
	global const int    *orbit,
	global const int    *region,

	global double *results
) {
	/* For explaination, refer to JavaCalculator.java */

	/* Find out where we are */
	const int id = get_global_id(0);
	const int x = id % *width;
	const int y = id / *width;

	/* Check that we should calculate for this pixel */
	if(y < *height) {
		struct complex past = newComplex(*start + (*stop - *start) * x / *width, *top + (*bottom - *top) * y / *height);

		/* Using DBL_MAX to indicate that there is no seed */
		struct complex base = *seedr == DBL_MAX ? past : newComplex(*seedr, *seedi);

		if(*inverse) {base = invComplex(base); past = invComplex(past);}

		results[id] = 0;

		if(*buddha) {
			switch(*selected) {
				default:
				case 0: /* Mandlebrot */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(past, *order), base);

						int j = (int) ((*width  * (past.r - *start)) / (*stop   - *start));
						int k = (int) ((*height * (past.i - *top))   / (*bottom - *top));
						if(k * *width + j >= 0 && k * *width + j < *width * *height) results[k * *width + j] = results[k * *width + j] + 1;
					}
					break;
				case 1: /* Burning Ship */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(absComplex(past), *order), base);

						int j = (int) ((*width  * (past.r - *start)) / (*stop   - *start));
						int k = (int) ((*height * (past.i - *top))   / (*bottom - *top));
						if(k * *width + j >= 0 && k * *width + j < *width * *height) results[k * *width + j] = results[k * *width + j] + 1;
					}
					break;
				case 2: /* Tricorn */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(newComplex(past.r, 0 - past.i), *order), base);

						int j = (int) ((*width  * (past.r - *start)) / (*stop   - *start));
						int k = (int) ((*height * (past.i - *top))   / (*bottom - *top));
						if(k * *width + j >= 0 && k * *width + j < *width * *height) results[k * *width + j] = results[k * *width + j] + 1;
					}
					break;
				case 3: /* Nova */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(divComplex(mulComplex(newComplex(1, 0), subComplex(powComplex(past, *order), newComplex(1, 0))), mulComplex(newComplex(*order, 0), powComplex(past, *order - 1))), base);

						int j = (int) ((*width  * (past.r - *start)) / (*stop   - *start));
						int k = (int) ((*height * (past.i - *top))   / (*bottom - *top));
						if(k * *width + j >= 0 && k * *width + j < *width * *height) results[k * *width + j] = results[k * *width + j] + 1;
					}
					break;
				case 4: /* Circle */
					for(int i = 1; i < *iterations; i++) {
						past = powComplex(past, *order);

						int j = (int) ((*width  * (past.r - *start)) / (*stop   - *start));
						int k = (int) ((*height * (past.i - *top))   / (*bottom - *top));
						if(k * *width + j >= 0 && k * *width + j < *width * *height) results[k * *width + j] = results[k * *width + j] + 1;
					}
					break;
			}
		} else if(*orbit) {
			/* For orbit traps */
			int    distancelength = *region == 1 ? 5 : (*region == 2 ? 4 : 1);
			double distance[5];
			for(int i = 0; i < distancelength; i++) distance[i] = DBL_MAX;

			switch(*selected) {
				default:
				case 0: /* Mandlebrot */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(past, *order), base);

						if(*orbit == 1) {
							if(past.r*past.r < distance[i % distancelength]) distance[i % distancelength] = past.r*past.r;
							if(past.i*past.i < distance[i % distancelength]) distance[i % distancelength] = past.i*past.i;
						} else {
							if(mod2Complex(past) < distance[i % distancelength]) distance[i % distancelength] = mod2Complex(past);
						}
					}
					break;
				case 1: /* Burning Ship */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(absComplex(past), *order), base);

						if(*orbit == 1) {
							if(past.r*past.r < distance[i % distancelength]) distance[i % distancelength] = past.r*past.r;
							if(past.i*past.i < distance[i % distancelength]) distance[i % distancelength] = past.i*past.i;
						} else {
							if(mod2Complex(past) < distance[i % distancelength]) distance[i % distancelength] = mod2Complex(past);
						}
					}
					break;
				case 2: /* Tricorn */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(newComplex(past.r, 0 - past.i), *order), base);

						if(*orbit == 1) {
							if(past.r*past.r < distance[i % distancelength]) distance[i % distancelength] = past.r*past.r;
							if(past.i*past.i < distance[i % distancelength]) distance[i % distancelength] = past.i*past.i;
						} else {
							if(mod2Complex(past) < distance[i % distancelength]) distance[i % distancelength] = mod2Complex(past);
						}
					}
					break;
				case 3: /* Nova */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(divComplex(mulComplex(newComplex(1, 0), subComplex(powComplex(past, *order), newComplex(1, 0))), mulComplex(newComplex(*order, 0), powComplex(past, *order - 1))), base);

						if(*orbit == 1) {
							if(past.r*past.r < distance[i % distancelength]) distance[i % distancelength] = past.r*past.r;
							if(past.i*past.i < distance[i % distancelength]) distance[i % distancelength] = past.i*past.i;
						} else {
							if(mod2Complex(past) < distance[i % distancelength]) distance[i % distancelength] = mod2Complex(past);
						}
					}
					break;
				case 4: /* Circle */
					for(int i = 1; i < *iterations; i++) {
						past = powComplex(past, *order);

						if(*orbit == 1) {
							if(past.r*past.r < distance[i % distancelength]) distance[i % distancelength] = past.r*past.r;
							if(past.i*past.i < distance[i % distancelength]) distance[i % distancelength] = past.i*past.i;
						} else {
							if(mod2Complex(past) < distance[i % distancelength]) distance[i % distancelength] = mod2Complex(past);
						}
					}
					break;
			}

			int pointer = *region == 1 ? (past.i > 0 ? 1 : 0) + (past.r > 0 ? 2 : 0) : (*iterations - 1) % distancelength;
			if(distance[pointer] < 1) results[id] = sqrt(distance[pointer]) * *iterations;
		} else if(*smooth) {
			const double t = *threshold * *threshold;
			results[id] = -1;

			switch(*selected) {
				default:
				case 0: /* Mandlebrot */
					for(int i = 1; i < *iterations; i++) {
						struct complex current = addComplex(powComplex(past, *order), base);

						double modulusPast = mod2Complex(past);
						double modulusCurrent = mod2Complex(current);

						if(modulusCurrent > t) {
							double k = (t - modulusPast) / abs(modulusPast - modulusCurrent);
							if(k < 0) k = 0.0;
							results[id] = i + k - 1;
							break;
						}

						past = current;
					}
					break;
				case 1: /* Burning Ship */
					for(int i = 1; i < *iterations; i++) {
						struct complex current = addComplex(powComplex(absComplex(past), *order), base);

						double modulusPast = mod2Complex(past);
						double modulusCurrent = mod2Complex(current);

						if(modulusCurrent > t) {
							double k = (t - modulusPast) / abs(modulusPast - modulusCurrent);
							if(k < 0) k = 0.0;
							results[id] = i + k - 1;
							break;
						}

						past = current;
					}
					break;
				case 2: /* Tricorn */
					for(int i = 1; i < *iterations; i++) {
						struct complex current = addComplex(powComplex(newComplex(past.r, 0 - past.i), *order), base);

						double modulusPast = mod2Complex(past);
						double modulusCurrent = mod2Complex(current);

						if(modulusCurrent > t) {
							double k = (t - modulusPast) / abs(modulusPast - modulusCurrent);
							if(k < 0) k = 0.0;
							results[id] = i + k - 1;
							break;
						}

						past = current;
					}
					break;
				case 3: /* Nova */
					for(int i = 1; i < *iterations; i++) {
						struct complex current = addComplex(divComplex(mulComplex(newComplex(1, 0), subComplex(powComplex(past, *order), newComplex(1, 0))), mulComplex(newComplex(*order, 0), powComplex(past, *order - 1))), base);

						double modulusPast = mod2Complex(past);
						double modulusCurrent = mod2Complex(current);

						if(modulusCurrent > t) {
							double k = (t - modulusPast) / abs(modulusPast - modulusCurrent);
							if(k < 0) k = 0.0;
							results[id] = i + k - 1;
							break;
						}

						past = current;
					}
					break;
				case 4: /* Circle */
					for(int i = 1; i < *iterations; i++) {
						struct complex current = powComplex(past, *order);

						double modulusPast = mod2Complex(past);
						double modulusCurrent = mod2Complex(current);

						if(modulusCurrent > t) {
							double k = (t - modulusPast) / abs(modulusPast - modulusCurrent);
							if(k < 0) k = 0.0;
							results[id] = i + k - 1;
							break;
						}

						past = current;
					}
					break;
			}
		} else {
			const double t = *threshold * *threshold;
			results[id] = -1;

			switch(*selected) {
				default:
				case 0: /* Mandlebrot */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(past, *order), base);

						if(mod2Complex(past) > t) {
							results[id] = i - 1;
							break;
						}
					}
					break;
				case 1: /* Burning Ship */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(absComplex(past), *order), base);

						if(mod2Complex(past) > t) {
							results[id] = i - 1;
							break;
						}
					}
					break;
				case 2: /* Tricorn */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(powComplex(newComplex(past.r, 0 - past.i), *order), base);

						if(mod2Complex(past) > t) {
							results[id] = i - 1;
							break;
						}
					}
					break;
				case 3: /* Nova */
					for(int i = 1; i < *iterations; i++) {
						past = addComplex(divComplex(mulComplex(newComplex(1, 0), subComplex(powComplex(past, *order), newComplex(1, 0))), mulComplex(newComplex(*order, 0), powComplex(past, *order - 1))), base);

						if(mod2Complex(past) > t) {
							results[id] = i - 1;
							break;
						}
					}
					break;
				case 4: /* Circle */
					for(int i = 1; i < *iterations; i++) {
						past = powComplex(past, *order);

						if(mod2Complex(past) > t) {
							results[id] = i - 1;
							break;
						}
					}
					break;
			}
		}
	}
};
