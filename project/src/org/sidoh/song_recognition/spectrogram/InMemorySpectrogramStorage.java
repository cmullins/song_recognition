package org.sidoh.song_recognition.spectrogram;

import java.util.ArrayList;

public class InMemorySpectrogramStorage implements SpectrogramStorage {
	private int maxTick;
	private final int maxFrequency;
	private final int bucketSize;
	private final int numBuckets;
	
	private final ArrayList<double[]> ticks;
	
	public InMemorySpectrogramStorage(int maxFrequency, int numBuckets) {
		this.maxFrequency = maxFrequency;
		this.numBuckets = numBuckets;
		this.bucketSize = maxFrequency / numBuckets;
		this.ticks = new ArrayList<double[]>();
		this.maxTick = -1;
	}

	@Override
	public void put(int tick, int frequency, double intensity) {
		if (tick >= ticks.size()) {
			for (int i = ticks.size()-1; i < tick; i++) {
				ticks.add(new double[numBuckets]);
			}
		}
		if (tick > maxTick) {
			maxTick = tick;
		}
		
		ticks.get(tick)[frequency] = intensity;
	}

	@Override
	public double get(int tick, double frequency) {
		if (ticks.size() <= tick) {
			return 0;
		}
		return ticks.get(tick)[Math.min(freqToIndex(frequency), numBuckets-1)];
	}

	@Override
	public double get(int tick, int bin) {
		if (ticks.size() <= tick || numBuckets <= bin) {
			return 0;
		}
		return ticks.get(tick)[bin];
	}

	protected int freqToIndex(double frequency) {
		return (int)Math.floor(frequency / bucketSize);
	}

	@Override
	public int getMaxTick() {
		return maxTick;
	}
	
	public void free() {
		ticks.clear();
		ticks.trimToSize();
	}
}
