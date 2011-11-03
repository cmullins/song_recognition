package org.sidoh.song_recognition.signature;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.sidoh.collections.HashOfPriorityQueues;
import org.sidoh.peak_detection.PeakDetector;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.StarEnergyComparator;
import org.sidoh.song_recognition.spectrogram.FrameBufferSpectrogram;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

import com.google.common.collect.MinMaxPriorityQueue;

public class ConstellationMapExtractor implements SpectrogramSignatureExtractor<ConstellationMapSignature> {
	
	private final PeakDetector peakDetector;
	private final double starDensityFactor;
	
	public ConstellationMapExtractor(PeakDetector peakDetector, double starDensityFactor) {
		this.peakDetector = peakDetector;
		this.starDensityFactor = starDensityFactor;
	}
	
	@Override
	public ConstellationMapSignature extractSignature(FrameBuffer frames) {
		Spectrogram spec = new FrameBufferSpectrogram(frames);
		return extractSignature(spec);
	}

	@Override
	public ConstellationMapSignature extractSignature(Spectrogram spec) {
		return new ConstellationMapSignature(findStars(spec), spec);
	}
	
	/**
	 * Helper method to populate a ConstellationMap from the peaks that the {@link PeakDetector}
	 * provides.
	 * 
	 * @param spec
	 * @return
	 */
	protected ConstellationMap findStars(Spectrogram spec) {
		ConstellationMap map = new ConstellationMap();
		StarBuffer stars = new SpreadStarBuffer(spec);
		
		double[] binFloors = spec.getBinFloors();
		int numBins = binFloors.length;
		
		for (int bin = 0; bin < numBins; bin++) {
			Iterable<Integer> peaks = peakDetector.findPeaks(new FixedFrequencyValues(spec, bin));
			
			for (Integer tick : peaks) {
				stars.add(tick, getStar(spec, tick, bin));
			}
		}
		
		map.addAll(stars.flush());
		
		return map;
	}
	
	/**
	 * Get the maximum number of stars per second of audio. This is computed using
	 * {@link #starDensityFactor}, which is provided in the constructor.
	 * 
	 * @param spec
	 * @return
	 */
	protected int getStarsPerSecond(Spectrogram spec) {
		return (int)Math.floor(
				(starDensityFactor * spec.getMaxTick()) 
					/ 
				(double)spec.tickToSeconds(spec.getMaxTick()));
	}
	
	protected int getMaxStars(Spectrogram spec) {
		return (int)Math.floor(starDensityFactor*spec.getMaxTick());
	}
	
	protected static boolean isSecondBoundary(Spectrogram spec, int tick) {
		return tick > 0 && ((tick % spec.ticksInSeconds()) == 0);
	}
	
	/**
	 * 
	 * @param spec
	 * @param x
	 * @param y
	 * @return
	 */
	protected Star getStar(final Spectrogram spec, final int x, final int y) {
		double seconds = spec.tickToSeconds(x);
		double freq    = spec.binToFrequency(y);
		return new Star(seconds, freq, spec.getIntensity(x, y), x, y);
	}
	
	protected class StarBuffer {
		protected HashOfPriorityQueues<Integer, Star> starsBySecond;
		protected Spectrogram spec;
		
		protected StarBuffer() {
			this.spec = null;
		}
		
		public StarBuffer(Spectrogram spec) {
			this.spec = spec;
			int size = getStarsPerSecond(spec);
			starsBySecond = new HashOfPriorityQueues<Integer, Star>(
				MinMaxPriorityQueue
					.orderedBy(new StarEnergyComparator(true))
					.maximumSize(size));
		}
		
		public void add(int tick, Star s) {
			int nearestSecond = (int)Math.floor(spec.tickToSeconds(tick));
			starsBySecond.addFor(nearestSecond, s);
		}
		
		public Collection<Star> flush() {
			Collection<Star> ret = new LinkedList<Star>();
			for (Collection<Star> secondStars : starsBySecond.values()) {
				ret.addAll(secondStars);
			}
			return ret;
		}
	}
	
	protected class SpreadStarBuffer extends StarBuffer {
		protected MinMaxPriorityQueue<Star> pq;
		
		public SpreadStarBuffer(Spectrogram spec) {
			this.spec = spec;
			int size  = getMaxStars(spec);
			pq = MinMaxPriorityQueue
				.orderedBy(new StarEnergyComparator(true))
				.maximumSize(size)
				.create();
		}
		
		@Override
		public void add(int tick, Star s) {
			pq.add(s);
		}
		
		@Override
		public Collection<Star> flush() {
			Collection<Star> ret = new LinkedList<Star>(pq);
			pq.clear();
			return ret;
		}
	}
	
	public static class FixedFrequencyValues implements Iterable<Double> {
		
		private final Spectrogram spec;
		private final int bin;

		public FixedFrequencyValues(Spectrogram spec, int bin) {
			this.spec = spec;
			this.bin = bin;
		}

		@Override
		public Iterator<Double> iterator() {
			return new InnerIterator();
		}
		
		protected class InnerIterator implements Iterator<Double> {
			
			private int tick = 0;

			@Override
			public boolean hasNext() {
				return !(tick >= spec.getMaxTick());
			}

			@Override
			public Double next() {
				return spec.getIntensity(tick++, bin);
			}

			@Override
			public void remove() {
				throw new RuntimeException("remove() isn't implemented.");
			}
			
		}
	}
}
