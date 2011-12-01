package org.sidoh.song_recognition.signature;

import java.util.List;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.sidoh.math.Histogram;

public class EntropyScorer extends HistogramScorer {
	private static final long serialVersionUID = -263581399604698738L;

	@Override
	public double score(Histogram hist) {
		double entropy = 0d;

		NormalDistribution dist = new NormalDistributionImpl(hist.meanCount(), hist.sdCount());
		
		for (List<Double> bucket : hist.getValues().values()) {
			double p = dist.density(Double.valueOf(bucket.size()));
			
			entropy += p*Math.log(p);
		}
		
		return -entropy;
	}
	
	protected double getProbSuccess(Histogram hist) {
		return 1d / hist.getNumBins();
	}
	
}
