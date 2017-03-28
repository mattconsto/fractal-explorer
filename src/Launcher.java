import javax.swing.UIManager;

import mc21g14.fractal.misc.FractalExplorer;

/**
 * Main class to start viewing fractals
 * Compile: javac *.java
 * Running: java Launcher
 * 
 * @author Matthew Consterdine
 */
public class Launcher {
	/**
	 * Start the Fractal Explorer
	 * @param args Command line arguments, ignored.
	 */
	public static void main(String[] args) {
		// Java on Windows likes to default to Nimbus which doesn't scale and looks terrible, if for
		// some reason you actually like Nimbus, comment this out, or substitute it with your own.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
			// If this fails we are stuck with Nimbus, sigh...
			System.err.println("Setting SystemLookAndFeel failed.");
		}
		
		// Print out memory usage.
		Runtime rt = Runtime.getRuntime();
	    System.out.println("Memory: " + rt.totalMemory() / 1_000_000 + "MB / " + rt.maxMemory() / 1_000_000  + "MB");
	    
		// Lets go!
		new FractalExplorer();
	}
}
