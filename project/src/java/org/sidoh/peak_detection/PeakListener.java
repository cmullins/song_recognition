package org.sidoh.peak_detection;

/**
 * Something that gets notified when a peak has been detected.
 * 
 * @author chris
 *
 */
public interface PeakListener {
	public void peakDetected(int index, double value);
}
