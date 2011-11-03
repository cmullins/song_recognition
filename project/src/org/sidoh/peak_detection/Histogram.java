package org.sidoh.peak_detection;

import java.util.List;
import java.util.Map;

import org.sidoh.collections.HashOfLists;
import org.sidoh.collections.ReversingComparator;

import com.google.common.collect.MinMaxPriorityQueue;

/**
 * Puts doubles into bins of integers.
 * 
 * @author chris
 *
 */
public class Histogram {
	private HashOfLists<Integer, Double> values;
	private int maxCount;
	private int minCount;
	private int maxBin;
	private int minBin;
	private int total;
	
	public Histogram() {
		maxCount = 0;
		minCount = Integer.MAX_VALUE;
		maxBin = -1;
		minBin = -1;
		total = 0;
		values = new HashOfLists<Integer, Double>();
	}
	
	public void addValue(double value) {
		int bin = (int)Math.floor(value);
		values.addFor(bin, value);
		int size = values.get(bin).size();
		
		if (size > maxCount) {
			maxCount = size;
			maxBin   = bin;
		}
		if (size < minCount) {
			minCount = size;
			minBin   = bin;
		}
		
		total++;
	}

	public Map<Integer, List<Double>> getValues() {
		return values;
	}

	public int getMaxCount() {
		return maxCount;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxBin() {
		return maxBin;
	}

	public int getMinBin() {
		return minBin;
	}

	public int getTotal() {
		return total;
	}
	
	public double meanCount() {
		return total / (double)values.size();
	}
	
	public double sdCount() {
		int sum = 0;
		int sq  = 0;
		
		for (List<Double> bin : values.values()) {
			sum += bin.size();
			sq  += bin.size()*bin.size();
		}
		
		int n = values.size();
		
		return Math.sqrt(
				((n*sq) - sum*sum)
					/
				(double)(n*(n-1)));
	}
	
	public double getNthPercentileMean(double p) {
		int size = (int)Math.floor(p*values.size());
		size = Math.max(2, size);
		MinMaxPriorityQueue<Integer> topN
			= MinMaxPriorityQueue
				.orderedBy(new ReversingComparator<Integer>())
				.maximumSize(size)
				.create();
		
		for (Integer bin : values.keySet()) {
			topN.add(values.get(bin).size());
		}
		
		double sum = 0d;
		for (Integer binSize : topN) {
			sum += binSize;
		}
		
		return sum/size;
	}
	
	public double maxSignificance() {
		// Punt unless there are at least a few buckets to consider.
		if (values.size() < 5) {
			return 0d;
		}
		
		int size = (int)Math.floor(values.size());
		size = Math.max(2, size);
		MinMaxPriorityQueue<Integer> topN
			= MinMaxPriorityQueue
				.orderedBy(new ReversingComparator<Integer>())
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
		
		int max = getMaxCount();
		
		double sdsFromMean = Math.max(1d, (Math.abs(max - mean) / sd));

		// via the Chebyshev Inequality
		double significance = (1d - (1d/(sdsFromMean*sdsFromMean)));
		double heightWeight = 1d - (1d / Math.max(1d,getMaxCount() - mean - 5*sd));
		
		return significance*heightWeight;
	}
}
