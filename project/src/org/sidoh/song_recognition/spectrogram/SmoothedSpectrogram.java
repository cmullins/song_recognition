package org.sidoh.song_recognition.spectrogram;

import java.util.Arrays;

/**
 * This smooths a provided Spectrogram by finding the mean of each bin formed by
 * resizing the Spectrogram to the provided size.
 * 
 * @author chris
 *
 */
public class SmoothedSpectrogram extends Spectrogram {
	
	private final int maxTick;
	private final int numBins;
	private final int ticksInSeconds;
	private final double[] binFloors;
	private final double[] oldBins;
	private final Spectrogram old;
	private SpectrogramStorage store;
	private final int oldTicksInNewTick;
	private final int oldBinsInNewBin;
	private final double frequencyScaleFactor;
	private final double timeScaleFactor;
	private final double bucketSize;

	public SmoothedSpectrogram(Spectrogram old, double timeScaleFactor, double frequencyScaleFactor) {
		if (timeScaleFactor <= 0 || timeScaleFactor > 1 || frequencyScaleFactor <= 0 || frequencyScaleFactor > 1) {
			throw new IllegalArgumentException("scale factor should be > 0, <= 1.");
		}
		this.oldBins = old.getBinFloors();
		this.maxTick = (int)(old.getMaxTick()*timeScaleFactor);
		this.numBins = (int)(oldBins.length * frequencyScaleFactor);
		this.binFloors = new double[numBins];
		this.ticksInSeconds = (int)(old.ticksInSeconds()*timeScaleFactor);
		this.old = old;
		this.store = new InMemorySpectrogramStorage(old.getMaxFrequency(), numBins);
		this.oldTicksInNewTick = (int)(1 / timeScaleFactor);
		this.oldBinsInNewBin   = (int)(1 / frequencyScaleFactor);
		this.timeScaleFactor = timeScaleFactor;
		this.frequencyScaleFactor = frequencyScaleFactor;
		this.bucketSize = getMaxFrequency() / (double)numBins;
		
		double binSize = (old.getMaxFrequency() / numBins);
		for (int i = 0; i < numBins; i++) {
			binFloors[i] = (i*binSize);
		}
		
		smooth();
	}

	@Override
	public int getMaxTick() {
		return maxTick;
	}

	@Override
	public int ticksInSeconds() {
		return ticksInSeconds;
	}
	
	@Override
	public double tickToSeconds(int tick) {
		return tick / (double)ticksInSeconds();
	}

	@Override
	public int getMaxFrequency() {
		return old.getMaxFrequency();
	}

	@Override
	public double[] getBinFloors() {
		return Arrays.copyOf(binFloors, binFloors.length);
	}

	@Override
	public double getIntensity(int time, int bin) {
		return store.get(time, bin);
	}

	@Override
	public double getIntensity(int time, double frequency) {
		return store.get(time, frequency);
	}

	@Override
	public int secondsToTick(double second) {
		return (int)(Math.floor(second * ticksInSeconds()));
	}

	@Override
	public int frequencyToBin(double frequency) {
		return (int)Math.floor(frequency / binFloors.length);
	}

	@Override
	public double binToFrequency(int bin) {
		return binFloors[bin];
	}

	private void smooth() {
		
		for (int bin = 0; bin < numBins; bin++) {
			if (bin % 100 == 0)
			System.out.println(bin + "/" + numBins);
			for (int tick = 0; tick < maxTick; tick++) {
				store.put(tick, bin, smooth(tick, bin));
			}
		}
	}
	
	private double smooth(int tick, int frequency) {
		tick *= oldTicksInNewTick;
		frequency *= oldBinsInNewBin;
		
		int tickEnd = tick+oldTicksInNewTick;
		if ((old.getMaxTick() - tickEnd) < oldTicksInNewTick) {
			tickEnd = old.getMaxTick();
		}
		int freqEnd = frequency+oldBinsInNewBin;
		if ((oldBins.length - freqEnd) < oldBinsInNewBin) {
			freqEnd = oldBins.length;
		}
		
		double sum = 0.0;
		int count  = (tick - tickEnd) * (frequency - freqEnd);
		
		for (int i = tick; i < tickEnd; i++) {
			for (int j = frequency; j < freqEnd; j++) {
				sum += old.getIntensity(i, j);
			}
		}
		
		return (sum / count);
	}

	@Override
	public Spectrogram free() {
		old.free();
		store = null;
		return this;
	}
}
