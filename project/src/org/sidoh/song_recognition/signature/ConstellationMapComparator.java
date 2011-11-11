package org.sidoh.song_recognition.signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMap.TimeStarComparator;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class ConstellationMapComparator implements LikenessComparator<ConstellationMapSignature> {
	private static final long serialVersionUID = -1023025485046488094L;
	private static final Comparator<Star> starSorter = new TimeStarComparator();
	private final double matchThreshold;
	
	public ConstellationMapComparator(double matchThreshold) {
		this.matchThreshold = matchThreshold;
	}

	public double similarity(ConstellationMapSignature o1, ConstellationMapSignature o2) {
		return naiveCompare(o1, o2);
	}

	protected double naiveCompare(ConstellationMapSignature o1, ConstellationMapSignature o2) {
		ConstellationMap map1 = o1.getConstellationMap();
		ConstellationMap map2 = o2.getConstellationMap();
		
		// Sort by time
		List<Star> stars1 = timeOrder(map1);
		List<Star> stars2 = timeOrder(map2);
		
		if (stars1.size() > stars2.size()) {
			List<Star> o = stars1;
			ConstellationMapSignature om = o1;
			stars1 = stars2;
			stars2 = o;
			o1 = o2;
			o2 = om;
		}
		
		// Try various translations, pick the one that has the best match.
		int maxMatches    = 0;
		Spectrogram trans = o2.getSpectrogram();
		
		for (int x = 0; x < trans.getMaxTick(); x++) {
			double offset   = trans.tickToSeconds(x)-stars1.get(0).getTime();
			int matches     = getNumMatches(stars1, stars2, offset);
			
			if (x % 100 == 0)
			System.out.println(x + "(" + offset + ") " + "/" + trans.getMaxTick() + " = " + maxMatches/(double)stars1.size());
			
			if (matches > maxMatches) {
				maxMatches = matches;
			}
		}
		
		return maxMatches/(double)stars1.size();
	}
	
	protected int getNumMatches(List<Star> s1, List<Star> s2, double offset) {
		double[] minDistances = new double[s1.size()];
		Arrays.fill(minDistances, Double.POSITIVE_INFINITY);
		
		for (int i = 0; i < s1.size(); i++) {
			for (int j = 0; j < s2.size(); j++) {
				double dTime = ((s1.get(i).getTime() + offset) - s2.get(j).getTime());
				double dFreq = (s1.get(i).getFrequency() - s2.get(j).getFrequency());
				double distance = Math.sqrt(dTime*dTime + dFreq*dFreq);
				
				if (distance < minDistances[i]) {
					minDistances[i] = distance;
				}
			}
		}
		
		int matches = 0;
		for (double distance : minDistances) {
			if (distance < matchThreshold) {
				matches++;
			}
		}
		return matches;
	}
	
	protected List<Star> timeOrder(ConstellationMap map) {
		List<Star> list = new ArrayList<Star>(map.getStars());
		Collections.sort(list, starSorter);
		return list;
	}
}
