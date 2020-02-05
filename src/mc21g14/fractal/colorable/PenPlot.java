package mc21g14.fractal.colorable;

import java.awt.Color;
import java.awt.GridLayout;
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
public class PenPlot extends Colorable implements Serializable, UserConfigurable {
	private static final long serialVersionUID = 1L;

	protected float  distance;
	protected float  width;
	protected Color  background;
	protected Color  foreground;
	protected JPanel panel;
	
	/**
	 * Initialise Classic coloring with a hue offset of 0
	 */
	public PenPlot() {
		this(2, 0.05f, new Color(255, 255, 255), new Color(0, 0, 0));
	}
	
	/**
	 * Initialise Classic coloring with a specified hue offset
	 * @param distance The distance between lines
	 * @param width The line width
	 * @param background The background color
	 * @param foreground The foreground color
	 */
	public PenPlot(float distance, float width, Color background, Color foreground) {
		this.distance = distance;
		this.width = width;
		this.background = background;
		this.foreground = foreground;
	}
	
	private double getPixel(double[] data, int i, int width, int dx, int dy) {
		int x = i % width + dx;
		int y = (int)(i / width) + dy;
		
		if(x < 0 || x >= width || y < 0 || y >= data.length / width) {return 0;}
		
		return data[x + width * y];
	}
	
	@Override
	public int[] iterationsToRGB(double[] data, double pixelWidth, int iterations) {
		int[] results = new int[3 * data.length];

		for(int i = 0; i < data.length; i++) {
			if(data[i] >= 0 && (
			    getPixel(data, i, (int)pixelWidth, -1, 0) == -1 ||
				getPixel(data, i, (int)pixelWidth, +1, 0) == -1 ||
				getPixel(data, i, (int)pixelWidth, 0, -1) == -1 ||
				getPixel(data, i, (int)pixelWidth, 0, +1) == -1 ||
				getPixel(data, i, (int)pixelWidth, -1, 0) > 1 && (
					getPixel(data, i, (int)pixelWidth, -1, -1) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth, -1,  0) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth, -1, +1) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth,  0, -1) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth,  0, +1) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth, +1, -1) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth, +1,  0) % this.distance < this.width ||
					getPixel(data, i, (int)pixelWidth, +1, +1) % this.distance < this.width
				)
			)) {
				results[3 * i + 0] = foreground.getRed();
				results[3 * i + 1] = foreground.getBlue();
				results[3 * i + 2] = foreground.getGreen();
			} else {
				results[3 * i + 0] = background.getRed();
				results[3 * i + 1] = background.getBlue();
				results[3 * i + 2] = background.getGreen();
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

			JPanel inner = new JPanel(new GridLayout(0, 2, 50, 2));
			inner.setOpaque(false);
			
			inner.add(new JLabel("Distance"));
			final JTextField field = new JTextField(String.format("%.7f", distance));
			field.setHorizontalAlignment(JTextField.RIGHT);
			field.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						// Lets change things up and generate new colors
						distance = (float) Double.parseDouble(field.getText());
						fireActionPerformed();
					} catch(NumberFormatException ignored) {}
				}
			});
			inner.add(field);

			inner.add(new JLabel("Width"));
			final JTextField field2 = new JTextField(String.format("%.7f", width));
			field2.setHorizontalAlignment(JTextField.RIGHT);
			field2.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					try {
						// Lets change things up and generate new colors
						width = (float) Double.parseDouble(field2.getText());
						fireActionPerformed();
					} catch(NumberFormatException ignored) {}
				}
			});
			inner.add(field2);
			panel.add(inner);

			final JPanel colorPanel = new JPanel();
			colorPanel.setLayout(new GridLayout(1, 2, 10, 0));
			colorPanel.setOpaque(false);
			
			JPanel left = new JPanel();
			left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
			left.add(new JLabel("Background"));
			final ColorPicker base = new ColorPicker(this.background);
			base.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					PenPlot.this.background = base.getColor();
					fireActionPerformed();
				}
			});
			left.add(base);
			colorPanel.add(left);

			JPanel right = new JPanel();
			right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
			right.add(new JLabel("Foreground"));
			final ColorPicker limit = new ColorPicker(this.foreground);
			limit.addActionListener(new ActionListener() {
				@Override public void actionPerformed(ActionEvent e) {
					PenPlot.this.foreground = limit.getColor();
					fireActionPerformed();
				}
			});
			right.add(limit);
			colorPanel.add(right);
			
			panel.add(colorPanel);
		}
		return panel;
	}
	
	@Override
	public Colorable clone() {
		return new PenPlot(distance, width, background, foreground);
	}
}
