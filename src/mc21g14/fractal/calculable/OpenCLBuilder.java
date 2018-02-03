package mc21g14.fractal.calculable;

import mc21g14.fractal.misc.FractalState;

import java.awt.*;

public class OpenCLBuilder {
	public static String templateInverse = "if(*inverse) {base = invComplex(base); past = invComplex(past);}";

	public static String[] templateFormulas = {
			"addComplex(powComplex(past, *order), base)",
			"addComplex(powComplex(absComplex(past), *order), base)",
			"addComplex(powComplex(newComplex(past.r, 0 - past.i), *order), base)",
			"addComplex(divComplex(mulComplex(newComplex(1, 0), subComplex(powComplex(past, *order), newComplex(1, 0))), mulComplex(newComplex(*order, 0), powComplex(past, *order - 1)), base)",
			"powComplex(past, *order)"
	};

	public static String templateOrderSetup =
			"		/* For orbit traps */\n" +
					"		int    distancelength = *region == 1 ? 5 : (*region == 2 ? 4 : 1);\n" +
					"		double distance[5];\n" +
					"		for(int i = 0; i < distancelength; i++) distance[i] = DBL_MAX;\n";

	public static String[] templateAfter = {
		"				// Buddha colouring, slow\n" +
		"				int j = (int) ((*width  * (current.r - *start)) / (*stop   - *start));\n" +
		"				int k = (int) ((*height * (current.i - *top))   / (*bottom - *top));\n" +
		"				if(k * *width + j >= 0 && k * *width + j < *width * *height)\n" +
		"				results[k * *width + j] = results[k * *width + j] + 1;\n",

		"				if(*orbit == 1) {\n" +
		"					if(current.r*current.r < distance[i %% distancelength]) distance[i %% distancelength] = current.r*current.r;\n" +
		"					if(current.i*current.i < distance[i %% distancelength]) distance[i %% distancelength] = current.i*current.i;\n" +
		"				} else {\n" +
		"					if(mod2Complex(current) < distance[i %% distancelength])\n" +
		"						distance[i %% distancelength] = mod2Complex(current);\n" +
		"				}\n" +
		"				int pointer;\n" +
		"				if(*region == 1) {\n" +
		"					pointer = (current.i > 0 ? 1 : 0) + (current.r > 0 ? 2 : 0);\n" +
		"				} else {\n" +
		"					pointer = i %% distancelength;\n" +
		"				}\n" +
		"				if(i == *iterations - 1) {\n" +
		"					if(distance[pointer] < 1) {\n" +
		"						results[y * *width + x] = sqrt(distance[pointer]) * *iterations;\n" +
		"					} else {\n" +
		"						results[y * *width + x] = 0;\n" +
		"					}\n" +
		"					break;\n" +
		"				}\n",

		"				if(modulus > t) {\n" +
		"					double k = (t - mod2Complex(past)) / abs(mod2Complex(past) - mod2Complex(current));\n" +
		"					if(k < 0) k = 0.0;\n" +
		"					results[y * *width + x] = i + k - 1;\n" +
		"					break;\n" +
		"				} else if(i == *iterations - 1) {\n" +
		"					/* Fallback if we don't reach anything. We need to fill in everything otherwise we\n" +
		"					   start to see weird patterns as variables don't really have a default here */\n" +
		"					results[y * *width + x] = -1;\n" +
		"				}\n",

		"				if(modulus > t) {\n" +
		"					results[y * *width + x] = i - 1;\n" +
		"					break;\n" +
		"				} else if(i == *iterations - 1) {\n" +
		"					/* Fallback if we don't reach anything. We need to fill in everything otherwise we\n" +
		"					   start to see weird patterns as variables don't really have a default here */\n" +
		"					results[y * *width + x] = -1;\n" +
		"				}\n"
	};

