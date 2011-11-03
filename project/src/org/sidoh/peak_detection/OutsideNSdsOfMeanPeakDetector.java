package org.sidoh.peak_detection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class OutsideNSdsOfMeanPeakDetector extends PeakDetector {
	
	private final int k;
	private final double numSds;

	/**
	 * 
	 * @param windowWidth width of the window centered around each point
	 * @param numSds number of sds
	 */
	public OutsideNSdsOfMeanPeakDetector(int windowWidth, double numSds) {
		this.numSds = numSds;
		this.k = windowWidth/2;
		
	}

	@Override
	public Iterable<Integer> findPeaks(Iterable<Double> values) {
		Iterator<Double> itr = values.iterator();
		List<Integer> peaks  = new ArrayList<Integer>();
		
		// Pre-populate windows
		SlidingWindow leftValues = new SlidingWindow(k);
		SlidingWindow rightValues = new SlidingWindow(k);
		
		for (int i = 0; i < k; i++) {
			leftValues.pushValue(itr.next());
		}
		
		// Store first point we'll consider for a peak
		double xi = itr.next();
		int index = k;
		
		for (int i = 0; i < k; i++) {
			rightValues.pushValue(itr.next());
		}
		
		// Consider each point we can.
		while (itr.hasNext()) {
			double mean = mean(leftValues, rightValues);
			double sd   = sd(leftValues, rightValues);
			
			// Check if this thing is a peak.
			if (Math.abs(xi - mean) > (numSds*sd)) {
				peaks.add(index);
			}
			
			// Update the windows n' stuff.
			leftValues.pushValue(xi);
			xi = rightValues.pushValue(itr.next());
			index++;
		}
		
		return peaks;
	}

	protected double mean(SlidingWindow left, SlidingWindow right) {
		return (left.mean() + right.mean()) / 2;
	}
	
	protected double sd(SlidingWindow left, SlidingWindow right) {
		int n = 2*k;
		double sumOfSquares = (left.sumOfSquares() + right.sumOfSquares());
		double sum = left.sum()+right.sum();
		
		return Math.sqrt(
				(n*(sumOfSquares) - (sum*sum))
					/
				(n*(n-1)));
	}
}
