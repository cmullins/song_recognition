package org.sidoh.peak_detection;

/**
 * This class allows one to deal with sliding windows that are centered at a point
 * and deal with computations on the left and the right sliding windows.
 * 
 *   ============ * ============
 *    ^           ^   ^____ Right window
 *    |           |________ Center
 *    |____________________ Left window
 * @author chris
 *
 */
public class WindowWithCenter {
	private final int k;
	private final SlidingWindow windowLeft;
	private final SlidingWindow windowRight;

	private double x;

	/**
	 * 
	 * @param windowSize the number of elements in the ENTIRE WINDOW. Note that this
	 *  includes the center point and both the left and right windows! It will round
	 *  up to the nearest odd number so the left and right windows can be divided
	 *  evenly.
	 */
	public WindowWithCenter(int windowSize) {
		k = windowSize / 2;
		windowLeft = new SlidingWindow(k);
		windowRight = new SlidingWindow(k);
		x = 0;
	}
	
	/**
	 * Offers a value to the window. If the window is full of elements, this will return
	 * true. Otherwise, it returns false.
	 * 
	 * @param value
	 * @return
	 */
	public boolean offer(double value) {
		// Push value onto the right window.
		double shifted = windowRight.pushValue(value);
		
		if (shifted != Double.NEGATIVE_INFINITY) {
			windowLeft.pushValue(x);
			x = shifted;
			return true;
		}
		
		return false;
	}
	
	/**
	 * Gets the left portion of this window.
	 * 
	 * @return
	 */
	public SlidingWindow left() {
		return windowLeft;
	}
	
	/**
	 * Gets the right portion of this window.
	 * 
	 * @return
	 */
	public SlidingWindow right() {
		return windowRight;
	}
	
	/**
	 * Returns the value of the centroid of this window.
	 * 
	 * @return
	 */
	public double x() {
		return x;
	}
}
