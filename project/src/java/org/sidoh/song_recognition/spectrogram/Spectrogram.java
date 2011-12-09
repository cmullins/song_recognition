package org.sidoh.song_recognition.spectrogram;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.transform.Transform;

public abstract class Spectrogram {
	public static class Builder {
		private Transform.Builder transformBuilder = Transform.Builder.defaultBuilder();
		private final SpectrogramStorage.Builder storageBuilder;
		private int tickHistory;
		private boolean configured = false;
		private int contrast = 1;
		private int brightness = 0;
		private boolean inverted = false;
		private ConfigurableSpectrogram.Scale scale = ConfigurableSpectrogram.Scale.LINEAR;
		private ProgressNotifier.Builder notifier;
		
		private Builder(SpectrogramStorage.Builder storageBuilder) {
			this.storageBuilder = storageBuilder;
			this.tickHistory = -1;
			this.notifier = ProgressNotifier.nullNotifier();
		}
		
		private Builder(SpectrogramStorage.Builder storageBuilder, int tickHistory) {
			this.storageBuilder = storageBuilder;
			this.tickHistory = tickHistory;
		}
		
		public Builder transformBuilder(Transform.Builder builder) {
			this.transformBuilder = builder;
			return this;
		}
		
		public Builder copy() {
			Builder b = new Builder(storageBuilder, tickHistory);
			b.transformBuilder = this.transformBuilder;
			b.tickHistory = this.tickHistory;
			b.configured = this.configured;
			b.contrast = this.contrast;
			b.brightness = this.brightness;
			b.scale = this.scale;
			b.notifier = this.notifier;
			
			return b;
		}
		
		public Builder brightness(int brightness) {
			configured = true;
			this.brightness = brightness;
			return this;
		}
		
		public Builder contrast(int contrast) {
			configured = true;
			this.contrast = contrast;
			return this;
		}
		
		public Builder scale(ConfigurableSpectrogram.Scale scale) {
			configured = true;
			this.scale = scale;
			return this;
		}
		
		public Builder invert() {
			configured = true;
			this.inverted = true;
			return this;
		}
		
		public Builder progressNotifier(ProgressNotifier.Builder notifier) {
			this.notifier = notifier;
			return this;
		}
		
		public Spectrogram create(FrameBuffer buffer) {
			Spectrogram spec;
			
			if (storageBuilder instanceof BufferedSpectrogramStorage.Builder) {
				spec = new MemoryLimitedFrameBufferSpectrogram(buffer, tickHistory);
			}
			else {
				spec = new FrameBufferSpectrogram(buffer, transformBuilder, storageBuilder, notifier);
			}
			
			if (configured) {
				spec = new ConfigurableSpectrogram(spec)
				 .setBrightness(brightness)
				 .setContrast(contrast)
				 .setScale(scale)
				 .setInverted(inverted);
			}
			
			return spec;
		}
	}
	/**
	 * Returns the maximum time that appears in this spectrogram.
	 * 
	 * @return
	 */
	public abstract int getMaxTick();
	
	/**
	 * Returns how many ticks are in a second.
	 * 
	 * @return
	 */
	public abstract int ticksInSeconds();
	
	/**
	 * Returns the maximum frequency this spectrogram tracks.
	 * 
	 * @return
	 */
	public abstract int getMaxFrequency();
	
	/**
	 * Converts a tick to seconds.
	 * 
	 * @param tick
	 * @return
	 */
	public abstract double tickToSeconds(int tick);
	
	/**
	 * 
	 * @param second
	 * @return
	 */
	public abstract int secondsToTick(double second);
	
	/**
	 * 
	 * @param frequency
	 * @return
	 */
	public abstract int frequencyToBin(double frequency);
	
	/**
	 * Returns a list of buckets that the frequencies fall into.
	 * 
	 * @return
	 */
	public abstract double[] getBinFloors();
	
	/**
	 * 
	 * @param bin
	 * @return
	 */
	public abstract double binToFrequency(int bin);
	
	/**
	 * Returns the intensity value at a particular point.
	 * 
	 * @param time
	 * @param frequency
	 * @return
	 */
	public abstract double getIntensity(int time, double frequency);
	
	/**
	 * Returns the intensity value at a particular time/bin.
	 * 
	 * @param time
	 * @param bin
	 * @return
	 */
	public abstract double getIntensity(int time, int bin);
	
	/**
	 * Free memory by releasing the intensity values.
	 * 
	 */
	public abstract Spectrogram free();
	
	/**
	 * Get Spectrogram builder that uses a limited-memory buffer.
	 * 
	 * @param maxTickHistory
	 * @return
	 */
	public static Builder memoryLimited(int maxTickHistory) {
		return new Builder(SpectrogramStorage.Builder.buffered(maxTickHistory), maxTickHistory);
	}
	
	/**
	 * Get Spectrogram builder that puts everything in its own block of memory. This
	 * is usually really expensive and probably shouldn't be used unless absolutely
	 * necessary. You'll probably want to use {@link #singletonStorage()} instead.
	 * 
	 * @return
	 */
	public static Builder inMemory() {
		return new Builder(SpectrogramStorage.Builder.inMemory());
	}
	
	/**
	 * Gets a Builder that creates Spectrograms that use the same block of memory to
	 * store values. This is useful when one is processing Spectrograms one after the
	 * other, and doesn't need to access stuff in parallel. Avoids the overhead of
	 * tons of freeing/allocating memory.
	 * 
	 * @return
	 */
	public static Builder singletonStorage() {
		return new Builder(SpectrogramStorage.Builder.singleton());
	}
}
