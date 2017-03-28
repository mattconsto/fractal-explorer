package mc21g14.fractal.colorable;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.security.InvalidParameterException;

import javax.swing.*;

import mc21g14.colorpicker.ColorPicker;
import mc21g14.fractal.misc.UserConfigurable;

/**
 * Very simple coloring. If we never reach the pixel it is one color, otherwise it's another
 * 
 * @author Matthew Consterdine
 */
public class Binary extends Colorable implements Serializable, UserConfigurable {
	protected Color  base;
	protected Color  limit;
	protected JPanel panel;
	
	/**
	 * Initialise Binary coloring with black and white
	 */
	public Binary() {
		this(new Color(0, 0,  0), new Color(255, 255, 255));
    }
	
	/**
	 * Initialise Binary coloring with your chosen colors
	 * @param base Base color
	 * @param limit Limit color
	 */
	public Binary(Color base, Color limit) {
	    this.base  = base;
	    this.limit = limit;
    }
	
	@Override
	public int[] iterationsToRGB(double[] data, int iterations) {
		// We need valid colors
		if(base  == null) throw new InvalidParameterException("Base  cannot be null!");
		if(limit == null) throw new InvalidParameterException("Limit cannot be null!");
		
		int[] results = new int[data.length * 3];
		
		// For each pixel pick the color we want
		for(int i = 0; i < data.length; i++) {
			results[i * 3 + 0] = data[i] < 0 ? limit.getRed()   : base.getRed();
			results[i * 3 + 1] = data[i] < 0 ? limit.getGreen() : base.getGreen();
			results[i * 3 + 2] = data[i] < 0 ? limit.getBlue()  : base.getBlue();
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
					Binary.this.base = base.getColor();
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
					Binary.this.limit = limit.getColor();
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
	    return new Binary(base, limit);
	}
}
