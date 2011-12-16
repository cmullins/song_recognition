package org.sidoh.peak_detection;


/**
 * A PeakDetector finds "peaks" in a time series. It should accept an Iterable list of
 * numerical types and return an Iterable of peak LOCATIONS within the time series.
 * @author chris
 *
 */
public abstract class StatelessPeakDetector {
	/**
	 * Accepts a time series and returns the locations within the series that this 
	 * detector considers peaks. The locations should be 0-indexed from the first 
	 * thing in the time series.
	 * 
	 * @param series
	 * @return
	 */
	public abstract Iterable<Integer> findPeaks(Iterable<Double> values);
}
