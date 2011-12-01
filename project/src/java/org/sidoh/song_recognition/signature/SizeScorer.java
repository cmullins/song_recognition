package org.sidoh.song_recognition.signature;

import org.sidoh.math.Histogram;

public class SizeScorer extends HistogramScorer {
	private static final long serialVersionUID = -3264789217685915505L;

	@Override
	public double score(Histogram hist) {
		return 1.0 - 1.0/hist.getTotal();
	}

}
