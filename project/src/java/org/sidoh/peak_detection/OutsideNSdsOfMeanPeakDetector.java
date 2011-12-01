package org.sidoh.peak_detection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class OutsideNSdsOfMeanPeakDetector extends StatelessPeakDetector {
	
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
			double mean = SlidingWindow.mean(leftValues, rightValues);
			double sd   = SlidingWindow.sd(leftValues, rightValues);
			
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
}
