package mc21g14.fractal.misc;

import java.io.Serializable;

import mc21g14.fractal.colorable.*;

/**
 * Our Fractal state
 * 
 * @author Matthew Consterdine
 */
public class FractalState implements Cloneable, Serializable {
	/*
	 * Here is our state
	 */
	public final int       iterations;
	public final double    threshold;
	public final boolean   smooth;
	public final Complex   seed;
	public final String    fractal;
	public final int       order;
	public final boolean   inverse;
	public final boolean   buddha;
	public final String    orbitTraps;
	public final String    regionSplits;
	public final Colorable coloring;
	public final boolean   invert;

	public FractalState setIterations(int iterations) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setThreshold(double threshold) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setSmooth(boolean smooth) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setSeed(Complex seed) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setFractal(String fractal) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setOrder(int order) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setInverse(boolean inverse) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setBuddha(boolean buddha) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setOrbitTraps(String orbitTraps) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setRegionSplits(String regionSplits) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setColoring(Colorable coloring) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	public FractalState setInvert(boolean invert) {return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse, buddha, orbitTraps, regionSplits, coloring, invert);}
	
	/**
	 * Initialise a state with default variables
	 */
	public FractalState() {
		this(2000, 2f, true, null, "Mandlebrot", 2, false, false, "None", "None", new HueShift(), false);
	}

	/**
	 * Initialise a state with custom variables, used mainly for cloning.
	 */
	protected FractalState(
        int      iterations,   double  threshold,  boolean smooth,     Complex seed,
        String   selected,     int     order,      boolean inverse,    boolean buddha,
        String   regionSplits, String  orbitTraps, Colorable coloring, boolean invert
	) {
		this.iterations   = iterations;
		this.threshold    = threshold;
		this.smooth       = smooth;
		this.seed         = seed;
		this.fractal      = selected;
		this.order        = order;
		this.inverse      = inverse;
		this.buddha       = buddha;
		this.regionSplits = regionSplits;
		this.orbitTraps   = orbitTraps;
		this.coloring     = coloring;
		this.invert       = invert;
	}
	
	@Override
	public FractalState clone() {
		return new FractalState(iterations, threshold, smooth, seed, fractal, order, inverse,
			buddha, regionSplits, orbitTraps, coloring, invert
		);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Object)) return false;
		
		FractalState that = (FractalState) other;
		
		// Long and horrid comparison
	    return this.iterations   == that.iterations   &&
	    	   this.threshold    == that.threshold    &&
	    	   this.smooth       == that.smooth       &&
	    	   this.seed         == that.seed         &&
	    	   this.fractal      == that.fractal      &&
	    	   this.order        == that.order        &&
	    	   this.inverse      == that.inverse      &&
	    	   this.buddha       == that.buddha       &&
	    	   this.regionSplits == that.regionSplits &&
	    	   this.orbitTraps   == that.orbitTraps   &&
	    	   this.coloring     == that.coloring     &&
	    	   this.invert       == that.invert       ;
	}
}
