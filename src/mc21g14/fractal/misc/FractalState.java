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
	public double    start         = -2.0f;
	public double    end           = 2.0f;
	public double    top           = -1.6f;
	public double    bottom        = 1.6f;
	public int       iterations    = 100;
	public double    threshold     = 2f;
	public boolean   smooth        = true;
	public Complex   seed          = null;
	public String    fractal       = "Mandlebrot";
	public int       order         = 2;
	public boolean   inverse       = false;
	public boolean   buddha        = false;
	public String    orbitTraps    = "None";
	public String    regionSplits  = "None";
	public Colorable coloring      = new HueShift();
	public boolean   invert        = false;
	
	/**
	 * Initialise a state with default variables
	 */
	public FractalState() {}
	
	/**
	 * Initialise a state with custom variables, used mainly for cloning.
	 */
	protected FractalState(
		double   start,        double  end,        double  top,        double  bottom, 
        int      iterations,   double  threshold,  boolean smooth,     Complex seed,
        String   selected,     int     order,      boolean inverse,    boolean buddha,
        String   regionSplits, String  orbitTraps, Colorable coloring, boolean invert
	) {
		this.start        = start;
		this.end          = end;
		this.top          = top;
		this.bottom       = bottom;
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
		return new FractalState(
			start, end, top, bottom, iterations, threshold, smooth, seed, fractal, order, inverse,
			buddha, regionSplits, orbitTraps, coloring, invert
		);
	}
	
	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Object)) return false;
		
		FractalState that = (FractalState) other;
		
		// Long and horrid comparison
	    return this.start        == that.start        &&
	    	   this.end          == that.end          &&
	    	   this.top          == that.top          &&
	    	   this.bottom       == that.bottom       &&
	    	   this.iterations   == that.iterations   &&
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
