package org.sidoh.peak_detection;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
		
		public Builder withSmoothingFunction(StatefulSmoothingFunction.Builder smoothingFnBuilder) {
			if (smoothingFnBuilder == null) {
				return this;
			}
			else {
				return new StatefulSmoothedPeakDetector.Builder(smoothingFnBuilder, this);
			}
		}
	}
	
	private final PeakListener peaks;
	private int index;
	
	private static int counter = 0;
	protected int count;
	
	public StatefulPeakDetector(PeakListener peaks) {
		this.peaks = peaks;
		this.index = 0;
		count = counter++;
	}
	
	/**
	 * Informs the PeakListener that there is a peak at #index.
	 * 
	 * @param index
	 */
	protected final void offerPeak(int index, double value) {
		peaks.peakDetected(index, value);
	}
	
	/**
	 * 
	 */
	public final void offerNewValue(double value) {
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
	public static StatefulSdsFromMeanPeakDetector.Builder sdsFromMean(int windowWidth, double sds) {
		return new StatefulSdsFromMeanPeakDetector.Builder(windowWidth, sds);
	}
	
	/**
	 * Get a Builder that creates instances of {@link StatefulMeanDeltaPeakDetector}.
	 * 
	 * @param windowWidth
	 * @return
	 */
	public static StatefulMeanDeltaPeakDetector.Builder meanDelta(int windowWidth) {
		return new StatefulMeanDeltaPeakDetector.Builder(windowWidth);
	}
}
