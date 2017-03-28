package mc21g14.fractal.colorable;

import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.*;

import javax.swing.*;

import mc21g14.fractal.misc.UserConfigurable;

/**
 * Coloring where each step has a color
 * 
 * @author Matthew Consterdine
 */
public class Stepped extends Colorable implements Serializable, UserConfigurable {
	protected int    maximum;
	protected double stretch;
	protected JPanel panel = null;
	protected ArrayList<Color> colors = new ArrayList<Color>();
	
	/**
	 * Create a new Stepped coloring with 4 colors and a strech value of 10
	 */
	public Stepped() {
		this(4, 10);
	}
	
	/**
	 * Create a new Stepped coloring with specified number of colors and stretch
	 * @param maximum The number of colors
	 * @param stretch The stretch value
	 */
	public Stepped(int maximum, double stretch) {
		this.maximum = maximum;
		this.stretch = stretch;
    }
	
	@Override
    public int[] iterationsToRGB(double[] data, int iterations) {
		int[] results = new int[3 * data.length];

		// We need to fill the colors array if we don't have enough
		Random r = new Random();
		while(colors.size() <= maximum)
			colors.add(new Color(r.nextFloat(), r.nextFloat(), r.nextFloat()));
		
    	for(int i = 0; i < data.length; i++) {
    		if(data[i] >= 0 && colors.get((int) data[i] % colors.size()) != null) {
        		// If pixel reached
    			double mod    = (data[i] % stretch) / stretch;
    			Color  colorA = colors.get((int) (data[i] / stretch    ) % colors.size());
    			Color  colorB = colors.get((int) (data[i] / stretch + 1) % colors.size());
    			
    			// Interpolate between the two colors
        		results[3 * i + 0] = (int) ((1 - mod) * colorA.getRed()   + mod * colorB.getRed());
        		results[3 * i + 1] = (int) ((1 - mod) * colorA.getGreen() + mod * colorB.getGreen());
        		results[3 * i + 2] = (int) ((1 - mod) * colorA.getBlue()  + mod * colorB.getBlue());
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
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setOpaque(false);

			JPanel inner = new JPanel(new GridLayout(0, 2, 50, 2));
			inner.setOpaque(false);

			inner.add(new JLabel("Number of colors"));
			final JTextField maximum = new JTextField(Integer.toString(this.maximum));
			maximum.setHorizontalAlignment(JTextField.RIGHT);
			maximum.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						// Lets change things up and generate new colors
						Stepped.this.maximum = Integer.parseInt(maximum.getText());
						colors.clear();
						fireActionPerformed();
					} catch(NumberFormatException ignored) {}
				}
			});
			inner.add(maximum);
			
			inner.add(new JLabel("Color Stretch"));
			final JTextField stretch = new JTextField(Double.toString(this.stretch));
			stretch.setHorizontalAlignment(JTextField.RIGHT);
			stretch.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						// Lets change things up and generate new colors
						Stepped.this.stretch = Double.parseDouble(stretch.getText());
						fireActionPerformed();
					} catch(NumberFormatException ignored) {}
				}
			});
			inner.add(stretch);
			
			panel.add(inner);
			// Prevent some resizing
			panel.add(Box.createVerticalStrut(72));
		}
		return panel;
	}
	
	@SuppressWarnings("unchecked")
    @Override
	public Colorable clone() {
		Stepped temp = new Stepped(maximum, stretch);
		temp.colors  = (ArrayList<Color>) this.colors.clone(); // Unchecked cast
	    return temp;
	}
}
