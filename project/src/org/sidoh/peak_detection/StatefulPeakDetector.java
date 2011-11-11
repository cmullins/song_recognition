package org.sidoh.peak_detection;

/**
 * Performs the same operation as a {@link StatelessPeakDetector}, but allows the
 * class to have state. This should be used where asynchronous stuff is happening.
 * 
 * @author chris
 *
 */
public abstract class StatefulPeakDetector implements ValueListener {
	public abstract static class Builder {
		public abstract StatefulPeakDetector create(PeakListener peaks);
	}
	
	private final PeakListener peaks;
	private int index;

	public StatefulPeakDetector(PeakListener peaks) {
		this.peaks = peaks;
		this.index = 0;
	}
	
	/**
	 * Informs the PeakListener that there is a peak at #index.
	 * 
	 * @param index
	 */
	protected void offerPeak(int index, double value) {
		peaks.peakDetected(index, value);
	}
	
	/**
	 * 
	 */
	public synchronized void offerNewValue(double value) {
		handleNewInput(index++, value);
	}
	
	/**
	 * Handles a new input value.
	 * 
	 * @param value
	 */
	protected abstract void handleNewInput(int index, double value);
	
	/**
	 * Get a Builder that creates instances of {@link StatefulSdsFromMeanPeakDetector}
	 * with the provided arguments.
	 * 
	 * @param windowWidth
	 * @param sds
	 * @return
	 */
	public static Builder sdsFromMean(int windowWidth, double sds) {
		return new StatefulSdsFromMeanPeakDetector.SdsFromMeanBuilder(windowWidth, sds);
	}
}
