package org.sidoh.song_recognition.signature;

import java.io.Serializable;

import org.sidoh.math.Histogram;

/**
 * A {@link HistogramScorer} should return a score based on a {@link Histogram}.
 * 
 * @author chris
 *
 */
public abstract class HistogramScorer implements Serializable {
	private static final long serialVersionUID = 7388831796868643276L;

	/**
	 * Generate score for this {@link Histogram}
	 * @param hist
	 * @return
	 */
	public abstract double score(Histogram hist);
	
	public static HistogramScorer heightScorer() {
		return new HeightScorer();
	}
	
	public static HistogramScorer heightProbabilityScorer() {
		return new HeightProbabilityScorer();
	}
	
	public static HistogramScorer sdsFromMean() {
		return new SdsFromMeanScorer();
	}
	
	public static HistogramScorer sizeScorer() {
		return new SizeScorer();
	}
}
