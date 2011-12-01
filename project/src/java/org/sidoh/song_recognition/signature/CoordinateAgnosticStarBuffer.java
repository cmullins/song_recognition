package org.sidoh.song_recognition.signature;

import java.util.Collection;
import java.util.Collections;

import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.StarEnergyComparator;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

import com.google.common.collect.MinMaxPriorityQueue;

/**
 * This {@link StarBuffer} doesn't care where stars come from. It just keeps
 * track of the ones with the highest intensity using a {@link MinMaxPriorityQueue}.
 * 
 * @author chris
 *
 */
public class CoordinateAgnosticStarBuffer extends StarBuffer {
	
	protected static class Builder extends StarBuffer.Builder {
		public Builder(double starDensityFactor) {
			super(starDensityFactor);
		}
		
		@Override
		public StarBuffer create(Spectrogram spec) {
			return new CoordinateAgnosticStarBuffer(spec, starDensityFactor);
		}
	}
	
	private final double starDensityFactor;
	private final Collection<Star> pq;

	public CoordinateAgnosticStarBuffer(Spectrogram spec, double starDensityFactor) {
		super(spec);
		this.starDensityFactor = starDensityFactor;
		this.pq = Collections.synchronizedCollection(MinMaxPriorityQueue
			.orderedBy(new StarEnergyComparator(true))
			.maximumSize((int)Math.floor(spec.getMaxTick() * starDensityFactor))
			.create());
	}

	@Override
	public void offerStar(Star s) {
		pq.add(s);
	}

	@Override
	public Iterable<Star> flush() {
		return pq;
	}

}