	public static String template = "#pragma OPENCL EXTENSION cl_khr_fp64 : enable\n" +
			"\n" +
			"double __attribute__((overloadable)) abs(double value) {\n" +
			"	if(value >= 0) return value;\n" +
			"	return 0 - value;\n" +
			"}\n" +
			"\n" +
			"struct complex {\n" +
			"	double r;\n" +
			"	double i;\n" +
			"};\n" +
			"\n" +
			"struct complex newComplex(double r, double i) {\n" +
			"	struct complex c;\n" +
			"	c.r = r;\n" +
			"	c.i = i;\n" +
			"	return c;\n" +
			"}\n" +
			"\n" +
			"struct complex addComplex(struct complex a, struct complex b) {\n" +
			"	return newComplex(a.r + b.r, a.i + b.i);\n" +
			"}\n" +
			"\n" +
			"struct complex subComplex(struct complex a, struct complex b) {\n" +
			"	return newComplex(a.r - b.r, a.i - b.i);\n" +
			"}\n" +
			"\n" +
			"struct complex absComplex(struct complex c) {\n" +
			"	return newComplex(abs(c.r), abs(c.i));\n" +
			"}\n" +
			"\n" +
			"struct complex powComplex(struct complex c, int n) {\n" +
			"	switch (n) {\n" +
			"			case 0:  return newComplex(1, 0);\n" +
			"\n" +
			"			case 1:  return newComplex(c.r, c.i);\n" +
			"			case 2:  return newComplex(c.r*c.r - c.i*c.i, 2*c.r*c.i);\n" +
			"			case 3:  return newComplex(c.r*c.r*c.r - 3*c.i*c.i*c.r, 3*c.i*c.r*c.r - c.i*c.i*c.i);\n" +
			"			case 5:  return newComplex(c.r*c.r*c.r*c.r*c.r - 10*c.i*c.i*c.r*c.r*c.r + 5*c.i*c.i*c.i*c.i*c.r, 5*c.i*c.r*c.r*c.r*c.r - 10*c.i*c.i*c.i*c.r*c.r + c.i*c.i*c.i*c.i*c.i);\n" +
			"			case 7:  return newComplex(c.r*c.r*c.r*c.r*c.r*c.r*c.r - 21*c.i*c.i*c.r*c.r*c.r*c.r*c.r + 35*c.i*c.i*c.i*c.i*c.r*c.r*c.r - 7*c.i*c.i*c.i*c.i*c.i*c.i*c.r, 7*c.i*c.r*c.r*c.r*c.r*c.r*c.r - 35*c.i*c.i*c.i*c.r*c.r*c.r*c.r + 21*c.i*c.i*c.i*c.i*c.i*c.r*c.r - c.i*c.i*c.i*c.i*c.i*c.i*c.i);\n" +
			"\n" +
			"			case 4:  return powComplex(powComplex(c, 2), 2);\n" +
			"			case 6:  return powComplex(powComplex(c, 3), 2);\n" +
			"			case 8:  return powComplex(powComplex(c, 4), 2);\n" +
			"			case 9:  return powComplex(powComplex(c, 3), 3);\n" +
			"			case 10: return powComplex(powComplex(c, 5), 2);\n" +
			"\n" +
			"			default: {\n" +
			"				double rn = pown(sqrt(pown(c.r, 2) + pown(c.i, 2)), n);\n" +
			"				double th = atan(c.i / c.r);\n" +
			"\n" +
			"				return newComplex(rn * cos(n * th), rn * sin(n * th));\n" +
			"			}\n" +
			"		}\n" +
			"}\n" +
			"\n" +
			"double mod2Complex(struct complex c) {\n" +
			"	return c.r*c.r + c.i*c.i;\n" +
			"}\n" +
			"\n" +
			"struct complex mulComplex(struct complex a, struct complex b) {\n" +
			"	return newComplex(a.r*b.r - a.i*b.i, a.r*b.i + a.i*b.r);\n" +
			"}\n" +
			"\n" +
			"struct complex divComplex(struct complex a, struct complex b) {\n" +
			"	double inter = b.r*b.r + b.i*b.i;\n" +
			"	return newComplex((a.r*b.r + a.i*b.i)/inter, (a.i*b.r - a.r*b.i)/inter);\n" +
			"}\n" +
			"\n" +
			"struct complex invComplex(struct complex c) {\n" +
			"	return divComplex(newComplex(1, 0), c);\n" +
			"}\n" +
			"\n" +
			"kernel void fractalKernel(\n" +
			"	global const double *start,\n" +
			"	global const double *stop,\n" +
			"	global const double *top,\n" +
			"	global const double *bottom,\n" +
			"\n" +
			"	global const int    *width,\n" +
			"	global const int    *height,\n" +
			"	global const int    *iterations,\n" +
			"\n" +
			"	global const double *threshold,\n" +
			"\n" +
			"	global const double *seedr,\n" +
			"	global const double *seedi,\n" +
			"\n" +
			"	global const int    *order,\n" +
			"	global const int    *orbit,\n" +
			"	global const int    *region,\n" +
			"\n" +
			"	global double *results\n" +
			") {\n" +
			"	/* For explaination, refer to JavaCalculator.java */\n" +
			"\n" +
			"	/* Find out where we are */\n" +
			"	const int id = get_global_id(0);\n" +
			"	const int x = id %% *width;\n" +
			"	const int y = id / *width;\n" +
			"\n" +
			"	/* Check that we should calculate for this pixel */\n" +
			"	if(y < *height) {\n" +
			"		struct complex past = newComplex(\n" +
			"			*start + (*stop   - *start) * x / *width,\n" +
			"			*top   + (*bottom - *top  ) * y / *height\n" +
			"		);\n" +
			"\n" +
			"		/* Using DBL_MAX to indicate that there is no seed */\n" +
			"		struct complex base = *seedr == DBL_MAX ? past : newComplex(*seedr, *seedi);\n" +
			"\n" +
			"%s\n" +
			"\n" +
			"		const double t = *threshold * *threshold;\n" +
			"\n" +
			"%s\n" +
			"\n" +
			"		for(int i = 1; i < *iterations; i++) {\n" +
			"\n" +
			"			struct complex current = %s;\n" +
			"\n" +
			"			/* Modulus */\n" +
			"			double modulus = mod2Complex(current);\n" +
			"\n" +
			"%s\n" +
			"\n" +
			"			/* Copy the new to the old, we will need it later, but by another name */\n" +
			"			past = current;\n" +
			"		}\n" +
			"	}\n" +
			"};";

	public static String build(FractalState state) {
		int fractalIndex = 0;
		for(int i = 0; i < Calculable.getImplemented().length; i++) {
			if(Calculable.getImplemented()[i].equals(state.fractal)) {
				fractalIndex = i;
				break;
			}
		}

		return String.format(template, state.inverse ? templateInverse : "", state.orbitTraps != "None" ? templateOrderSetup : "", templateFormulas[fractalIndex], templateAfter[state.buddha ? 0 : (state.orbitTraps != "None" ? 1 : (state.smooth ? 2 : 3))]);
	}
}
