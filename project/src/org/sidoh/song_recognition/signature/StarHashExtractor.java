package org.sidoh.song_recognition.signature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sidoh.collections.HashOfSets;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.TimeStarComparator;
import org.sidoh.song_recognition.spectrogram.FrameBufferSpectrogram;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class StarHashExtractor implements SpectrogramSignatureExtractor<StarHashSignature> {
	
	private final ConstellationMapExtractor starExtractor;
	private final Region.Builder regionBuilder;

	public StarHashExtractor(ConstellationMapExtractor starExtractor, Region.Builder regionBuilder) {
		this.starExtractor = starExtractor;
		this.regionBuilder = regionBuilder;
		
	}

	@Override
	public StarHashSignature extractSignature(FrameBuffer frames) {
		return extractSignature(new FrameBufferSpectrogram(frames));
	}
	
	@Override
	public StarHashSignature extractSignature(Spectrogram spec) {
		ConstellationMapSignature starSig = starExtractor.extractSignature(spec);
		List<Star> stars = new ArrayList<Star>(starSig.getConstellationMap().getStars());
		Collections.sort(stars, new TimeStarComparator());
		
		Set<Region> regions = new HashSet<Region>();
		HashOfSets<Integer, Star> starHashes = new HashOfSets<Integer, Star>();
		for (Star s : stars) {
			Set<Region> toRemove = new HashSet<Region>();
			for (Region region : regions) {
				switch (region.isInRegion(s.getTick(), s.getBin())) {
				case IN_REGION:
					int hash = StarHashSignature.computeHash(
							region.getX(), s.getTick(), 
							region.getY(), s.getBin(),
							spec);
					starHashes.addFor(hash, s);
					break;
				case AFTER_REGION:
					toRemove.add(region);
				}
			}
			regions.removeAll(toRemove);
			regions.add(regionBuilder.create(s.getTick(), s.getBin()));
		}
		
		return new StarHashSignature(starSig.getConstellationMap(), starHashes);
	}

	
}
