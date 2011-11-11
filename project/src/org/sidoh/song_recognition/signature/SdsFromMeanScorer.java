package org.sidoh.song_recognition.signature;

import org.sidoh.math.Histogram;

public class SdsFromMeanScorer extends HistogramScorer {
	private static final long serialVersionUID = -6916751322431791357L;

	@Override
	public double score(Histogram hist) {
		double max  = hist.getMaxCount();
		double mean = hist.meanCount();
		double sd   = hist.sdCount();
		
		double sdsFromMean = (Math.abs(max - mean) / sd);
		
		return (1d - (1d / sdsFromMean));
	}

}
