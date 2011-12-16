package org.sidoh.song_recognition.spectrogram;

import java.util.ArrayList;

public class InMemorySpectrogramStorage implements SpectrogramStorage {
	public static class Builder extends SpectrogramStorage.Builder {
		@Override
		public SpectrogramStorage create(int maxFrequency, int numBins) {
			return new InMemorySpectrogramStorage(maxFrequency, numBins);
		}
	}
	
	protected int maxTick;
	private final int maxFrequency;
	private final int bucketSize;
	private final int numBuckets;
	
	protected final ArrayList<double[]> ticks;
	
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
		return get(tick, freqToIndex(frequency));
	}

	@Override
	public double get(int tick, int bin) {
		if (ticks.size() <= tick || numBuckets <= bin) {
			throw new IllegalArgumentException(
					String.format("Trying to access tick that doesn't exist! Bounds: t:[0,%d], f:[0,%d], accessed: (%d,%d)",
						getMaxTick(), numBuckets, tick, bin));
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
