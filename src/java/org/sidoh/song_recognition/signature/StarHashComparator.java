package org.sidoh.song_recognition.signature;

import java.util.Map;
import java.util.Set;

import org.sidoh.math.Histogram;

public class StarHashComparator implements LikenessComparator<StarHashSignature> {
	private static final long serialVersionUID = 7842677071829673521L;
	private HistogramScorer scorer;
	
	public StarHashComparator(HistogramScorer scorer) {
		this.scorer = scorer;
	}
	
	@Override
	public double similarity(StarHashSignature o1, StarHashSignature o2) {
		Map<Integer, Set<Integer>> hashes1 = o1.getStarHashes();
		Map<Integer, Set<Integer>> hashes2 = o2.getStarHashes();
		
		if (hashes1.size() < hashes2.size()) {
			return similarity(o2, o1);
		}
		
		Histogram offsets = new Histogram(Math.max(hashes1.size(), hashes2.size()));
		for (Integer hash : hashes1.keySet()) {
			if (hashes2.containsKey(hash)) {
				Set<Integer> offsets1 = hashes1.get(hash);
				Set<Integer> offsets2 = hashes2.get(hash);
			
				for (Integer offset1 : offsets2) {
					for (Integer offset2 : offsets1) {
						offsets.addValue(offset2 - offset1);
					}
				}
			}
		}
		
		return scorer.score(offsets);
	}
	
	public StarHashComparator copy() {
		return new StarHashComparator(scorer);
	}

	public StarHashComparator setHistogramScorer(HistogramScorer scorer) {
		this.scorer = scorer;
		return this;
	}
}
