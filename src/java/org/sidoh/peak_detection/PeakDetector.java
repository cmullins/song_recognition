package org.sidoh.peak_detection;

import java.util.List;

/**
 * A PeakDetector finds "peaks" in a time series. It should accept an Iterable list of
 * numerical types and return an Iterable of peak LOCATIONS within the time series.
 * @author chris
 *
 */
public abstract class PeakDetector {
	/** 
	 * Number of standard deviations outside of the mean a score has to be to be 
	 * considered a peak.
	 * 
	 */
	protected final static double PEAK_THRESHOLD = 3;
	
	/**
	 * Accepts a time series and returns the locations within the series that this 
	 * detector considers peaks. The locations should be 0-indexed from the first 
	 * thing in the time series.
	 * 
	 * @param series
	 * @return
	 */
	public abstract Iterable<Integer> findPeaks(Iterable<Double> values);
	
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	protected static Double mean(List<Double> a) {
		Double sum = 0d;
		for (Double value : a) {
			sum += value;
		}
		return sum/a.size();
	}
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	protected static Double sd(List<Double> a) {
		double sum = 0d;
		double sq  = 0d;
		int n      = a.size();
		for (Double value : a) {
			sum += value;
			sq  += value*value;
		}
		return Math.sqrt(
				(n*sq - sum*sum)
					/
				(n * (n-1)));
	}
}
