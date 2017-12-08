package mc21g14.fractal.colorable;

import java.awt.event.*;
import java.io.Serializable;

import javax.swing.event.EventListenerList;

/**
 * Abstract for classes capable of converting iterations to rgb
 * 
 * @author Matthew Consterdine
 */
public abstract class Colorable implements Cloneable, Serializable {
	protected EventListenerList listenerList = new EventListenerList();
	
	/**
	 * Convert a double array containing a series of iterations to rgb values
	 * @param data data
	 * @param iterations Maximum iterations reached
	 * @return RGB formatted int array
	 */
	public abstract int[] iterationsToRGB(double[] data, int iterations);
	
	/**
	 * List colorable implementations
	 * @return List of colorable implementation
	 */
	public static Colorable[] getImplementations() {
		return new Colorable[] {
			new Binary(), new Fire(), new Gradient(), new HueShift(), new Stepped(), new Ultra()
		};
	}
	
	/**
	 * Add an ActionListener to the Fractals internal list
	 * @param listener The Listener
	 */
	public synchronized void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}
	
	/**
	 * Remove an ActionListener from the Fractals internal list
	 * @param listener The Listener
	 */
	public synchronized void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}
	
	/**
	 * Remove all listeners
	 */
	public synchronized void removeAllListeners() {
		listenerList = new EventListenerList();
	}
	
	/**
	 * Fired whenever the fractal changes
	 */
	protected void fireActionPerformed() {
		Object[]    list  = listenerList.getListenerList();
		ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Color Change");
		
		// Loop through the Listener list, firing actions when we can
		for(int i = list.length - 2; i >= 0; i -= 2) {
			if(list[i] == ActionListener.class)
				((ActionListener) list[i + 1]).actionPerformed(event);
		}
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	@Override public abstract Colorable clone();
}
