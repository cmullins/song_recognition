package org.sidoh.song_recognition.spectrogram;

/**
 * This spectrogram storage engine attempts to reuse claimed memory to avoid
 * the cost of finding new memory and garbage collecting old memory. This should
 * ONLY be used if a Spectrogram isn't going to be persisted after you're done
 * with it.
 * 
 * @author chris
 */
public class SingletonInMemorySpectrogramStorage extends InMemorySpectrogramStorage {
	
	public static class Builder extends SpectrogramStorage.Builder {
		private SingletonInMemorySpectrogramStorage instance;
		
		@Override
		public SpectrogramStorage create(int maxFrequency, int numBins) {
			if (instance == null) {
				instance = new SingletonInMemorySpectrogramStorage(maxFrequency, numBins);
			}
			instance.maxTick = 0;
			
			return instance;
		}
	}

	private SingletonInMemorySpectrogramStorage(int maxFrequency, int numBuckets) {
		super(maxFrequency, numBuckets);
	}
}
