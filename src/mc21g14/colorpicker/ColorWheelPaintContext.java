package mc21g14.colorpicker;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Class to create the custom Paint Context, used in the Color Picker
 * 
 * @author Matthew Consterdine
 */
class ColorWheelPaintContext implements PaintContext {
	protected ColorModel      model;
	protected Rectangle2D     bounds;
	protected AffineTransform xform;
	protected float           bri;
	protected int             offsetx;
	protected int             offsety;
	
	public ColorWheelPaintContext(ColorModel model, Rectangle2D bounds, AffineTransform xform, float bri, int offsetx, int offsety) {
		this.model   = model;
		this.bounds  = bounds;
		this.xform   = xform;
		this.bri     = bri;
		this.offsetx = offsetx;
		this.offsety = offsety;
	}
	
	@Override
	public void dispose() {
		// Do nothing
	}

	@Override
	public ColorModel getColorModel() {
		// Only reason we care about the color model.
		return model;
	}

	@Override
	public Raster getRaster(int x, int y, int w, int h) {
		// On initialisation, the x and y values given are relative to the window, not the panel
		// As a result we need to subtract the values in the AffineTransform
		x = x - (int) xform.getTranslateX();
		y = y - (int) xform.getTranslateY();
		
		// Setup
		WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
		int bits = getColorModel().getNumComponents();
		float[] values = new float[w * h * bits];
		
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				// We need to calculate the hue and saturation values for each pixel
				float hue = (float) (Math.atan2(
					x - offsetx + i - bounds.getWidth() / 2,
					y - offsety + j - bounds.getHeight() / 2) / Math.PI / 2 + 0.5
				);
				
				// Find the saturation, using pythagorus
				float sa  = (float) ((bounds.getWidth()  / 2 - x + offsetx - i) / bounds.getWidth()  * 2);
				float sb  = (float) ((bounds.getHeight() / 2 - y + offsety - j) / bounds.getHeight() * 2);
				float sat = (float) Math.sqrt(sa * sa + sb * sb);
				
				// Convert to RGB
				float[] array = Color.getHSBColor(hue, sat, bri).getColorComponents(null);
				
				// And store in an array
				values[i * bits + j * w * bits + 0] = array[0] * 255;
				values[i * bits + j * w * bits + 1] = array[1] * 255;
				values[i * bits + j * w * bits + 2] = array[2] * 255;
			}
		}
		
		// Set all the pixels at once
		raster.setPixels(0, 0, w, h, values);
		
		return raster;
	}	
}