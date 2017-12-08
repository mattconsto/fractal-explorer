package mc21g14.fractal.calculable;

import java.awt.Dimension;
import java.util.Arrays;

import mc21g14.fractal.misc.Complex;
import mc21g14.fractal.misc.FractalState;

class JavaRunnable implements Runnable {
	protected FractalState state;
	protected Dimension size;
	
	protected double[] results;
	
	// SO we know where we are and what to do
	protected int total;
	protected int id;
	
	public JavaRunnable(FractalState state, Dimension size, double[] results, int total, int id) {
		this.state   = state;
		this.size    = size;
		this.results = results;
		this.total   = total;
		this.id      = id;
	}
	
	@Override
	public void run() {
		// Save a little time later
		double threshold = state.threshold * state.threshold;
		
		// Iterate only over the pixels we are 
		for(int x = id * size.width / total; x < (id + 1) * size.width / total; x++) {
			for(int y = 0; y < size.height; y++) {
				// We need a base complex and a past complex for each iteration
				Complex past = new Complex(
					state.start + (state.end - state.start) * x / size.width,
					state.top - (state.top - state.bottom) * y / size.height
				);
				Complex base = state.seed != null ? state.seed : past;
				
				// Inverse the base value, for pretty images
				if(state.inverse) {
					base = base.inverse();
					past = past.inverse();
				}
				
				// For orbit traps
				double distance[] = new double[state.regionSplits.equals("Iterations") ? 5 : (state.regionSplits.equals("Axis") ? 4 : 1)];
				Arrays.fill(distance, Double.MAX_VALUE);
				
				for(int i = 1; i < state.iterations; i++) {
					Complex current = null;
					
					// Choose our fractal
					switch(state.fractal) {
						case "Mandlebrot":
							current = past.pow(state.order).add(base);
							break;
						case "Burning Ship":
							current = new Complex(Math.abs(past.r), Math.abs(past.i)).pow(state.order).add(base);
							break;
						case "Tricorn":
							current = new Complex(past.r, past.i * -1).pow(state.order).add(base);
							break;
						case "Nova": // Not 100% sure that this is correct, but oh well, it looks nice, if slow
							current = past.subtract(new Complex(1, 0).multiply(
									past.pow(state.order).subtract(new Complex(1, 0))
								).divide(
									new Complex(state.order, 0).multiply(past.pow(state.order - 1))
								)).add(base);
							break;
						case "Circle":
							// Why not :D
							current = past.pow(state.order);
							break;
						default:
							throw new UnsupportedOperationException(state.fractal + " isn't implemented");
					}
					
					if(state.buddha) {
						// Buddha colouring, slow
						int j = (int) ((size.width  * (current.r - state.start)) / (state.end	- state.start));
						int k = (int) ((size.height * (current.i - state.top))   / (state.bottom - state.top));
						
						if(k * size.width + j >= 0 && k * size.width + j < size.width * size.height) {
							results[k * size.width + j]++;
						}
					} else if(!state.orbitTraps.equals("None")) {
						// Trap our orbits
						if(state.orbitTraps.equals("Cross")) {
							if(current.r*current.r < distance[i % distance.length]) distance[i % distance.length] = current.r*current.r;
							if(current.i*current.i < distance[i % distance.length]) distance[i % distance.length] = current.i*current.i;
						} else {
							if(current.modulusSquared() < distance[i % distance.length]) distance[i % distance.length] = current.modulusSquared();
						}
						
						int pointer;
						if(state.regionSplits.equals("Iterations")) {
							pointer = (current.i > 0 ? 1 : 0) + (current.r > 0 ? 2 : 0);
						} else {
							pointer = i % distance.length;
						}
						if(i == state.iterations - 1 && distance[pointer] < 1) {
							results[y * size.width + x] = Math.sqrt(distance[pointer]) * state.iterations;
							break;
						}
					} else {
						if(current.modulusSquared() > threshold) {
							if(state.smooth) {
								// Smoothness
								double k = Math.max((threshold - past.modulusSquared()) / Math.abs((past.modulusSquared() - current.modulusSquared())), 0);
								results[y * size.width + x] = i + k - 1;
							} else {
								// No smoothness
								results[y * size.width + x] = i - 1;
							}
							break;
						} else if(i == state.iterations - 1) {
							// Default case of -1
							results[y * size.width + x] = -1;
							break;
						}
					}
					
					// Store the past
					past = current;
				}
			}
		}
	}
}