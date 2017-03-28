package mc21g14.fractal.colorable;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;

import java.security.InvalidParameterException;

import mc21g14.colorpicker.ColorPicker;
import mc21g14.fractal.misc.UserConfigurable;

/**
 * Linear Coloring between a set base and limit
 * 
 * @author Matthew Consterdine
 */
public class Gradient extends Colorable implements Serializable, UserConfigurable {
	protected Color  base;
	protected Color  limit;
	protected JPanel panel;
	
	/**
	 * Create a new gradient between red and white
	 */
	public Gradient() {
		this(new Color(250, 10,  10), new Color(255, 255, 255));
    }
	
	/**
	 * Create a new gradient between two colors
	 * @param base The base color
	 * @param limit The limit color
	 */
	public Gradient(Color base, Color limit) {
	    this.base  = base;
	    this.limit = limit;
    }

	@Override
    public int[] iterationsToRGB(double[] data, int iterations) {
		// We need valid colors
		if(base  == null) throw new InvalidParameterException("Base  cannot be null!");
		if(limit == null) throw new InvalidParameterException("Limit cannot be null!");
		
		int[] results = new int[3 * data.length];
		
		// Calculate deltas to save a small amount of calculation
		int[] deltas  = new int[] {
			limit.getRed()   - base.getRed(),
			limit.getGreen() - base.getGreen(),
			limit.getBlue()  - base.getBlue()
		};
		
    	for(int i = 0; i < data.length; i++) {
    		if(data[i] >= 0) {
        		// If pixel reached
    			double value = Math.min(data[i] / iterations, 1);
    			
    			// Interpolate between the two colors
        		results[3 * i + 0] = (int) (base.getRed()   + value * deltas[0]);
        		results[3 * i + 1] = (int) (base.getGreen() + value * deltas[1]);
        		results[3 * i + 2] = (int) (base.getBlue()  + value * deltas[2]);
    		} else {
    			// Fallback
        		results[3 * i + 0] = limit.getRed();
        		results[3 * i + 1] = limit.getGreen();
        		results[3 * i + 2] = limit.getRed();
    		}
    	}
    	
    	return results;
    }

	@Override
	public JPanel getSettingsPanel() {
		if(panel == null) {
			// Construct a panel for the user
			panel = new JPanel();
			panel.setLayout(new GridLayout(1, 2, 10, 0));
			panel.setOpaque(false);
			
			JPanel left = new JPanel();
			left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
			left.add(new JLabel("Base Color"));
			final ColorPicker base = new ColorPicker(this.base);
			base.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					Gradient.this.base = base.getColor();
					fireActionPerformed();
				}
			});
			left.add(base);
			panel.add(left);

			JPanel right = new JPanel();
			right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
			right.add(new JLabel("Limit Color"));
			final ColorPicker limit = new ColorPicker(this.limit);
			limit.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					Gradient.this.limit = limit.getColor();
					fireActionPerformed();
				}
			});
			right.add(limit);
			panel.add(right);
		}
		return panel;
	}
	
	@Override
	public Colorable clone() {
	    return new Gradient(base, limit);
	}
}
