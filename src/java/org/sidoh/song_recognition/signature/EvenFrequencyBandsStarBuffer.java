package org.sidoh.song_recognition.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.TimeStarComparator;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class EvenFrequencyBandsStarBuffer extends StarBuffer {
	
	public static class Builder extends StarBuffer.Builder {
		private final StarBuffer.Builder inner;
		private int numBands;

		public Builder(int numBands, StarBuffer.Builder inner) {
			super(inner.starDensityFactor);
			this.numBands = numBands;
			this.inner = inner;
		}

		@Override
		public StarBuffer create(Spectrogram spec) {
			return new EvenFrequencyBandsStarBuffer(numBands, starDensityFactor, spec, inner);
		}
		
		@Override
		public Builder evenlyBanded(int numBands) {
			this.numBands = numBands;
			return this;
		}
		
		@Override
		public ManuallyBucketizedFrequencyBandsStarBuffer.Builder manuallyBanded(double[] a, double[] b) {
			throw new IllegalStateException("Can't decorate a banded star buffer with another banded star buffer!");
		}
		
		@Override
		public ManuallyBucketizedFrequencyBandsStarBuffer.Builder fairlyBanded() {
			throw new IllegalStateException("Can't decorate a banded star buffer with another banded star buffer!");
		}
	}
	
	private final StarBuffer[] innerBuffers;
	private final int numBands;
	private final int bandSize;

	public EvenFrequencyBandsStarBuffer(int numBands, 
			double densityFactor, 
			Spectrogram spec, 
			StarBuffer.Builder inner) {
		super(spec);
		this.numBands = numBands;
		this.bandSize = spec.getBinFloors().length / numBands;
		
		// Density factor for inner builder will need to be divided amongst the bands
		inner = inner.starDensityFactor(densityFactor / numBands);
		
		// Initialize bands
		innerBuffers = new StarBuffer[numBands];
		for (int i = 0; i < innerBuffers.length; i++) {
			innerBuffers[i] = inner.create(spec);
		}
	}

	@Override
	public void offerStar(Star s) {
		innerBuffers[getBandIndex(s)].offerStar(s);
	}

	@Override
	public Iterable<Star> flush() {
		List<Star> allStars = new ArrayList<Star>();
		
		for (StarBuffer innerBuffer : innerBuffers) {
			for (Star s : innerBuffer.flush()) {
				allStars.add(s);
			}
		}
		
		Collections.sort(allStars, new TimeStarComparator());
		
		return allStars;
	}
	
	/**
	 * Computes the band that a star falls into
	 * 
	 * @param s
	 * @return
	 */
	protected int getBandIndex(Star s) {
		return Math.min(numBands-1, s.getBin() / bandSize);
	}
}
