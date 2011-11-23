package org.sidoh.song_recognition.signature;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.sidoh.collections.TreeMapOfPriorityQueues;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.StarEnergyComparator;
import org.sidoh.song_recognition.signature.CoordinateAgnosticStarBuffer.Builder;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

import com.google.common.collect.MinMaxPriorityQueue;

/**
 * This {@link StarBuffer} equally distributes {@link Star}s throughout the
 * ticks in a {@link Spectrogram}. That is, each tick will be allocated the
 * same capacity for stars, and the capacity will be reset after each 
 * SECOND (not tick, there are almost always many ticks in a second).
 * 
 * This is potentially dangerous because it isn't translation invariant,
 * and could induce unwanted anomalies in the {@link ConstellationMap}.
 * 
 */
public class EvenlySpreadInTimeStarBuffer extends StarBuffer {
	
	protected static class Builder extends StarBuffer.Builder {
		public Builder(double starDensityFactor) {
			super(starDensityFactor);
		}

		@Override
		public StarBuffer create(Spectrogram spec) {
			return new EvenlySpreadInTimeStarBuffer(spec, starDensityFactor);
		}
		
	}
	
	private final double starDensityFactor;
	private final Map<Integer, Collection<Star>> stars;

	/**
	 * 
	 * @param spec
	 * @param starDensityFactor maximum number of stars per tick. this capacity will be
	 *  evenly distributed throughout all of the second boundaries.
	 */
	public EvenlySpreadInTimeStarBuffer(Spectrogram spec, double starDensityFactor) {
		super(spec);
		this.starDensityFactor = starDensityFactor;
		
		int starsPerSecond = (int)Math.floor(
			spec.getMaxTick() * starDensityFactor / spec.tickToSeconds(spec.getMaxTick()));
		
		this.stars = Collections.synchronizedMap(new TreeMapOfPriorityQueues<Integer, Star>(
			MinMaxPriorityQueue
				.orderedBy(new StarEnergyComparator(true))
				.maximumSize(starsPerSecond),
			true));
	}

	@Override
	public void offerStar(Star s) {
		stars.get(getSecond(s)).add(s);
	}

	@Override
	public Iterable<Star> flush() {
		Collection<Star> stars = new LinkedList<Star>();
		
		for (Collection<Star> starsInSecond : this.stars.values()) {
			stars.addAll(starsInSecond);
		}
		
		return stars;
	}

	protected int getSecond(Star s) {
		return (int)Math.floor(s.getTime());
	}
}
