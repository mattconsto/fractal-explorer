package mc21g14.fractal.misc;

import javax.swing.JPanel;

/**
 * Interface for classes that can allow swing user interaction
 * 
 * @author Matthew Consterdine
 */
public interface UserConfigurable {
	/**
	 * Get a JPanel full of controls
	 * @return JPanel full of controls
	 */
	public JPanel getSettingsPanel();
}
