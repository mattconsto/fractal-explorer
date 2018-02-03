package mc21g14.fractal.misc;

import java.io.Serializable;

import mc21g14.fractal.colorable.*;

/**
 * Our Fractal location
 *
 * @author Matthew Consterdine
 */
public class FractalLocation implements Cloneable, Serializable {
	/*
	 * Here is our state
	 */
	public double    start         = -2.0f;
	public double    end           = 2.0f;
	public double    top           = -1.6f;
	public double    bottom        = 1.6f;

	/**
	 * Initialise a state with default variables
	 */
	public FractalLocation() {}

	/**
	 * Initialise a state with custom variables, used mainly for cloning.
	 */
	protected FractalLocation(double start, double end, double top, double bottom) {
		this.start        = start;
		this.end          = end;
		this.top          = top;
		this.bottom       = bottom;
	}

	@Override
	public FractalLocation clone() {
		return new FractalLocation(start, end, top, bottom);
	}

	@Override
	public boolean equals(Object other) {
		if(!(other instanceof Object)) return false;

		FractalLocation that = (FractalLocation) other;

		// Long and horrid comparison
		return this.start == that.start && this.end == that.end && this.top == that.top && this.bottom == that.bottom;
	}
}
