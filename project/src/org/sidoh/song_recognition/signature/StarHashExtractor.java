package org.sidoh.song_recognition.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sidoh.collections.TreeMapOfSets;
import org.sidoh.io.ProgressNotifier;
import org.sidoh.io.ProgressNotifier.Builder;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.TimeStarComparator;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class StarHashExtractor implements SpectrogramSignatureExtractor<StarHashSignature> {
	
	private final ConstellationMapExtractor starExtractor;
	private final Region.Builder regionBuilder;
	private final int timeResolution;
	private final ProgressNotifier.Builder progress;

	public StarHashExtractor(ConstellationMapExtractor starExtractor, 
			Region.Builder regionBuilder, 
			int timeResolution,
			ProgressNotifier.Builder progress) {
		this.starExtractor = starExtractor;
		this.regionBuilder = regionBuilder;
		this.timeResolution = timeResolution;
		this.progress = progress;
	}
	
	@Override
	public StarHashSignature extractSignature(Spectrogram spec) {
		ConstellationMapSignature starSig = starExtractor.extractSignature(spec);
		List<Star> stars = new ArrayList<Star>(starSig.getConstellationMap().getStars());
		Collections.sort(stars, new TimeStarComparator());
		
		ProgressNotifier notifier = progress.create("Extracting hash values...",stars.size());
		int i = 0;
		
		Set<Region> regions = new HashSet<Region>();
		TreeMapOfSets<Integer, Integer> starHashes = new TreeMapOfSets<Integer, Integer>();
		for (Star s : stars) {
			Set<Region> toRemove = new HashSet<Region>();
			for (Region region : regions) {
				switch (region.isInRegion(s.getTick(), s.getBin())) {
				case IN_REGION:
					int hash = StarHashSignature.computeHash(
							region.getX(), s.getTick(), 
							region.getY(), s.getBin(),
							spec);
					starHashes.addFor(hash, getTime(s));
					break;
				case AFTER_REGION:
					toRemove.add(region);
				}
			}
			regions.removeAll(toRemove);
			regions.add(regionBuilder.create(s.getTick(), s.getBin()));
			
			notifier.update(i++);
		}
		
		notifier.complete();
		
		return new StarHashSignature(starSig.getConstellationMap(), starHashes);
	}

	protected int getTime(Star s) {
		return (int)Math.floor(s.getTime() * timeResolution);
	}
}
