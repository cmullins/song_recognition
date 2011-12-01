package org.sidoh.song_recognition.spectrogram;

import java.util.Arrays;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.OverlappingFrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.audio_io.WavFrameBuffer;
import org.sidoh.song_recognition.spectrogram.SpectrogramStorage.Builder;
import org.sidoh.song_recognition.transform.DiscreteCosineTransform;
import org.sidoh.song_recognition.transform.Transform;
import org.sidoh.song_recognition.transform.VorbisSmoothingTransform;

/**
 * Implements {@link Spectrogram} for a {@link WavFile}.
 * 
 * @author chris
 */
public class FrameBufferSpectrogram extends Spectrogram {
	
	private final Transform transform;
	private final double[] binFloors;
	private SpectrogramStorage store;
	private final FrameBuffer buffer;
	private final double bucketSize;
	private final SpectrogramStorage.Builder storageBuilder;
	private final org.sidoh.io.ProgressNotifier.Builder progress;
	
	protected FrameBufferSpectrogram(FrameBuffer buffer, SpectrogramStorage.Builder storage, ProgressNotifier.Builder progress) {
		this(buffer, Transform.Builder.defaultBuilder(), storage, progress);
	}
	
	protected FrameBufferSpectrogram(FrameBuffer buffer, 
			Transform.Builder transformBuilder,
			SpectrogramStorage.Builder storageBuilder,
			ProgressNotifier.Builder progress) {
		this.storageBuilder = storageBuilder;
		this.progress = progress;
		transform = transformBuilder.create(buffer.getFrameSize(), buffer.getSampleRate());
		binFloors = transform.getBinFloors();
		bucketSize = getMaxFrequency() / (double)binFloors.length;
		this.buffer = buffer;
		
		if (storageBuilder instanceof BufferedSpectrogramStorage.Builder) {
			throw new IllegalArgumentException("FrameBufferSpectrogram incompatable with BufferedSpectrogramStorage!");
		}
		
		store = storageBuilder.create(getMaxFrequency(), binFloors.length);
		compute();
	}

	@Override
	public int getMaxTick() {
		if (store.getMaxTick() == 0) {
			return (int)buffer.getNumFrames();
		}
		else {
			return store.getMaxTick();
		}
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
		
		ProgressNotifier tracker = progress.create(
				String.format("Creating spectrogram (length = %.1f seconds, sample rate = %.2f fps)", 
						buffer.getNumFrames()/buffer.getFramesPerSecond(),
						buffer.getFramesPerSecond()), 
				buffer.getNumFrames());
		
		for (double[] frames : buffer) {
			transform.transform(frames);
			
			for (int i = 0; i < binFloors.length; i++) {
				store.put(tick, i, Math.abs(frames[i]));
			}
			tick++;
			
			tracker.update(tick);
		}
		
		tracker.complete();
	}

	@Override
	public Spectrogram free() {
		store.free();
		return this;
	}
}
