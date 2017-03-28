package mc21g14.fractal.colorable;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;

import javax.swing.*;

import mc21g14.colorpicker.ColorPicker;
import mc21g14.fractal.misc.UserConfigurable;

/**
 * Classic Mandlebrot coloring, going through the spectrum
 * 
 * @author Matthew Consterdine
 */
public class HueShift extends Colorable implements Serializable, UserConfigurable {
	protected float  hue;
	protected JPanel panel;
	
	/**
	 * Initialise Classic coloring with a hue offset of 0
	 */
	public HueShift() {
		this(0);
    }
	
	/**
	 * Initialise Classic coloring with a specified hue offset
	 * @param hue The hue offset
	 */
	public HueShift(float hue) {
		this.hue = hue;
	}
	
	@Override
    public int[] iterationsToRGB(double[] data, int iterations) {
		int[] results = new int[3 * data.length];
		
    	for(int i = 0; i < data.length; i++) {
    		if(data[i] >= 0) {
        		// Using HSB color as it allows us to easily change hue, unlike RGB
    			int rgb = Color.HSBtoRGB((float) (hue + data[i] / iterations), 1f, 1f);
    			// Bitshift the RGB values as we need to produce an RGB array
        		results[3 * i + 0] = (rgb >> 16) & 0xFF;
        		results[3 * i + 1] = (rgb >>  8) & 0xFF;
        		results[3 * i + 2] = (rgb >>  0) & 0xFF;
    		} else {
    			// Fallback
        		results[3 * i + 0] = results[3 * i + 1] = results[3 * i + 2] = 0;
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
			
			panel.add(new JLabel("Starting Hue"));
			final ColorPicker hue = new ColorPicker(this.hue, 1f, 1f);
			hue.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					HueShift.this.hue = hue.hue;
					fireActionPerformed();
				}
			});
			panel.add(hue);
		}
		return panel;
	}
	
	@Override
	public Colorable clone() {
	    return new HueShift(hue);
	}
}
