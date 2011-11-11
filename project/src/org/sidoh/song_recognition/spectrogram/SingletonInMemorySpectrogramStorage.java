package org.sidoh.song_recognition.spectrogram;

/**
 * This spectrogram storage engine attempts to reuse claimed memory to avoid
 * the cost of finding new memory and garbage collecting old memory. This should
 * ONLY be used if a Spectrogram isn't going to be persisted after you're done
 * with it.
 * 
 * When you call {@link #getInstance()}, counters are reset, but the claimed
 * memory stays intact and gets reused.
 * 
 * @author chris
 *
 */
public class SingletonInMemorySpectrogramStorage extends InMemorySpectrogramStorage {
	
	public static class Builder extends SpectrogramStorage.Builder {
		@Override
		public SpectrogramStorage create(int maxFrequency, int numBins) {
			return getInstance(maxFrequency, numBins);
		}
	}

	private static SingletonInMemorySpectrogramStorage instance = null;
	
	private SingletonInMemorySpectrogramStorage(int maxFrequency, int numBuckets) {
		super(maxFrequency, numBuckets);
	}

	/**
	 * Gets the instance of {@link SingletonInMemorySpectrogramStorage}. <B>WARNING</b>
	 * this will cause any {@link Spectrogram} also using {@link SingletonInMemorySpectrogramStorage}
	 * to be reset.
	 * 
	 * @return
	 */
	public static SingletonInMemorySpectrogramStorage getInstance(int maxFrequency, int numBuckets) {
		if (instance == null) {
			instance = new SingletonInMemorySpectrogramStorage(maxFrequency, numBuckets);
		}
		
		instance.maxTick = 0;

		return instance;
	}
}
