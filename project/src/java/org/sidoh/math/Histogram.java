package org.sidoh.math;

import java.util.HashMap;
import java.util.Map;

import org.sidoh.collections.BufferedCountingMap;
import org.sidoh.collections.Pair;

/**
 * Puts doubles into bins of integers.
 * 
 * @author chris
 *
 */
public class Histogram {
	private BufferedCountingMap values;

	public Histogram(int maxValues) {
		this.values = new BufferedCountingMap(maxValues);
	}
	
	public void addValue(double value) {
		int bin = (int)Math.floor(value);
		values.increment(bin);
	}
	
	public void reset() {
		values.reset();
	}

	public int getMaxCount() {
		return values.getMaxCount();
	}

	public int getMinCount() {
		return values.getMinCount();
	}

	public int getTotal() {
		return (int)values.getCountsSum();
	}
	
	public double meanCount() {
		return values.getMeanCount();
	}
	
	public double sdCount() {
		return values.getCountSd();
	}
	
	public int getNumBins() {
		return values.getNumBins();
	}
	
	public Map<Integer, Integer> getValues() {
		Map<Integer, Integer> r = new HashMap<Integer, Integer>();
		for (Pair<Integer,Integer> p : values.getEntries()) {
			r.put(p.getV1(), p.getV2());
		}
		return r;
	}
}
