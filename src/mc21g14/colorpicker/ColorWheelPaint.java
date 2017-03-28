package mc21g14.colorpicker;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * Paints a ColourWheel, designed to be used *only* with ColorPickerPanel.
 * 
 * @author Matthew Consterdine
 */
public class ColorWheelPaint implements Paint {
	protected float bri;
	protected int   offsetx;
	protected int   offsety;
	
	/**
	 * Create a new Color Wheel Paint
	 * @param bri Brightness
	 * @param offsetx Offset X
	 * @param offsety Offset Y
	 */
	public ColorWheelPaint(float bri, int offsetx, int offsety) {
		this.bri     = bri;
		this.offsetx = offsetx;
		this.offsety = offsety;
	}
	
	@Override
	public int getTransparency() {
		// Transparency doesn't make sense for this picker
		return 0;
	}

	@Override
	public PaintContext createContext(ColorModel model, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		// Lets make ourselves a context
		return new ColorWheelPaintContext(model, userBounds, xform, bri, offsetx, offsety);
	}
}
