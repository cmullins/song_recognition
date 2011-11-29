package org.sidoh.song_recognition.signature;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.sidoh.collections.HashOfPriorityQueues;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

import com.google.common.collect.MinMaxPriorityQueue;

public class EvenlySpreadInFrequencyStarBuffer extends StarBuffer {
	
	protected static class Builder extends StarBuffer.Builder {

		public Builder(double starDensityFactor) {
			super(starDensityFactor);
		}
		
		public EvenlySpreadInFrequencyStarBuffer create(Spectrogram spec) {
			return new EvenlySpreadInFrequencyStarBuffer(spec, starDensityFactor);
		}
		
	}
	
	protected Map<Integer, Collection<Star>> stars;

	public EvenlySpreadInFrequencyStarBuffer(Spectrogram spec, double starDensityFactor) {
		super(spec);
		
		int perFreqSize = Math.max(1,(int)Math.floor((starDensityFactor*spec.getMaxTick()) / spec.getBinFloors().length));
		
		stars = Collections.synchronizedMap(new HashOfPriorityQueues<Integer, Star>(
					MinMaxPriorityQueue
						.orderedBy(new ConstellationMap.StarEnergyComparator(true))
						.maximumSize(perFreqSize), true));
	}

	@Override
	public void offerStar(Star s) {
		stars.get(s.getBin()).add(s);
	}

	@Override
	public Iterable<Star> flush() {
		Collection<Star> ret = new LinkedList<Star>();
		
		for (Collection<Star> fStars : stars.values()) {
			ret.addAll(fStars);
		}
		
		return ret;
	}

}
