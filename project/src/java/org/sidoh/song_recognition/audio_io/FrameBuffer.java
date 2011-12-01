package org.sidoh.song_recognition.audio_io;

import java.io.File;
import java.io.IOException;

/**
 * A frame buffer is an iterator across frames.
 * 
 * @author chris
 *
 */
public abstract class FrameBuffer implements Iterable<double[]> {
	public static class Builder {
		private int frameSize;
		private double overlap;

		private Builder(int frameSize) {
			this.frameSize = frameSize;
			overlap = 0d;
		}
		
		public Builder sampleOverlap(double rate) {
			overlap = rate;
			return this;
		}
		
		public Builder frameSize(int size) {
			this.frameSize = size;
			return this;
		}
		
		public FrameBuffer fromWavFile(String filename) throws IOException, WavFileException {
			return fromWavFile(WavFile.openWavFile(new File(filename)));
		}
		
		public FrameBuffer fromWavFile(WavFile wav) {
			FrameBuffer buffer = new WavFrameBuffer(wav, frameSize);
			
			if (overlap != 0d) {
				buffer = new OverlappingFrameBuffer(buffer, overlap);
			}
			
			return buffer;
		}
	}
	
	/**
	 * Returns the size of the next frame
	 * 
	 * @return
	 */
	public abstract int getFrameSize();
	
	/**
	 * Returns the number of frames in this buffer.
	 * 
	 * @return
	 */
	public abstract int getNumFrames();
	
	/**
	 * Returns sample rate
	 * 
	 * @return
	 */
	public abstract double getSampleRate();
	
	/**
	 * Returns # of frames in a second.
	 * 
	 * @return
	 */
	public abstract double getFramesPerSecond();
	

	/**
	 * Create new builder with specified frame size
	 * 
	 * @param size
	 * @return
	 */
	public static Builder frameSize(int size) {
		return new Builder(size);
	}
}
