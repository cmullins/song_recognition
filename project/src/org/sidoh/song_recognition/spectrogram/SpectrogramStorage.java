package org.sidoh.song_recognition.spectrogram;

/**
 * Defines an interface that stores all of the necessary values for a spectrogram.
 * 
 * @author chris
 *
 */
public interface SpectrogramStorage {
	public abstract static class Builder {
		public static Builder buffered(int bufferSize) {
			return new BufferedSpectrogramStorage.Builder(bufferSize);
		}
		
		public static Builder inMemory() {
			return new InMemorySpectrogramStorage.Builder();
		}
		
		public static Builder singleton() {
			return new SingletonInMemorySpectrogramStorage.Builder();
		}
		
		public abstract SpectrogramStorage create(int maxFrequency, int numBins);
	}
	/**
	 * Store a value.
	 * 
	 * @param tick
	 * @param frequency
	 * @param intensity
	 */
	public void put(int tick, int frequency, double intensity);
	
	/**
	 * Get a value by tick/frequency
	 * 
	 * @param tick
	 * @param frequency
	 * @return
	 */
	public double get(int tick, double frequency);
	
	/**
	 * Get a value by tick/frequency bin.
	 * 
	 * @param tick
	 * @param bin
	 * @return
	 */
	public double get(int tick, int bin);
	
	/**
	 * Gets the biggest tick seen.
	 * 
	 * @return
	 */
	public int getMaxTick();
	
	/**
	 * Release memory
	 * 
	 */
	public void free();
}
