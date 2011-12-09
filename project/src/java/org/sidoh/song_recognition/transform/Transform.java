package org.sidoh.song_recognition.transform;


/**
 * Transform takes an array of stuff and performs some kind of transform on it
 * and returns an array of amplitude values.
 *  
 * @author chris
 *
 */
public interface Transform {
	public static class Builder {
		private final int frameSize;
		private boolean vorbis;

		private Builder(int frameSize) {
			this.frameSize = frameSize;
			this.vorbis = true;
		}
		
		public static Builder frameSize(int size) {
			return new Builder(size);
		}
		
		public static Builder defaultBuilder() {
			return new Builder(0);
		}
		
		public Builder withVorbis() {
			vorbis = true;
			return this;
		}
		
		public Transform create(double sampleRate) {
			Transform t = new DiscreteCosineTransform(frameSize, sampleRate);
			if (vorbis) {
				t = new VorbisSmoothingTransform(t);
			}
			
			return t;
		}
		
		public Transform create(int frameSize, double sampleRate) {
			Transform t = new DiscreteCosineTransform(frameSize, sampleRate);
			if (vorbis) {
				t = new VorbisSmoothingTransform(t);
			}
			
			return t;
		}
	}
	/**
	 * Transforms values into a list of amplitudes.
	 * 
	 * @param values
	 * @return
	 */
	public double[] transform(double[] values);
	
	/**
	 * Returns the smallest value in each bin of frequency values.
	 * 
	 * @return
	 */
	public double[] getBinFloors();
}
