package org.sidoh.song_recognition.signature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import org.sidoh.collections.Pair;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.EvenFrequencyBandsStarBuffer.Builder;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class ManuallyBucketizedFrequencyBandsStarBuffer extends StarBuffer {
	
	public static class Builder extends StarBuffer.Builder {

		private double[] bandSizes;
		private double[] bandWeights;
		private final StarBuffer.Builder innerBuilder;

		public Builder(double starDensityFactor, 
				double[] bandSizes, 
				double[] bandWeights, 
				StarBuffer.Builder innerBuilder) {
			super(starDensityFactor);
			this.bandSizes = bandSizes;
			this.bandWeights = bandWeights;
			this.innerBuilder = innerBuilder;
		}

		@Override
		public StarBuffer create(Spectrogram spec) {
			return new ManuallyBucketizedFrequencyBandsStarBuffer(starDensityFactor, bandSizes, bandWeights, innerBuilder, spec);
		}
		
		@Override
		public EvenFrequencyBandsStarBuffer.Builder evenlyBanded(int numBands) {
			throw new IllegalStateException("Can't decorate a banded star buffer with another banded star buffer!");
		}
		
		@Override
		public ManuallyBucketizedFrequencyBandsStarBuffer.Builder manuallyBanded(double[] bandSizes, double[] bandWeights) {
			this.bandSizes = bandSizes;
			this.bandWeights = bandWeights;
			return this;
		}
		
		@Override
		public ManuallyBucketizedFrequencyBandsStarBuffer.Builder fairlyBanded() {
			return this.manuallyBanded(fairSizes, fairWeights);
		}
	}
	
	// Multiple references to the same objects to optimize performance.
	private final StarBuffer[] innerBuffers;
	
	// A plain ol' list of all of the inner star buffers
	private final Collection<StarBuffer> innerBufferList;
	private final StarBuffer.Builder innerBuilder;
	
	public ManuallyBucketizedFrequencyBandsStarBuffer(double starDensityFactor,
			double[] bandSizes,
			double[] bandWeights,
			StarBuffer.Builder innerBuilder,
			Spectrogram spec) {
		super(spec);
		this.innerBuilder = innerBuilder;
		this.innerBuffers = new StarBuffer[spec.getBinFloors().length];
		this.innerBufferList = new ArrayList<StarBuffer>(bandSizes.length);
		
		// Compute the end indexes of the bands given the percentage sizes
		Deque<Pair<Double, Integer>> buckets = new LinkedList<Pair<Double, Integer>>();
		int lastStart = 0;
		
		for (int i = 0; i < bandSizes.length; i++) {
			int nextStart = lastStart + (int)(bandSizes[i] * innerBuffers.length);
			double density = bandWeights[i] * starDensityFactor;
			buckets.addLast(Pair.create(density, nextStart));
			lastStart = nextStart;
		}
		
		if (buckets.peekLast().getV2() < innerBuffers.length) {
			Pair<Double, Integer> old = buckets.pollLast();
			buckets.addLast(Pair.create(old.getV1(), innerBuffers.length));
		}
		
		StarBuffer currentBuffer = getStarBuffer(buckets.peekFirst());
		innerBufferList.add(currentBuffer);
		for (int i = 0; i < innerBuffers.length; i++) {
			if (i >= buckets.peekFirst().getV2()) {
				buckets.pollFirst();
				currentBuffer = getStarBuffer(buckets.peekFirst());
				innerBufferList.add(currentBuffer);
			}
			innerBuffers[i] = currentBuffer;
		}
	}
	
	protected StarBuffer getStarBuffer(Pair<Double, Integer> bucket) {
		return innerBuilder.starDensityFactor(bucket.getV1()).create(spec);
	}

	@Override
	public void offerStar(Star s) {
		getInnerBuffer(s).offerStar(s);
	}

	@Override
	public Iterable<Star> flush() {
		Collection<Star> r = new ArrayList<Star>();
		
		for (StarBuffer innerBuffer : innerBufferList) {
			for (Star s : innerBuffer.flush()) {
				r.add(s);
			}
		}
		
		return r;
	}

	protected StarBuffer getInnerBuffer(Star s) {
		int bucket = s.getBin();
		return innerBuffers[bucket];
	}
}
