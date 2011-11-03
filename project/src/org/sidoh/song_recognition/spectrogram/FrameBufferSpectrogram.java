package org.sidoh.song_recognition.spectrogram;

import java.util.Arrays;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.OverlappingFrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.audio_io.WavFrameBuffer;
import org.sidoh.song_recognition.transform.DiscreteCosineTransform;
import org.sidoh.song_recognition.transform.Transform;
import org.sidoh.song_recognition.transform.VorbisSmoothingTransform;

/**
 * Implements {@link Spectrogram} for a {@link WavFile}.
 * 
 * @author chris
 */
public class FrameBufferSpectrogram implements Spectrogram {
	
	private final Transform transform;
	private final double[] binFloors;
	private SpectrogramStorage store;
	private final FrameBuffer buffer;
	private final double bucketSize;
	
	public FrameBufferSpectrogram(FrameBuffer buffer) {
		this(buffer, Transform.Builder.defaultBuilder());
	}
	
	public FrameBufferSpectrogram(FrameBuffer buffer, Transform.Builder transformBuilder) {
		transform = transformBuilder.create(buffer.getFrameSize(), buffer.getSampleRate());
		binFloors = transform.getBinFloors();
		bucketSize = getMaxFrequency() / (double)binFloors.length;
		this.buffer = buffer;
		
		store = new InMemorySpectrogramStorage(getMaxFrequency(), binFloors.length);
		compute();
	}

	@Override
	public int getMaxTick() {
		return (int)store.getMaxTick();
	}

	@Override
	public int ticksInSeconds() {
		return (int)buffer.getFramesPerSecond();
	}
	
	@Override
	public double tickToSeconds(final int tick) {
		return tick / (double)ticksInSeconds();
	}

	@Override
	public int getMaxFrequency() {
		return (int)binFloors[binFloors.length - 1];
	}

	@Override
	public double getIntensity(int time, int bin) {
		return store.get(time, bin);
	}

	@Override
	public double getIntensity(int time, double frequency) {
		return store.get(time, frequency);
	}

	@Override
	public double[] getBinFloors() {
		return Arrays.copyOf(binFloors, binFloors.length);
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

	private void compute() {
		int tick = 0;
		for (double[] frames : buffer) {
			transform.transform(frames);
			
			for (int i = 0; i < binFloors.length; i++) {
				store.put(tick, i, Math.abs(frames[i]));
			}
			tick++;
		}
	}

	@Override
	public Spectrogram free() {
		store.free();
		return this;
	}
}
