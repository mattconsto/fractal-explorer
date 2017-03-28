package mc21g14.fractal.colorable;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;

import java.security.InvalidParameterException;

import mc21g14.colorpicker.ColorPicker;
import mc21g14.fractal.misc.UserConfigurable;

/**
 * Fire/Electric coloring
 * 
 * @author Matthew Consterdine
 */
public class Fire extends Colorable implements Serializable, UserConfigurable {
	protected Color  base;
	protected JPanel panel;
	
	/**
	 * Create a new red fire
	 */
	public Fire() {
		this(new Color(250, 10,  10));
    }
	
	/**
	 * Create a fire with a set color
	 * @param base The color
	 */
	public Fire(Color base) {
	    this.base  = base;
    }

	@Override
    public int[] iterationsToRGB(double[] data, int iterations) {
		// We need valid colors
		if(base  == null) throw new InvalidParameterException("Base  cannot be null!");
		
		int[] results = new int[3 * data.length];
		
    	for(int i = 0; i < data.length; i++) {
    		if(data[i] >= 0) {
        		// If pixel reached
    			double value = Math.min(data[i] / iterations, 1);
    			
    			// Fade between the color and black
        		results[3 * i + 0] = (int) (value * base.getRed());
        		results[3 * i + 1] = (int) (value * base.getGreen());
        		results[3 * i + 2] = (int) (value * base.getBlue());
    		} else {
    			// Fallback
        		results[3 * i + 0] = 0;
        		results[3 * i + 1] = 0;
        		results[3 * i + 2] = 0;
    		}
    	}
    	
    	return results;
    }

	@Override
	public JPanel getSettingsPanel() {
		if(panel == null) {
			// Construct a panel for the user
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setOpaque(false);
			
			panel.add(new JLabel("Base Color"));
			final ColorPicker base = new ColorPicker(this.base);
			base.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					Fire.this.base = base.getColor();
					fireActionPerformed();
				}
			});
			panel.add(base);
		}
		return panel;
	}
	
	@Override
	public Colorable clone() {
	    return new Fire(base);
	}
}