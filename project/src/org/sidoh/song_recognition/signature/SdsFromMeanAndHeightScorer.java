package org.sidoh.song_recognition.signature;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.sidoh.math.Histogram;

import com.google.common.collect.MinMaxPriorityQueue;

public class SdsFromMeanAndHeightScorer extends HistogramScorer {
	private static final long serialVersionUID = -5465267212432618116L;
	private final int heightSignificanceFactor;
	
	/**
	 * 
	 * @param heightSignificanceFactor the number of SDs from the mean should be
	 * considered significant.
	 */
	public SdsFromMeanAndHeightScorer(int heightSignificanceFactor) {
		this.heightSignificanceFactor = heightSignificanceFactor;
		
	}

	@Override
	public double score(Histogram hist) {
		Map<Integer, List<Double>> values = hist.getValues();

		// Punt unless there are at least a few buckets to consider.
		if (values.size() < 5) {
			return 0d;
		}
		
		int size = values.size();
		MinMaxPriorityQueue<Integer> topN
			= MinMaxPriorityQueue
				.orderedBy(Collections.reverseOrder())
				.maximumSize(size)
				.create();
		
		for (Integer bin : values.keySet()) {
			topN.add(values.get(bin).size());
		}
		
		// Pop the max because it shouldn't be included in the mean. Will skew results.
		topN.poll();
		size = topN.size();
		
		double sum = 0d;
		double sq  = 0d;
		for (Integer binSize : topN) {
			sum += binSize;
			sq  += binSize*binSize;
		}
		
		double mean = sum/size;
		double sd   = Math.sqrt(
			((size*sq) - sum*sum)
				/
			(double)(size*(size-1)));
		
		int max = hist.getMaxCount();
		
		double sdsFromMean = Math.max(1d, (Math.abs(max - mean) / sd));

		// via the Chebyshev Inequality
		double significance = (1d - (1d/(sdsFromMean*sdsFromMean)));
		double heightWeight = 1d - (1d / Math.max(1d,hist.getMaxCount() - mean - heightSignificanceFactor*sd));
		
		return significance*heightWeight;
	}
}
