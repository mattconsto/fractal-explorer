package mc21g14.colorpicker;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Custom Swing ColorPicker
 * 
 * @author Matthew Consterdine
 */
public class ColorPicker extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;

	public float hue = 0;
	public float sat = 1;
	public float bri = 1;
	
	protected int width = 20;
	
	/**
	 * Create a new ColorPicker with default values
	 */
	public ColorPicker() {
		addMouseListener(this);
		addMouseMotionListener(this);
		setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		setBorder(new MatteBorder(3, 3, 3, 3, Color.BLACK));
	}
	
	/**
	 * Create a ColorPicker with set values
	 * @param hue Hue
	 * @param sat Saturation
	 * @param bri Brightness
	 */
	public ColorPicker(float hue, float sat, float bri) {
		this();
		this.hue = hue;
		this.sat = sat;
		this.bri = bri;
	}
	
	/**
	 * Create a ColorPicker from a HSB array
	 * @param hsb HSB array
	 */
	public ColorPicker(float[] hsb) {
		this(hsb[0], hsb[1], hsb[2]);
	}
	
	/**
	 * Create a ColorPicker from a Color object
	 * @param color The Color
	 */
	public ColorPicker(Color color) {
		this(Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null));
	}
	
	/**
	 * Get the current Color
	 * @return A Color object
	 */
	public Color getColor() {
		return Color.getHSBColor(hue, sat, bri);
	}

	/**
	 * Fire an interaction at the ColorPicker
	 * @param p The point on the screen we want to interact with
	 */
	protected void interact(Point p) {
		int diameter = (int) (Math.min(getWidth(), getHeight()) * 0.9);

		// Create a Circle Object for us to check within.
		Ellipse2D circle = new Ellipse2D.Double(
			(getWidth()  - diameter) / 2,
			(getHeight() - diameter) / 2,
			diameter, diameter);
		
		// If we click inside the circle we change hue + sat, outside we change bri.
		if(circle.contains(p)) {
			hue = limit((float) (
				Math.atan2(
					p.getX() - getWidth()  / 2.0,
					p.getY() - getHeight() / 2.0
				) / Math.PI / 2.0 + 0.5
			), 0, 1);
			sat = limit((float) (
				Math.sqrt(
					Math.pow((p.getX() - getWidth()  / 2.0) / diameter * 2, 2) + 
					Math.pow((p.getY() - getHeight() / 2.0) / diameter * 2, 2)
				)
			), 0, 1);
		} else {
			bri = limit((float) (1.0 - p.getY() / getHeight()), 0, 1);
		}
		
		// Tell everyone something has happened.
		fireActionPerformed();
		
		repaint();
	}
	
	/**
	 * Little helper function to limit a value to a range
	 * @param value Value
	 * @param min Minimum
	 * @param max Maximum
	 * @return A number between and including Minimum and Maximum
	 */
	protected static float limit(float value, float min, float max) {
		return value > min ? (value < max ? value : max) : min;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Find the biggest circle we can fit within the window, minus 10%
		int diameter = (int) (Math.min(getWidth(), getHeight()) * 0.9);
		
		// Create our shapes
		Ellipse2D circle	= new Ellipse2D.Double(
			(getWidth()  - diameter) / 2,
			(getHeight() - diameter) / 2,
			diameter, diameter);
		Ellipse2D selection = new Ellipse2D.Double(
			(getWidth()  - width)/2.0 + sat*diameter/2.0*Math.cos((1.0 - hue - 0.25)*Math.PI*2),
			(getHeight() - width)/2.0 + sat*diameter/2.0*Math.sin((1.0 - hue - 0.25)*Math.PI*2),
			width, width);
		
		// Graphics2D > Graphics
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setStroke(new BasicStroke(3f));

		// Fill background
		g2d.setPaint(new GradientPaint(new Point(0, 0),		   Color.getHSBColor(hue, sat, 1),
									   new Point(0, getHeight()), Color.getHSBColor(hue, sat, 0)));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		// Draw bri indicator
		g2d.setColor(getForeground());
		g2d.drawLine(0,		  (int) ((1.0 - bri) * getHeight()),
					 getWidth(), (int) ((1.0 - bri) * getHeight()));
		
		// Fill circle
		g2d.setPaint(new ColorWheelPaint(bri, (getWidth()-diameter)/2, (getHeight()-diameter)/2));
		g2d.fill(circle);
		
		// Draw circle
		g2d.setColor(getForeground());
		g2d.draw(circle);
		
		// Fill selection
		g2d.setColor(getColor());
		g2d.fill(selection);

		// Draw selection
		g2d.setColor(getForeground());
		g2d.draw(selection);
	}
	
	@Override
	public Dimension getPreferredSize() {
		// We need defaults so it actually takes up space
		return new Dimension(100, 100);
	}
	
	@Override
	public String toString() {
		return String.format("[%.3f, %.3f, %.3f]", hue, sat, bri);
	}
	
	// Interaction
	@Override public void mouseDragged (MouseEvent e) {interact(e.getPoint());}
	@Override public void mousePressed (MouseEvent e) {interact(e.getPoint());}

	/**
	 * Add an ActionListener to the ColorPickers internal list
	 * @param listener The Listener
	 */
	public synchronized void addActionListener(ActionListener listener) {
		listenerList.add(ActionListener.class, listener);
	}
	
	/**
	 * Remove an ActionListener from the ColorPickers internal list
	 * @param listener The Listener
	 */
	public synchronized void removeActionListener(ActionListener listener) {
		listenerList.remove(ActionListener.class, listener);
	}
	
	/**
	 * Fired whenever the color changes
	 */
	protected void fireActionPerformed() {
		Object[]	list  = listenerList.getListenerList();
		ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, toString());
		
		// Loop through the Listener list, firing actions when we can
		for(int i = list.length - 2; i >= 0; i -= 2) {
			if(list[i] == ActionListener.class)
				((ActionListener) list[i + 1]).actionPerformed(event);
		}
	}

	/**
	 * Create a JFrame containing one ColorPickerPanel. Useful for development
	 * @param args Command line arguments
	 */
	public static void main(String[] args) {
		// Create a picker
		final ColorPicker picker = new ColorPicker(0.5f, 0.5f, 0.5f);
		picker.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.out.println(picker.getColor());
			}
		});
		
		// Create our frame
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.add(picker);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	// Stubs
	@Override public void mouseMoved   (MouseEvent e) {}
	@Override public void mouseClicked (MouseEvent e) {}
	@Override public void mouseEntered (MouseEvent e) {}
	@Override public void mouseExited  (MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
}
