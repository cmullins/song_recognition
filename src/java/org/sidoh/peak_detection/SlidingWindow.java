package org.sidoh.peak_detection;

import java.util.Iterator;

import org.sidoh.collections.AscendingMinimaWindow;
import org.sidoh.collections.DoubleAscendingMinimaWindow;
import org.sidoh.collections.DoubleDeque;

/**
 * this is a utility class that will get used in some of the peak detection
 * algorithms in this package.
 * 
 * old values are automatically shifted out as new ones come in.
 * 
 * @author chris
 *
 */
public class SlidingWindow {
	private DoubleDeque values;
	private final int maxSize;
	private double sum;
	private double sumOfSquares;
	private double deltaSum;
	private double deltaRSum;
	
	private DoubleAscendingMinimaWindow minTracker;
	private DoubleAscendingMinimaWindow maxTracker;
	
	public SlidingWindow(int maxSize) {
		this.maxSize = maxSize;
		this.values  = new DoubleDeque(maxSize);
		
		this.minTracker = DoubleAscendingMinimaWindow.minimaTracker(maxSize);
		this.maxTracker = DoubleAscendingMinimaWindow.maximaTracker(maxSize);
		
		// yuck
		this.sum          = 0d;
		this.sumOfSquares = 0d;
		this.deltaSum     = 0d;
		this.deltaRSum    = 0d;
	}
	
	public SlidingWindow(int maxSize, Iterable<Double> initialElements) {
		this(maxSize);
		Iterator<Double> itr = initialElements.iterator();
		for (int i = 0; i < maxSize; i++) {
			pushValue(itr.next());
		}
	}
	
	/**
	 * Push a value onto this window. Returns the value that gets shifted off of the 
	 * end if there is one. Returns null if there wasn't one.
	 * 
	 * @param value
	 */
	public double pushValue(double value) {
		double shifted = Double.NEGATIVE_INFINITY;
		if (values.size() >= maxSize) {
			double oldValue = values.removeFirst();
			
			shifted = oldValue;
		
			sum -= oldValue;
			sumOfSquares -= oldValue*oldValue;
			deltaSum -= (values.peekFirst() - oldValue);
		}
		
		sum += value;
		sumOfSquares += value*value;
		
		if (values.size() > 0) {
			deltaSum += (value - values.peekLast());
		}
		
		values.addLast(value);
		minTracker.offer(value);
		maxTracker.offer(value);
		
		return shifted;
	}
	
	/**
	 * Get the sum of the values in this window.
	 * 
	 * @return
	 */
	public double sum() {
		return sum;
	}
	
	/**
	 * Get sum of the squared values in this window.
	 * 
	 * @return
	 */
	public double sumOfSquares() {
		return sumOfSquares;
	}
	
	/**
	 * Get the mean of the values in this window.
	 * 
	 * @return
	 */
	public double mean() {
		return sum / values.size();
	}
	
	/**
	 * Get the mean delta value of this window
	 * 
	 * @return
	 */
	public double meanDelta() {
		return (deltaSum - deltaRSum) / (values.size() - 1);
	}
	
	/**
	 * Get the standard deviation of values in this window.
	 * 
	 * @return
	 */
	public double standardDeviation() {
		return Math.sqrt(variance());
	}
	
	public double min() {
		return minTracker.getMinimum();
	}
	
	public double max() {
		return maxTracker.getMinimum();
	}
	
	/**
	 * Get the variance of the values in this window.
	 * 
	 * @return
	 */
	public double variance() {
		int n = values.size();
		return 
			((n*sumOfSquares) - sum*sum)
				/
			(n * (n-1));
	}

	public static double mean(SlidingWindow left, SlidingWindow right) {
		return (left.mean() + right.mean()) / 2;
	}
	
	public static double sd(SlidingWindow left, SlidingWindow right) {
		int n = left.maxSize+right.maxSize;
		double sumOfSquares = (left.sumOfSquares() + right.sumOfSquares());
		double sum = left.sum()+right.sum();
		
		return Math.sqrt(
				(n*(sumOfSquares) - (sum*sum))
					/
				(n*(n-1)));
	}
	
	public String toString() {
		return values.toString();
	}
}
