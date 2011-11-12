package org.sidoh.song_recognition.signature;

import java.util.Collection;
import java.util.Collections;

import org.sidoh.math.Histogram;

import com.google.common.collect.MinMaxPriorityQueue;

/**
 * Finds the max outside of the Nth percentile (technically, the min in the Nth percentile)
 * and shifts the maximum by that amount.
 * 
 * @author chris
 *
 */
public class DownShiftingHeightScorer extends HistogramScorer {
	private static final long serialVersionUID = 8404074525155734100L;
	private final double percentile;
	private final boolean defaultTo2;
	
	public DownShiftingHeightScorer(double percentile, boolean defaultTo2) {
		this.percentile = percentile;
		this.defaultTo2 = defaultTo2;
	}

	@Override
	public double score(Histogram hist) {
		int size = (int)Math.floor(percentile * hist.getNumBins());
		
		if (size <= 2 && !defaultTo2) {
			return 0d;
		}
		else if (size < 2) {
			size = 2;
		}
		
		MinMaxPriorityQueue<Integer> pq = 
			MinMaxPriorityQueue
				.orderedBy(Collections.reverseOrder())
				.maximumSize(size)
				.create();
		
		for (Collection<Double> bucket : hist.getValues().values()) {
			pq.add(bucket.size());
		}
		
		double minInNth = pq.peekLast();
		double max = hist.getMaxCount();
		double den = Math.pow((max-minInNth), 2) / 2;
		den = Math.max(1, den);
		
		return (1d - 1d/den);
	}

}
