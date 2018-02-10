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
		if(state.buddha) runBuddha();
		else if(!state.orbitTraps.equals("None")) runTraps();
		else if(state.smooth) runSmooth();
		else runInteger();
	}

	public void runBuddha() {
		for(int i = 0; i < size.width * size.height; i++) {
			int x = i % size.width;
			int y = i / size.width;

			// We need a base complex and a past complex for each iteration
			Complex past = new Complex(state.start + (state.end - state.start) * x / size.width, state.top - (state.top - state.bottom) * y / size.height);
			Complex base = state.seed != null ? state.seed : past;

			// Inverse the base value, for pretty images
			if(state.inverse) {base = base.inverse(); past = past.inverse();}

			switch(state.fractal) {
				case "Mandlebrot": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.pow(state.order).add(base);

						// Buddha colouring, slow
						int j = (int) ((size.width  * (past.r - state.start)) / (state.end    - state.start));
						int k = (int) ((size.height * (past.i - state.top))   / (state.bottom - state.top));
						if(k * size.width + j >= 0 && k * size.width + j < size.width * size.height) results[i]++;
					}
				} break;
				case "Burning Ship": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.abs().pow(state.order).add(base);

						// Buddha colouring, slow
						int j = (int) ((size.width  * (past.r - state.start)) / (state.end    - state.start));
						int k = (int) ((size.height * (past.i - state.top))   / (state.bottom - state.top));
						if(k * size.width + j >= 0 && k * size.width + j < size.width * size.height) results[i]++;
					}
				} break;
				case "Tricorn": {
					for(int it = 1; it < state.iterations; it++) {
						past = new Complex(past.r, past.i * -1).pow(state.order).add(base);

						// Buddha colouring, slow
						int j = (int) ((size.width  * (past.r - state.start)) / (state.end    - state.start));
						int k = (int) ((size.height * (past.i - state.top))   / (state.bottom - state.top));
						if(k * size.width + j >= 0 && k * size.width + j < size.width * size.height) results[i]++;
					}
				} break;
				case "Nova": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.subtract(new Complex(1, 0).multiply(past.pow(state.order).subtract(new Complex(1, 0))).divide(new Complex(state.order, 0).multiply(past.pow(state.order - 1)))).add(base);

						// Buddha colouring, slow
						int j = (int) ((size.width  * (past.r - state.start)) / (state.end    - state.start));
						int k = (int) ((size.height * (past.i - state.top))   / (state.bottom - state.top));
						if(k * size.width + j >= 0 && k * size.width + j < size.width * size.height) results[i]++;
					}
				} break;
				case "Circle": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.pow(state.order);

						// Buddha colouring, slow
						int j = (int) ((size.width  * (past.r - state.start)) / (state.end    - state.start));
						int k = (int) ((size.height * (past.i - state.top))   / (state.bottom - state.top));
						if(k * size.width + j >= 0 && k * size.width + j < size.width * size.height) results[i]++;
					}
				} break;
				default:
					throw new UnsupportedOperationException(state.fractal + " isn't implemented");
			}
		}
	}

	public void runTraps() {
		for(int i = 0; i < size.width * size.height; i++) {
			int x = i % size.width;
			int y = i / size.width;

			// We need a base complex and a past complex for each iteration
			Complex past = new Complex(state.start + (state.end - state.start) * x / size.width, state.top - (state.top - state.bottom) * y / size.height);
			Complex base = state.seed != null ? state.seed : past;

			// Inverse the base value, for pretty images
			if(state.inverse) {base = base.inverse(); past = past.inverse();}

			// For orbit traps
			double distance[] = new double[state.regionSplits.equals("Iterations") ? 5 : (state.regionSplits.equals("Axis") ? 4 : 1)];
			Arrays.fill(distance, Double.MAX_VALUE);

			switch(state.fractal) {
				case "Mandlebrot": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.pow(state.order).add(base);

						// Trap our orbits
						if(state.orbitTraps.equals("Cross")) {
							if(past.r*past.r < distance[it % distance.length]) distance[it % distance.length] = past.r*past.r;
							if(past.i*past.i < distance[it % distance.length]) distance[it % distance.length] = past.i*past.i;
						} else {
							if(past.modulusSquared() < distance[it % distance.length]) distance[it % distance.length] = past.modulusSquared();
						}

						int pointer = state.regionSplits.equals("Iterations") ? (past.i > 0 ? 1 : 0) + (past.r > 0 ? 2 : 0) : it % distance.length;

						if(i == state.iterations - 1 && distance[pointer] < 1) {
							results[i] = Math.sqrt(distance[pointer]) * state.iterations;
							break;
						}
					}
				} break;
				case "Burning Ship": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.abs().pow(state.order).add(base);

						// Trap our orbits
						if(state.orbitTraps.equals("Cross")) {
							if(past.r*past.r < distance[it % distance.length]) distance[it % distance.length] = past.r*past.r;
							if(past.i*past.i < distance[it % distance.length]) distance[it % distance.length] = past.i*past.i;
						} else {
							if(past.modulusSquared() < distance[it % distance.length]) distance[it % distance.length] = past.modulusSquared();
						}

						int pointer = state.regionSplits.equals("Iterations") ? (past.i > 0 ? 1 : 0) + (past.r > 0 ? 2 : 0) : it % distance.length;

						if(i == state.iterations - 1 && distance[pointer] < 1) {
							results[i] = Math.sqrt(distance[pointer]) * state.iterations;
							break;
						}
					}
				} break;
				case "Tricorn": {
					for(int it = 1; it < state.iterations; it++) {
						past = new Complex(past.r, past.i * -1).pow(state.order).add(base);

						// Trap our orbits
						if(state.orbitTraps.equals("Cross")) {
							if(past.r*past.r < distance[it % distance.length]) distance[it % distance.length] = past.r*past.r;
							if(past.i*past.i < distance[it % distance.length]) distance[it % distance.length] = past.i*past.i;
						} else {
							if(past.modulusSquared() < distance[it % distance.length]) distance[it % distance.length] = past.modulusSquared();
						}

						int pointer = state.regionSplits.equals("Iterations") ? (past.i > 0 ? 1 : 0) + (past.r > 0 ? 2 : 0) : it % distance.length;

						if(i == state.iterations - 1 && distance[pointer] < 1) {
							results[i] = Math.sqrt(distance[pointer]) * state.iterations;
							break;
						}
					}
				} break;
				case "Nova": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.subtract(new Complex(1, 0).multiply(past.pow(state.order).subtract(new Complex(1, 0))).divide(new Complex(state.order, 0).multiply(past.pow(state.order - 1)))).add(base);

						// Trap our orbits
						if(state.orbitTraps.equals("Cross")) {
							if(past.r*past.r < distance[it % distance.length]) distance[it % distance.length] = past.r*past.r;
							if(past.i*past.i < distance[it % distance.length]) distance[it % distance.length] = past.i*past.i;
						} else {
							if(past.modulusSquared() < distance[it % distance.length]) distance[it % distance.length] = past.modulusSquared();
						}

						int pointer = state.regionSplits.equals("Iterations") ? (past.i > 0 ? 1 : 0) + (past.r > 0 ? 2 : 0) : it % distance.length;

						if(i == state.iterations - 1 && distance[pointer] < 1) {
							results[i] = Math.sqrt(distance[pointer]) * state.iterations;
							break;
						}
					}
				} break;
				case "Circle": {
					for(int it = 1; it < state.iterations; it++) {
						past = past.pow(state.order);

						// Trap our orbits
						if(state.orbitTraps.equals("Cross")) {
							if(past.r*past.r < distance[it % distance.length]) distance[it % distance.length] = past.r*past.r;
							if(past.i*past.i < distance[it % distance.length]) distance[it % distance.length] = past.i*past.i;
						} else {
							if(past.modulusSquared() < distance[it % distance.length]) distance[it % distance.length] = past.modulusSquared();
						}

						int pointer = state.regionSplits.equals("Iterations") ? (past.i > 0 ? 1 : 0) + (past.r > 0 ? 2 : 0) : it % distance.length;

						if(i == state.iterations - 1 && distance[pointer] < 1) {
							results[i] = Math.sqrt(distance[pointer]) * state.iterations;
							break;
						}
					}
				} break;
				default:
					throw new UnsupportedOperationException(state.fractal + " isn't implemented");
			}
		}
	}

	public void runSmooth() {
		// Save a little time later
		double threshold = state.threshold * state.threshold;

		for(int i = 0; i < size.width * size.height; i++) {
			int x = i % size.width;
			int y = i / size.width;

			// We need a base complex and a past complex for each iteration
			Complex past = new Complex(state.start + (state.end - state.start) * x / size.width, state.top - (state.top - state.bottom) * y / size.height);
			Complex base = state.seed != null ? state.seed : past;

			// Inverse the base value, for pretty images
			if(state.inverse) {base = base.inverse(); past = past.inverse();}

			// For smoothness
			Complex current;

			switch(state.fractal) {
				case "Mandlebrot": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						current = past.pow(state.order).add(base);

						if(current.modulusSquared() > threshold) {
							double k = Math.max((threshold - past.modulusSquared()) / Math.abs((past.modulusSquared() - current.modulusSquared())), 0);
							results[i] = it + k - 1;
							break;
						}

						past = current;
					}
				} break;
				case "Burning Ship": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						current = new Complex(Math.abs(past.r), Math.abs(past.i)).pow(state.order).add(base);

						if(current.modulusSquared() > threshold) {
							double k = Math.max((threshold - past.modulusSquared()) / Math.abs((past.modulusSquared() - current.modulusSquared())), 0);
							results[i] = it + k - 1;
							break;
						}

						past = current;
					}
				} break;
				case "Tricorn": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						current = new Complex(past.r, past.i * -1).pow(state.order).add(base);

						if(current.modulusSquared() > threshold) {
							double k = Math.max((threshold - past.modulusSquared()) / Math.abs((past.modulusSquared() - current.modulusSquared())), 0);
							results[i] = it + k - 1;
							break;
						}

						past = current;
					}
				} break;
				case "Nova": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						current = past.subtract(new Complex(1, 0).multiply(past.pow(state.order).subtract(new Complex(1, 0))).divide(new Complex(state.order, 0).multiply(past.pow(state.order - 1)))).add(base);

						if(current.modulusSquared() > threshold) {
							double k = Math.max((threshold - past.modulusSquared()) / Math.abs((past.modulusSquared() - current.modulusSquared())), 0);
							results[i] = it + k - 1;
							break;
						}

						past = current;
					}
				} break;
				case "Circle": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						current = past.pow(state.order);

						if(current.modulusSquared() > threshold) {
							double k = Math.max((threshold - past.modulusSquared()) / Math.abs((past.modulusSquared() - current.modulusSquared())), 0);
							results[i] = it + k - 1;
							break;
						}

						past = current;
					}
				} break;
				default:
					throw new UnsupportedOperationException(state.fractal + " isn't implemented");
			}
		}
	}

	public void runInteger() {
		// Save a little time later
		double threshold = state.threshold * state.threshold;

		for(int i = 0; i < size.width * size.height; i++) {
			int x = i % size.width;
			int y = i / size.width;

			// We need a base complex and a past complex for each iteration
			Complex past = new Complex(state.start + (state.end - state.start) * x / size.width, state.top - (state.top - state.bottom) * y / size.height);
			Complex base = state.seed != null ? state.seed : past;

			// Inverse the base value, for pretty images
			if(state.inverse) {base = base.inverse(); past = past.inverse();}

			switch(state.fractal) {
				case "Mandlebrot": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						past = past.pow(state.order).add(base);

						if(past.modulusSquared() > threshold) {
							results[i] = it - 1;
							break;
						}
					}
				} break;
				case "Burning Ship": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						past = past.abs().pow(state.order).add(base);

						if(past.modulusSquared() > threshold) {
							results[i] = it - 1;
							break;
						}
					}
				} break;
				case "Tricorn": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						past = new Complex(past.r, past.i * -1).pow(state.order).add(base);

						if(past.modulusSquared() > threshold) {
							results[i] = it - 1;
							break;
						}
					}
				} break;
				case "Nova": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						past = past.subtract(new Complex(1, 0).multiply(past.pow(state.order).subtract(new Complex(1, 0))).divide(new Complex(state.order, 0).multiply(past.pow(state.order - 1)))).add(base);

						if(past.modulusSquared() > threshold) {
							results[i] = it - 1;
							break;
						}
					}
				} break;
				case "Circle": {
					results[i] = -1;
					for(int it = 1; it < state.iterations; it++) {
						past = past.pow(state.order);

						if(past.modulusSquared() > threshold) {
							results[i] = it - 1;
							break;
						}
					}
				} break;
				default:
					throw new UnsupportedOperationException(state.fractal + " isn't implemented");
			}
		}
	}
}
