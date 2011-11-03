package org.sidoh.song_recognition.audio_io;

import java.io.IOException;
import java.util.Iterator;

public class WavFrameBuffer implements FrameBuffer {
	
	private final WavFile wav;
	private final int windowSize;

	public WavFrameBuffer(WavFile wav, int windowSize) {
		this.wav = wav;
		this.windowSize = windowSize;
	}

	@Override
	public Iterator<double[]> iterator() {
		return new WavFrameIterator();
	}

	@Override
	public int getFrameSize() {
		return windowSize;
	}

	@Override
	public int getNumFrames() {
		return (int)(wav.getNumFrames() / getFrameSize());
	}

	@Override
	public double getSampleRate() {
		return wav.getSampleRate();
	}

	@Override
	public double getFramesPerSecond() {
		return getSampleRate() / (double)getFrameSize();
	}

	private final class WavFrameIterator implements Iterator<double[]> {
		
		private final double[] buffer;
		
		public WavFrameIterator() {
			this.buffer = new double[windowSize];
		}

		@Override
		public boolean hasNext() {
			return wav.getFramesRemaining() > 0;
		}

		@Override
		public double[] next() {
			try {
				wav.readFrames(buffer, windowSize);
				return buffer;
			}
			catch (WavFileException e) {
				throw new RuntimeException(e);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Iterator#remove() not supported in WavFrameIterator.");
		}
		
	}
}
