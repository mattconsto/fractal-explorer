package mc21g14.fractal.calculable;

import java.awt.Dimension;

/**
 * Callback interface for the calculate method
 * 
 * @author Matthew Consterdine
 */
public interface Callback {
	/**
	 * Called when the fractal has been calculated
	 * @param data A double array containing the fractal data
	 * @param size The size of the fractal generated
	 */
	public void callback(double[] data, Dimension size);
}