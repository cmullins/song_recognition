package org.sidoh.song_recognition.spectrogram;

import java.util.Iterator;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.transform.Transform;

public class MemoryLimitedFrameBufferSpectrogram extends Spectrogram {
	
	private final Transform transform;
	private final double[] binFloors;
	private SpectrogramStorage store;
	private final FrameBuffer buffer;
	private final double bucketSize;
	private final int tickHistory;
	private Iterator<double[]> bufferItr;
	
	protected MemoryLimitedFrameBufferSpectrogram(FrameBuffer buffer, int tickHistory) {
		this(buffer, tickHistory, Transform.Builder.defaultBuilder());
	}
	
	protected MemoryLimitedFrameBufferSpectrogram(FrameBuffer buffer, int tickHistory, Transform.Builder transformBuilder) {
		this.tickHistory = tickHistory;
		transform = transformBuilder.create(buffer.getFrameSize(), buffer.getSampleRate());
		binFloors = transform.getBinFloors();
		bucketSize = getMaxFrequency() / (double)binFloors.length;
		this.buffer = buffer;
		this.bufferItr = buffer.iterator();
		
		store = new BufferedSpectrogramStorage(getMaxFrequency(), binFloors.length, tickHistory);
	}

	@Override
	public int getMaxTick() {
		return buffer.getNumFrames();
	}

	@Override
	public int ticksInSeconds() {
		return (int)buffer.getFramesPerSecond();
	}

	@Override
	public int getMaxFrequency() {
		return (int)binFloors[binFloors.length - 1];
	}

	@Override
	public double tickToSeconds(int tick) {
		return tick / (double)ticksInSeconds();
	}

	@Override
	public int secondsToTick(double second) {
		return (int)Math.floor(second * ticksInSeconds());
	}

	@Override
	public int frequencyToBin(double frequency) {
		return (int)Math.floor(frequency / bucketSize);
	}

	@Override
	public double binToFrequency(final int bin) {
		return binFloors[Math.min(binFloors.length-1, bin)];
	}

	@Override
	public double getIntensity(int time, int bin) {
		advanceIfRequired(time);
		return store.get(time, bin);
	}

	@Override
	public double getIntensity(int time, double frequency) {
		advanceIfRequired(time);
		return store.get(time, frequency);
	}

	@Override
	public double[] getBinFloors() {
		return binFloors;
	}

	@Override
	public Spectrogram free() {
		store.free();
		return this;
	}
	
	/**
	 * Runs the transform if need be.
	 * 
	 */
	protected synchronized void advanceIfRequired(int tick) {
		if (tick > store.getMaxTick()) {
			if (! bufferItr.hasNext()) {
				throw new IllegalStateException("Trying to advance beyond end of buffer!");
			}

			double[] frame = bufferItr.next();
			frame = transform.transform(frame);
			
			for (int bin = 0; bin < frame.length; bin++) {
				store.put(tick, bin, frame[bin]);
			}
		}
	}

}
