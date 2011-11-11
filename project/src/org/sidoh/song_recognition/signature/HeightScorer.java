package org.sidoh.song_recognition.signature;

import org.sidoh.math.Histogram;

public class HeightScorer extends HistogramScorer {
	private static final long serialVersionUID = -8408249092626406309L;

	@Override
	public double score(Histogram hist) {
		if (hist.getTotal() < 6) {
			return 0d;
		}
		double max = hist.getMaxCount();
		
		return (1d - (1d/max));
	}

}
