package org.sidoh.song_recognition.spectrogram;

/**
 * This decorator class adds the ability to twiddle the intensity values of a
 * {@link Spectrogram} by applying brightness, contrast, and some other settings.
 * 
 * @author chris
 */
public class ConfigurableSpectrogram extends Spectrogram {
	
	/**
	 * Types of scales one can use
	 * 
	 * @author chris
	 *
	 */
	public static enum Scale {
		LOG, LINEAR, SQUARE;
		
		/**
		 * applies the scaling function to the value.
		 * 
		 * @param value
		 * @return
		 */
		public double transform(double value) {
			switch (this) {
			case LOG:
				return Math.log1p(value);
			case LINEAR:
				return value;
			case SQUARE:
				return value*value;
			default:
				throw new UnsupportedOperationException("The scale: " + this.toString() + " isn't implemented!");
			}
		}
	}
	
	private Spectrogram inner;
	
	// Settings
	private double brightness = 0.0;
	private double contrast   = 1.0;
	private Scale scale       = Scale.LINEAR;
	private boolean invert    = false;
	
	public ConfigurableSpectrogram(Spectrogram inner) {
		this.inner = inner;
	}

	@Override
	public int getMaxTick() {
		return inner.getMaxTick();
	}

	@Override
	public int ticksInSeconds() {
		return inner.ticksInSeconds();
	}
	
	@Override
	public double tickToSeconds(int tick) {
		return inner.tickToSeconds(tick);
	}

	@Override
	public int getMaxFrequency() {
		return inner.getMaxFrequency();
	}

	@Override
	public double[] getBinFloors() {
		return inner.getBinFloors();
	}

	@Override
	public double getIntensity(int time, int bin) {
		double innerValue = scale.transform(inner.getIntensity(time, bin));
		
		return getIntensity(innerValue);
	}

	@Override
	public double getIntensity(int time, double frequency) {
		double innerValue = scale.transform(inner.getIntensity(time, frequency));
		
		return getIntensity(innerValue);
	}
	
	protected double getIntensity(double raw) {
		raw = (brightness + (contrast * raw));
		if (invert) {
			raw = 1.0d - raw;
		}
		return raw;
	}
	
	@Override
	public int secondsToTick(double second) {
		return inner.secondsToTick(second);
	}

	@Override
	public int frequencyToBin(double frequency) {
		return inner.frequencyToBin(frequency);
	}

	@Override
	public double binToFrequency(int bin) {
		return inner.binToFrequency(bin);
	}

	//
	// Getters / setters
	//
	
	public double getBrightness() {
		return brightness;
	}

	public ConfigurableSpectrogram setBrightness(double brightness) {
		this.brightness = brightness;
		return this;
	}

	public double getContrast() {
		return contrast;
	}

	public ConfigurableSpectrogram setContrast(double contrast) {
		this.contrast = contrast;
		return this;
	}

	public Scale getScale() {
		return scale;
	}

	public ConfigurableSpectrogram setScale(Scale scale) {
		this.scale = scale;
		return this;
	}
	
	public boolean isInverted() {
		return invert; 
	}
	
	public ConfigurableSpectrogram setInverted(boolean invert) {
		this.invert = invert;
		return this;
	}
	
	public ConfigurableSpectrogram invert() {
		this.invert = true;
		return this;
	}

	@Override
	public Spectrogram free() {
		inner = inner.free();
		return this;
	}
}
