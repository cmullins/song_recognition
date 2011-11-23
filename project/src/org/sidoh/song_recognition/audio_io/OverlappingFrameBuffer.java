package org.sidoh.song_recognition.audio_io;

import java.util.Arrays;
import java.util.Iterator;

public class OverlappingFrameBuffer extends FrameBuffer {
	
	private final FrameBuffer inner;
	private final double overlap;
	private final int overlapSize;
	private final int numFrames;
	private final double fps;

	public OverlappingFrameBuffer(FrameBuffer inner, double overlap) {
		if (overlap <= 0 || overlap >= 1) {
			throw new IllegalArgumentException("overlap should be > 0, < 1.");
		}
		
		this.overlap = overlap;
		this.inner = inner;	
		this.overlapSize = (int)(overlap * inner.getFrameSize());
		
		numFrames = (int)Math.floor((inner.getNumFrames() * inner.getFrameSize()) / (inner.getFrameSize() - overlapSize));
		
		double oldFps = inner.getFramesPerSecond();
		double growth = (numFrames / (double)inner.getNumFrames());
		
		fps = (oldFps * growth);
	}

	@Override
	public Iterator<double[]> iterator() {
		return new InnerIterator();
	}

	@Override
	public int getFrameSize() {
		return inner.getFrameSize();
	}
	
	@Override
	public int getNumFrames() {
		return numFrames;
	}

	@Override
	public double getSampleRate() {
		return inner.getSampleRate();
	}

	@Override
	public double getFramesPerSecond() {
		return fps;
	}
	
	private final class InnerIterator implements Iterator<double[]> {
		
		private double[] innerBuffer;
		private final double[] buffer;
		private double[] pBuffer;
		private int innerBufferIndex;
		private final Iterator<double[]> innerItr;
		private int i;
		
		public InnerIterator() {
			innerBufferIndex = getFrameSize();
			buffer = new double[getFrameSize()];
			pBuffer = new double[getFrameSize()];
			innerItr = inner.iterator();
			i = 0;
		}

		@Override
		public boolean hasNext() {
			return innerItr.hasNext();
		}

		@Override
		public double[] next() {
			if (innerBuffer == null) {
				innerBuffer = new double[getFrameSize()];
			}
			else {
				for (int i = 0; i < overlapSize; i++) {
					buffer[i] = pBuffer[pBuffer.length - overlapSize + i];
				}
			}
			
			for (int i = overlapSize; i < buffer.length; i++) {
				buffer[i] = nextValue();
			}
			
			System.arraycopy(buffer, 0, pBuffer, 0, buffer.length);
			return buffer;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Iterator#remove not implemented for OverlappingFrameBuffer#InnerIterator.");
		}
		
		private double nextValue() {
			if (innerBufferIndex >= innerBuffer.length) {
				innerBufferIndex = 0;
				innerBuffer = innerItr.next();
			}
			
			return innerBuffer[innerBufferIndex++];
		}
	}

}
