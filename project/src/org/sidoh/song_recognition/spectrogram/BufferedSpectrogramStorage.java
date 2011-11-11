package org.sidoh.song_recognition.spectrogram;


public class BufferedSpectrogramStorage implements SpectrogramStorage {
	
	public static class Builder extends SpectrogramStorage.Builder {
		private final int bufferSize;

		public Builder(int bufferSize) {
			this.bufferSize = bufferSize;
		}

		@Override
		public SpectrogramStorage create(int maxFrequency, int numBins) {
			return new BufferedSpectrogramStorage(maxFrequency, numBins, bufferSize);
		}
	}
	
	protected double[][] buffer;
	private int maxFrequency;
	private int numBuckets;
	private int bucketSize;
	private int maxTick;
	private final int bufferSize;
	
	public BufferedSpectrogramStorage(int maxFrequency, int numBuckets, int bufferSize) {
		this.maxFrequency = maxFrequency;
		this.numBuckets = numBuckets;
		this.bufferSize = bufferSize;
		this.bucketSize = maxFrequency / numBuckets;
		this.maxTick = -1;
		
		this.buffer = initBuffer(bufferSize, numBuckets, Double.NEGATIVE_INFINITY);
	}

	@Override
	public void put(int tick, int frequency, double intensity) {
		int x = (tick % bufferSize);
		int y = frequency;
		
		// Make sure we're not going out of bounds...
		if (tick < (maxTick - bufferSize)) {
			throw new IndexOutOfBoundsException("Can't access index `" + tick + "'.");
		} 
		
		if (tick > maxTick) {
			maxTick = tick;
		}
		
		buffer[x][y] = intensity;
	}

	@Override
	public synchronized double get(int tick, double frequency) {
		int bin = freqToIndex(frequency);
		return get(tick, bin);
	}

	@Override
	public synchronized double get(int tick, int bin) {
		if (tick > maxTick) {
			throw new IllegalStateException("Trying to access values that haven't been populated yet!");
		}
		if (tick < (maxTick - bufferSize)) {
			throw new IndexOutOfBoundsException("Can't access index `" + tick + "'.");
		}
		
		int x = (tick % bufferSize);
		int y = bin;
		
		return buffer[x][y];
	}

	@Override
	public int getMaxTick() {
		return maxTick;
	}

	@Override
	public void free() {
		buffer = null;
	}

	protected int freqToIndex(double frequency) {
		return (int)Math.floor(frequency / bucketSize);
	}

	/**
	 * Helper method to initialize buffer
	 * 
	 * @param x
	 * @param y
	 * @param value
	 * @return
	 */
	protected static double[][] initBuffer(int x, int y, double value) {
		double[][] a = new double[x][y];
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				a[i][j] = value;
			}
		}
		return a;
	}
}
