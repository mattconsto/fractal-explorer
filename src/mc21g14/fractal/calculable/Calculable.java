package mc21g14.fractal.calculable;

import java.awt.Dimension;

import mc21g14.fractal.misc.FractalState;

/**
 * Abstract class for the different supported calculation backends
 * 
 * @author Matthew Consterdine
 */
public abstract class Calculable {
	protected static Calculable instance;
	protected static boolean	running = false;
	
	/**
	 * Calculate the fractal asynchronously for a given state and size
	 * @param state The fractal state we want to render
	 * @param size The size of the fractal we want to render
	 * @param call The function called when the fractal has been calculated
	 */
	public static void calcAsync(final FractalState state, final Dimension size, final Callback call) {
		if(!running) {
			running = true;
			final long time = System.nanoTime();
			new Thread() {
				public void run() {
					call.callback(getInstance().calculate(state, size), size);
					System.out.println("Took " + (System.nanoTime() - time) / 1_000_000_000.0);
				};
			}.start();
			running = false;
		}
	}
	
	/**
	 * Calculate the fractal on the same thread for a given state and size
	 * @param state The fractal state we want to render
	 * @param size The size of the fractal we want to render
	 * @param call The function called when the fractal has been calculated
	 */
	public static void calcBlocking(final FractalState state, final Dimension size, final Callback call) {
		if(!running) {
			running = true;
			final long time = System.nanoTime();
			call.callback(getInstance().calculate(state, size), size);
			System.out.println((System.nanoTime() - time) / 1_000_000_000.0);
			running = false;
		}
	}
	
	/**
	 * Abstract method that calculates the fractal asynchronously for a given state and size
	 * @return A double array containing the fractal data
	 */
	protected abstract double[] calculate(final FractalState state, final Dimension size);

	/**
	 * Abstract method that gets an array of implemented fractals from an implementation. This may
	 * change depending on the calculator
	 * @return An array of implemented fractals
	 */
	protected abstract String[] getImplementedFractals();

	/**
	 * Get an array of implemented fractals. This may change depending on the calculator
	 * @return An array of implemented fractals
	 */
	public static String[] getImplemented() {
		return getInstance().getImplementedFractals();
	}
	
	protected static Calculable getInstance() {
		if(instance == null) {
			try {
				Calculable.set(OpenCLCalculator.class);
			} catch (Exception e) {
				System.err.println("GPU unavaliable, falling back to a slower CPU implementation");
				try {
					Calculable.set(JavaCalculator.class);
				} catch (Exception ohno) {
					e.printStackTrace();
				}
			}
		}
		
		return instance;
	}
	
	/**
	 * Get the current class used
	 * @return The class being used
	 */
	public static Class<? extends Calculable> get() {
		return instance == null ? null : instance.getClass();
	}
	
	/**
	 * Sets the current class to be used
	 * @param implementation The implementation
	 * @throws Exception Many things can go wrong
	 */
	public static void set(Class<? extends Calculable> implementation) throws Exception {
		// JOCL segfaults on linux
		if(implementation == OpenCLCalculator.class && !(
			System.getProperty("os.name").toLowerCase().contains("windows") ||
			System.getProperty("os.name").toLowerCase().contains("mac")
		)) throw new Exception("OS not supported");
		
		instance = implementation.getConstructor().newInstance();
	}
}
