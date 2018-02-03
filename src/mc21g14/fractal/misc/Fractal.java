package mc21g14.fractal.misc;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import mc21g14.fractal.calculable.*;
import mc21g14.fractal.colorable.*;

/**
 * The fractal class
 * 
 * @author Matthew Consterdine
 */
public class Fractal extends JPanel implements MouseListener, MouseMotionListener, 
	MouseWheelListener, KeyListener, Cloneable
{
	protected FractalState state = new FractalState();
	protected FractalLocation location = new FractalLocation();
	
	protected Complex   selected      = null;
	protected Complex   initial       = null;
	protected Complex   current       = null;
	
	protected Dimension lastSize      = getSize();
	protected double[]  fractalData   = null;
	protected Dimension fractalSize   = new Dimension(0, 0);
	
	protected boolean   keypressReady = true;
	protected boolean   movementReady = true;
	protected boolean   zoomReady     = true;
	
	/**
	 * Create a new Fractal
	 */
	public Fractal() {
		// Add listeners
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);

		// Nice cursor
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

		// Pick our coloring
		state.coloring.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {repaint();}});
	}
	
	/**
	 * Create a new fractal from a given state
	 * @param state The fractal state
	 */
	public Fractal(FractalState state) {
		this();
		this.state = state.clone();
	}

	// Public Getters
	public double       getBottom       () {return location.bottom;}
	public boolean      isBuddha        () {return state.buddha;}
	public Colorable    getColoring     () {return state.coloring;}
	public double       getEnd          () {return location.end;}
	public String       getFractal      () {return state.fractal;}
	public boolean      isInverse       () {return state.inverse;}
	public boolean      isInvert        () {return state.invert;}
	public int          getIterations   () {return state.iterations;}
	public String       getOrbitTraps   () {return state.orbitTraps;}
	public int          getOrder        () {return state.order;}
	public String       getRegionSplits () {return state.regionSplits;}
	public Complex      getSeed         () {return state.seed;}
	public Complex      getSelected     () {return selected;}
	public boolean      isSmooth        () {return state.smooth;}
	public double       getStart        () {return location.start;}
	public FractalState getState        () {return state;}
	public double       getTop          () {return location.top;}
	public double       getThreshold    () {return state.threshold;}

	// Public Setters
	public void setBottom (double  b) {location.bottom = b; regenerate();}
	public void setBuddha (boolean b) {state = state.setBuddha(b); regenerate();}
	
	public void setColoring(Colorable c) {
		state = state.setColoring(c);
		// A new listener is required.
		c.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {repaint();}
		});
		repaint();
	}
	
	public void setEnd           (double  e) {location.end = e; regenerate();}
	public void setFractal       (String  f) {state = state.setFractal(f); regenerate();}
	public void setInverse       (boolean i) {state = state.setInverse(i); regenerate();}
	public void setInvert        (boolean i) {state = state.setInvert(i); repaint();   }
	public void setIterations    (int     i) {state = state.setIterations(i); regenerate();}
	public void setOrbitTraps    (String  t) {state = state.setOrbitTraps(t); regenerate();}
	public void setOrder         (int     o) {state = state.setOrder(0); regenerate();}
	public void setRegionSplits  (String  s) {state = state.setRegionSplits(s); regenerate();}
	public void setSeed          (Complex s) {state = state.setSeed(s); regenerate();}
	public void setSelected      (Complex s) {selected = s; repaint();   }
	public void setSmooth        (boolean s) {state = state.setSmooth(s); regenerate();}
	public void setStart         (double  s) {location.start = s; regenerate();}

	public void setState(FractalState s) {
		// We need to clone the state so it isn't shared. If you want it shared, set it.
		this.state = s.clone();
		setColoring(s.coloring);
		regenerate();
	}
	
	public void setTop           (double  t) {location.top = t; regenerate();}
	public void setThreshold     (double  t) {state = state.setThreshold(t); regenerate();}
	
	/**
	 * Get a complex from an x, y point
	 * @param p The point
	 * @param f The fractal
	 * @return The Complex
	 */
	public static Complex getComplexFromPoint(Point p, Fractal f) {
		return new Complex(
			f.location.start + (f.location.end - f.location.start)  * p.getX() / f.getWidth(),
			f.location.top   - (f.location.top - f.location.bottom) * p.getY() / f.getHeight()
		);
	}

	/**
	 * Get a Point from a complex
	 * @param c The complex
	 * @param f The fractal
	 * @return The point
	 */
	public static Point getPointFromComplex(Complex c, Fractal f) {
		return new Point(
			(int) (f.getWidth()  * (c.r - f.location.start) / (f.location.end    - f.location.start)),
			(int) (f.getHeight() * (c.i - f.location.top)   / (f.location.bottom - f.location.top))
		);
	}
	
	/**
	 * Regenerate the fractal.
	 */
	public void regenerate() {
		System.out.println("state: " + state.hashCode());
		// If we have a size > 0
		if(getWidth() > 0 && getHeight() > 0) {
			// Render in a new thread
			Calculable.calcAsync(state, location, getSize(), new Callback() {
				@Override public void callback(double[] data, Dimension size) {
					fractalSize = size;
					fractalData = data;

					zoomReady     = true;
					movementReady = true;
					
					repaint();
				}
			});
			
			// Update our listeners
			fireActionPerformed();
			
			lastSize = getSize();
		}
	}
	
	/**
	 * Zoom into/out of the centre of the fractal
	 * @param amount The amount we want to zoom
	 */
	public void zoom(double amount) {
		double width  = location.end - location.start;
		double height = location.top - location.bottom;
		
		Point a = getPointFromComplex(new Complex(
			location.start  - 0.1 * width  * amount,
			location.top    + 0.1 * height * amount
		), this);
		Point b = getPointFromComplex(new Complex(
			location.end    + 0.1 * width  * amount,
			location.bottom - 0.1 * height * amount
		), this);
		
		location.start  -= 0.1 * width  * amount;
		location.end    += 0.1 * width  * amount;
		location.top    += 0.1 * height * amount;
		location.bottom -= 0.1 * height * amount;
		
		// Fake zoom in, to make it look more responsive.
		if(zoomReady && fractalData != null) {
			zoomReady = false;
			fractalData = sectionOf(
				fractalData,
				fractalSize.width,
				new Rectangle(
					Math.min(a.x,  b.x), Math.min(a.y,  b.y), 
					Math.abs(b.x - a.x), Math.abs(b.y - a.y))
			);
			fractalSize = new Dimension(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
			
			regenerate();
		}
		
		repaint();
	}
	
	public void move(Complex complex) {
		double width  = location.end - location.start;
		double height = location.top - location.bottom;
		
		Point a = getPointFromComplex(new Complex(
			location.start  - width  * complex.r * 0.1,
			location.top    + height * complex.i * 0.1
		), this);
		Point b = getPointFromComplex(new Complex(
			location.end    - width  * complex.r * 0.1,
			location.bottom + height * complex.i * 0.1
		), this);
		
		location.end    -= width  * complex.r * 0.1;
		location.start  -= width  * complex.r * 0.1;
		location.bottom += height * complex.i * 0.1;
		location.top    += height * complex.i * 0.1;

		// Fake move, to make it look more responsive.
		if(movementReady && fractalData != null) {
			movementReady = false;
			
			fractalData = sectionOf(
				fractalData,
				fractalSize.width,
				new Rectangle(
					Math.min(a.x,  b.x), Math.min(a.y,  b.y), 
					Math.abs(b.x - a.x), Math.abs(b.y - a.y))
			);
			fractalSize = new Dimension(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
			
			regenerate();
		}
		
		repaint();
	}
	
	/**
	 * Invert an int array
	 * @param input The int array
	 * @param max The max
	 * @return Inverted in array
	 */
	public static int[] invertArray(int[] input, int max) {
		int[] array = new int[input.length];
		for(int i = 0; i < array.length; i++) array[i] = max - input[i];
		return array;
	}
	
	/**
	 * Get a section of data
	 * @param input The data
	 * @param width The original width
	 * @param bounds The new bounds
	 * @return Modified data
	 */
	public static double[] sectionOf(double[] input, int width, Rectangle bounds) {
		// Only need an array that is the size of our bounds, we can translate coords.
		double[] array = new double[bounds.width * bounds.height];
		
		// Loop through each pixel that is inside the bounds, copy it to the new array,
		// Math.max/max are a nice way to ensure that we don't go outside the input array.
		for(int x = Math.max(bounds.x, 0); x < Math.min(bounds.x + bounds.width, width); x++) {
			for(int y = Math.max(bounds.y, 0); y < Math.min(bounds.y + bounds.height, input.length / width); y++) {
				array[(x - bounds.x) + (y - bounds.y) * bounds.width] = input[x + y * width];
			}
		}
		
		return array;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// We want anti-aliasing and fancy stuff
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// If the size has changed, calculate
		if(lastSize == null || !getSize().equals(lastSize)) regenerate();
		
		// Ensure we have a fractal
		while(fractalData == null) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ignored) {}
		}
		
		// draw it
		if(fractalSize.width <= 0 || fractalSize.height <= 0) return;
		
		BufferedImage image = new BufferedImage(
			(int) fractalSize.getWidth(),
			(int) fractalSize.getHeight(),
			BufferedImage.TYPE_INT_RGB
		);
		image.getRaster().setPixels(
			0, 0,
			(int) fractalSize.getWidth(),
			(int) fractalSize.getHeight(),
			state.invert
				? invertArray(state.coloring.iterationsToRGB(fractalData, state.iterations), 255)
				: state.coloring.iterationsToRGB(fractalData, state.iterations));
		g2d.drawImage(image,
			0, 0, getWidth(),       getHeight(),
			0, 0, image.getWidth(), image.getHeight(),
			null
		);
		
		// Draw selection
		if(initial != null && current != null) {
			Point a = getPointFromComplex(initial, this);
			Point b = getPointFromComplex(current, this);
			Rectangle rectangle = new Rectangle(Math.min(a.x,  b.x), Math.min(a.y,  b.y),
				                                  Math.abs(a.x - b.x), Math.abs(a.y - b.y));
			g2d.setColor(getForeground());
			g2d.fill(new BasicStroke(2).createStrokedShape(rectangle));
			g2d.setColor(getBackground());
			g2d.draw(rectangle);
		}
			
		// Draw complex
		if(selected != null && getWidth() > 200 & getHeight() > 200) {
			// Draw stroked complex number
			g2d.setFont(g2d.getFont().deriveFont(30f));
			Shape number = g2d.getFont().createGlyphVector(
				               g2d.getFontRenderContext(), selected.toString()
				           ).getOutline();
			g2d.translate(g2d.getFont().getSize(), getHeight() - g2d.getFont().getSize());
			g2d.setColor(getForeground());
			g2d.draw(new BasicStroke(1).createStrokedShape(number));
			g2d.setColor(getBackground());
			g2d.fill(number);
			g2d.translate(- g2d.getFont().getSize(), g2d.getFont().getSize() - getHeight());

			// Draw stroked point
			Point a = getPointFromComplex(selected, this);
			int length = g2d.getFont().getSize() / 2;
			g2d.setColor(getForeground());
			g2d.setStroke(new BasicStroke(5));
			g2d.drawLine(a.x - length, a.y, a.x + length, a.y);
			g2d.drawLine(a.x, a.y - length, a.x, a.y + length);
			g2d.setColor(getBackground());
			g2d.setStroke(new BasicStroke(3));
			g2d.drawLine(a.x - length, a.y, a.x + length, a.y);
			g2d.drawLine(a.x, a.y - length, a.x, a.y + length);
		}
		
		lastSize = getSize();
	}
	
	@Override
	public Dimension getPreferredSize() {
		// Not strictly necessary, but nice to have a default size
		return new Dimension(100, 100);
	}

	@Override
	public boolean isFocusable() {
		// Required for keypresses
		return true;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(initial == null) current = initial = getComplexFromPoint(e.getPoint(), this);
		
		// Maths to ensure that the current point always makes a rectangle with the ratio.
		double ratio = 1.0 * Math.max(
			getWidth(), getHeight()) / Math.min(getWidth(),
			getHeight()
		);
		Point  point = getPointFromComplex(initial, this);

		// Need to handle both orientations
		if(getWidth() > getHeight()) {
			int max = Math.max(
				(int) (Math.abs(e.getX() - point.x) / ratio),
				(int) (Math.abs(e.getY() - point.y))
			);
			current = getComplexFromPoint(new Point(
				(int) (point.x + (e.getX() > point.x ? 1 : -1) * max * ratio),
				(int) (point.y + (e.getY() > point.y ? 1 : -1) * max)
			), this);
		} else {
			int max = Math.max(
				(int) (Math.abs(e.getX() - point.x)),
				(int) (Math.abs(e.getY() - point.y) / ratio)
			);
			current = getComplexFromPoint(new Point(
				(int) (point.x + (e.getX() > point.x ? 1 : -1) * max),
				(int) (point.y + (e.getY() > point.y ? 1 : -1) * max * ratio)
			), this);
		}

		repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// Lets us select a fractal just by hovering over it
		requestFocus();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(initial != null && current != null) {
			Point a = getPointFromComplex(initial, this);
			Point b = getPointFromComplex(current, this);
			if(Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2)) > 10) {
				location.start  = Math.min(initial.r, current.r);
				location.end    = Math.max(initial.r, current.r);
				location.top    = Math.min(initial.i, current.i);
				location.bottom = Math.max(initial.i, current.i);
				
				// Fake zoom in, to make it look more responsive.
				if(fractalData != null) {
					fractalData = sectionOf(
						fractalData,
						fractalSize.width,
						new Rectangle(
							Math.min(a.x,  b.x), Math.min(a.y,  b.y), 
							Math.abs(b.x - a.x), Math.abs(b.y - a.y))
					);
					fractalSize = new Dimension(Math.abs(b.x - a.x), Math.abs(b.y - a.y));
					
					regenerate();
				}
			} else {
				selected = getComplexFromPoint(e.getPoint(), this);
				fireActionPerformed();
			}
		} else {
			selected = getComplexFromPoint(e.getPoint(), this);
			fireActionPerformed();
		}
		
		// We no longer want to draw the rectangle
		initial = null;
		current = null;
		
		repaint();
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		zoom(e.getWheelRotation()); // Generally equals 1/-1
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// We want to only handle the first keyPress event, as the Fractal takes time to re-render.
		// This is generally more responsive than using keyClicked(KeyEvent e)
		if(!keypressReady) return;
		keypressReady = false;
		
		switch (e.getKeyCode()) {
			case 37: case 74: case 65: move(new Complex(1,  0)); break; // Left
			case 38: case 73: case 87: move(new Complex(0,  1)); break; // Up
			case 39: case 76: case 68: move(new Complex(-1, 0)); break; // Right
			case 40: case 75: case 83: move(new Complex(0, -1)); break; // Down
			case 27: case 36: // Esc, Home. Restores defaults
				location.start   = -2;
				location.end     = 2;
				location.top     = -1.6;
				location.bottom  = 1.6;
				state = state.setSeed(null);
				regenerate();
				break;
			case 45: zoom(1);  break; // Out
			case 61: zoom(-1); break; // In
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// Let us type a new key
		keypressReady = true;
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
		ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Fractal Change");

		// Loop through the Listener list, firing actions when we can
		for(int i = list.length - 2; i >= 0; i -= 2) {
			if(list[i] == ActionListener.class) ((ActionListener) list[i + 1]).actionPerformed(event);
		}
	}

	// Stubs
	@Override public void keyTyped    (KeyEvent e)   {}
	@Override public void mouseClicked(MouseEvent e) {}
	@Override public void mouseExited (MouseEvent e) {}
	@Override public void mouseMoved  (MouseEvent e) {}
	@Override public void mousePressed(MouseEvent e) {}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Fractal");
		frame.add(new Fractal());
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
