package mc21g14.fractal.colorable;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mc21g14.fractal.misc.UserConfigurable;

/**
 * Coloring similar to that of UltraFractal
 * 
 * @author Matthew Consterdine
 */
public class Ultra extends Colorable implements UserConfigurable {
	private static final long serialVersionUID = 1L;

	protected double stretch;
	protected JPanel panel = null;
	protected ArrayList<Color> colors = new ArrayList<Color>();
	
	/**
	 * Create Ultra with a stretch value
	 * @param stretch Stretch value
	 */
	public Ultra(double stretch) {
		// Seed our colours with ones similar to ultrafractal
		for(Color c : new Color[]{
			new Color(  0,   7, 110),
			new Color( 32, 107, 203),
			new Color(237, 255, 255),
			new Color(255, 170,   0),
			new Color(  0,   2,   0)
		}) colors.add(c);
		
		this.stretch = stretch;
	}
	
	/**
	 * Create Ultra with a stretch value of 10
	 */
	public Ultra() {
		this(10);
	}
	
	@Override
	public int[] iterationsToRGB(double[] data, double pixelWidth, int iterations) {
		int[] results = new int[3 * data.length];
		
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
			inner.add(new JLabel("Color Stretch"));
			final JTextField field = new JTextField(Double.toString(stretch));
			field.setHorizontalAlignment(JTextField.RIGHT);
			field.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						// Lets change things up and generate new colors
						stretch = Double.parseDouble(field.getText());
						fireActionPerformed();
					} catch(NumberFormatException ignored) {}
				}
			});
			inner.add(field);
			
			panel.add(inner);
			// Prevent some resizing
			panel.add(Box.createVerticalStrut(94));
		}
		return panel;
	}

	@Override
	public Colorable clone() {
		return new Ultra(stretch);
	}

}
