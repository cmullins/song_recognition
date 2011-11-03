package org.sidoh.peak_detection;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

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
	private Deque<Double> values;
	private final int maxSize;
	private Double sum;
	private Double sumOfSquares;
	
	public SlidingWindow(int maxSize) {
		this.maxSize = maxSize;
		this.values  = new LinkedList<Double>();
		
		// yuck
		this.sum          = 0d;
		this.sumOfSquares = 0d;
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
	public Double pushValue(Double value) {
		Double shifted = null;
		if (values.size() >= maxSize) {
			double oldValue = values.removeFirst();
			shifted = oldValue;
		
			sum -= oldValue;
			sumOfSquares -= oldValue*oldValue;
		}
		
		sum += value;
		sumOfSquares += value*value;
		values.addLast(value);
		
		return shifted;
	}
	
	/**
	 * Get the sum of the values in this window.
	 * 
	 * @return
	 */
	public Double sum() {
		return sum;
	}
	
	/**
	 * Get sum of the squared values in this window.
	 * 
	 * @return
	 */
	public Double sumOfSquares() {
		return sumOfSquares;
	}
	
	/**
	 * Get the mean of the values in this window.
	 * 
	 * @return
	 */
	public Double mean() {
		return sum / values.size();
	}
	
	/**
	 * Get the standard deviation of values in this window.
	 * 
	 * @return
	 */
	public Double standardDeviation() {
		return Math.sqrt(variance());
	}
	
	/**
	 * Get the variance of the values in this window.
	 * 
	 * @return
	 */
	public Double variance() {
		int n = values.size();
		return 
			((n*sumOfSquares) - sum*sum)
				/
			(n * (n-1));
	}
}
