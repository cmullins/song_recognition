package org.sidoh.song_recognition.signature;

import org.sidoh.math.Histogram;

public class NumTimesLargerThanNthPercentileHistogramScorer extends HistogramScorer {
	private static final long serialVersionUID = -2006865079626816327L;
	
	private final double percentile;

	public NumTimesLargerThanNthPercentileHistogramScorer(double percentile) {
		this.percentile = percentile;
		
	}

	@Override
	public double score(Histogram hist) {
		if (hist.getTotal() < 6) {
			return 0d;
		}
		double mean = hist.getNthPercentileMean(percentile);
		double max  = hist.getMaxCount();
		
		return (1d - 1d/(max/mean));
	}

}
