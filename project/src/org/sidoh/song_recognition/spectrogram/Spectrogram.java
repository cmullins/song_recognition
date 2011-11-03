package org.sidoh.song_recognition.spectrogram;


public interface Spectrogram {
	/**
	 * Returns the maximum time that appears in this spectrogram.
	 * 
	 * @return
	 */
	public int getMaxTick();
	
	/**
	 * Returns how many ticks are in a second.
	 * 
	 * @return
	 */
	public int ticksInSeconds();
	
	/**
	 * Returns the maximum frequency this spectrogram tracks.
	 * 
	 * @return
	 */
	public int getMaxFrequency();
	
	/**
	 * Converts a tick to seconds.
	 * 
	 * @param tick
	 * @return
	 */
	public double tickToSeconds(int tick);
	
	/**
	 * 
	 * @param second
	 * @return
	 */
	public int secondsToTick(double second);
	
	/**
	 * 
	 * @param frequency
	 * @return
	 */
	public int frequencyToBin(double frequency);
	
	/**
	 * Returns a list of buckets that the frequencies fall into.
	 * 
	 * @return
	 */
	public double[] getBinFloors();
	
	/**
	 * 
	 * @param bin
	 * @return
	 */
	public double binToFrequency(int bin);
	
	/**
	 * Returns the intensity value at a particular point.
	 * 
	 * @param time
	 * @param frequency
	 * @return
	 */
	public double getIntensity(int time, double frequency);
	
	/**
	 * Returns the intensity value at a particular time/bin.
	 * 
	 * @param time
	 * @param bin
	 * @return
	 */
	public double getIntensity(int time, int bin);
	
	/**
	 * Free memory by releasing the intensity values.
	 * 
	 */
	public Spectrogram free();
}
