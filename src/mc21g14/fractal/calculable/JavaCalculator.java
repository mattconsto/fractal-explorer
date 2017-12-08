package mc21g14.fractal.calculable;

import java.awt.Dimension;

import mc21g14.fractal.misc.FractalState;

/**
 * Java calculation backend.
 * 
 * @author Matthew Consterdine
 */
public class JavaCalculator extends Calculable {
	@Override
	public String[] getImplementedFractals() {
		return new String[] {"Mandlebrot", "Burning Ship", "Tricorn", "Nova", "Circle"};
	}
	
	protected double[] calculate(final FractalState state, final Dimension size) {
		if(state.fractal == null) state.fractal = getImplementedFractals()[0];
		
		// We are storing the results as number of iterations taken. Negative means never reached
		double[] results = new double[size.width * size.height];
		
		// Create threads
		Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors()];
		for(int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new JavaRunnable(state, size, results, threads.length, i));
			threads[i].start();
		}

		// Join as we need to wait for them all to complete before we show the image
		try {
			for(int i = 0; i < threads.length; i++) threads[i].join();
		} catch (InterruptedException e) {
			System.err.println("Interrupted: " + e.getMessage());
		}
		
		return results;
	}
}
