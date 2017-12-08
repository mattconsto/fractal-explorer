package mc21g14.fractal.misc;

import java.io.Serializable;

/**
 * A Complex Number
 * 
 * @author Matthew Consterdine
 */
public class Complex implements Cloneable, Serializable {
	public final double r;
	public final double i;
	
	/**
	 * Create a complex number
	 * @param r The real part of the complex number
	 * @param i The imaginary part of the complex number
	 */
	public Complex(double r, double i) {
		this.r = r;
		this.i = i;
	}
	
	/**
	 * Get the real component
	 * @return The real component
	 */
	public double getR() {
		return r;
	}
	
	/**
	 * Get the imaginary component
	 * @return The imaginary component
	 */
	public double getI() {
		return i;
	}

	/**
	 * Square this complex number
	 * @return The square of this complex number
	 */
	public Complex square() {
		return pow(2);
	}
	
	/**
	 * Puts this number to the power provided
	 * @param n A number between and including 0 and 10
	 * @return This number to the order provided
	 */
	public Complex pow(int n) {
		// Some values are hardcoded for speed, avoids the pow and square rooting.
		switch (n) {
			case 0:  return new Complex(1, 0);
			
			case 1:  return new Complex(r, i);
			case 2:  return new Complex(r*r - i*i, 2*r*i);
			case 3:  return new Complex(r*r*r - 3*i*i*r, 3*i*r*r - i*i*i);
			case 5:  return new Complex(r*r*r*r*r - 10*i*i*r*r*r + 5*i*i*i*i*r, 5*i*r*r*r*r - 10*i*i*i*r*r + i*i*i*i*i);
			case 7:  return new Complex(r*r*r*r*r*r*r - 21*i*i*r*r*r*r*r + 35*i*i*i*i*r*r*r - 7*i*i*i*i*i*i*r, 7*i*r*r*r*r*r*r - 35*i*i*i*r*r*r*r + 21*i*i*i*i*i*r*r - i*i*i*i*i*i*i);

			case 4:  return pow(2).pow(2);
			case 6:  return pow(2).pow(3);
			case 8:  return pow(2).pow(4);
			case 9:  return pow(3).pow(3);
			case 10: return pow(2).pow(5);
			
			default:
				// Significantly slower than the code above, uses Polar form and DeMoivre's Theorem
				// Lets us use pretty, negative powers and large positive ones
				// http://stackoverflow.com/a/3099602
				double rn = Math.pow(Math.sqrt(Math.pow(r, 2) + Math.pow(i, 2)), n);
				double th = Math.atan(i / r);
				
				return new Complex(rn * Math.cos(n * th), rn * Math.sin(n * th));
		}
	}

	/**
	 * Perform Get the modulus squared of this complex number
	 * @return The result
	 */
	public double modulusSquared() {
		return r*r + i*i;
	}
	
	/**
	 * Add a complex number to this number
	 * @param o The complex number we want to add to this one
	 * @return The result
	 */
	public Complex add(Complex o) {
		return new Complex(r + o.r, i + o.i);
	}
	
	/**
	 * Subtract a complex number to this number
	 * @param o The complex number we want to add to this one
	 * @return The result
	 */
	public Complex subtract(Complex o) {
		return new Complex(r - o.r, i - o.i);
	}
	
	/**
	 * Multiply a complex number
	 * @param o The complex number we want to multiple this one by
	 * @return The result
	 */
	public Complex multiply(Complex o) {
		return new Complex(r*o.r - i*o.i, r*o.i + i*o.r);
	}
	
	/**
	 * Divide a complex number
	 * @param o The complex number we want to multiple this one by
	 * @return The result
	 */
	public Complex divide(Complex o) {
		return new Complex((r*o.r + i*o.i)/(o.r*o.r + o.i*o.i), (o.r*i - r*o.i)/(o.r*o.r + o.i*o.i));
	}
	
	/**
	 * Inverse a complex number
	 * @return The inversed complex number
	 */
	public Complex inverse() {
		return new Complex(1, 0).divide(this);
	}
	
	/**
	 * Get the string representation of this complex number
	 * @return String representation
	 */
	public String toString() {
		return String.format("%f%+fi", r, i);
	}
	
	/**
	 * Parse a string into a Complex
	 * @param string The input String
	 * @return A complex number if the string is valid, null if not.
	 */
	public static Complex parseString(String string) {
		if(string == null) return null;
		
		// Clean up the string
		string = string.replaceAll("[^0-9\\.\\+-]", "").replaceFirst("^\\+", "");
		
		// Check it matches our regex
		if(!string.matches("^[\\+-]?[0-9]+.[0-9]*[\\+-][0-9]+.[0-9]*$")) return null;
		
		// Split and create!
		int index = Math.max(string.lastIndexOf("+"), string.lastIndexOf("-"));
		String r  = string.substring(0, index);
		String i  = string.substring(index);
		
		return new Complex(Double.parseDouble(r), Double.parseDouble(i.substring(0, i.length() - 1)));
	}
	
	/**
	 * Check if the provided complex is identical to this one
	 * @param other The complex provided
	 * @return True if identical
	 */
	public boolean equals(Complex other) {
		return equals(other, 0);
	}
	
	/**
	 * Check if the difference between this complex and the provided is under a set threshold
	 * @param other The complex provided
	 * @param threshold The threshold set
	 * @return True if under the threshold
	 */
	public boolean equals(Complex other, double threshold) {
		return Math.abs(this.r - other.r) < threshold && Math.abs(this.i - other.i) < threshold;
	}
	
	/**
	 * Clone the complex number
	 */
	@Override
	protected Complex clone() {
		return new Complex(r, i);
	}
}
