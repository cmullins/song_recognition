package org.sidoh.song_recognition.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sidoh.peak_detection.Histogram;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;

public class StarHashComparator implements LikenessComparator<StarHashSignature> {
	private static final long serialVersionUID = 7842677071829673521L;

	@Override
	public double similarity(StarHashSignature o1, StarHashSignature o2) {
		Map<Integer, Set<Star>> hashes1 = o1.getStarHashes();
		Map<Integer, Set<Star>> hashes2 = o2.getStarHashes();
		
		if (hashes1.size() < hashes2.size()) {
			return similarity(o2, o1);
		}
		
		Histogram offsets = new Histogram();
		for (Integer hash : hashes1.keySet()) {
			if (hashes2.containsKey(hash)) {
				Set<Star> stars1 = hashes1.get(hash);
				Set<Star> stars2 = hashes2.get(hash);
			
				for (Star star2 : stars2) {
					for (Star star1 : stars1) {
						offsets.addValue((star1.getTime() - star2.getTime()));
					}
				}
			}
		}
		
		try {
			PrintStream out = new PrintStream(new FileOutputStream(new File("/tmp/histograms/" + offsets.maxSignificance())));
			for (Entry<Integer, List<Double>> entry : offsets.getValues().entrySet()) {
				for (Double value : entry.getValue()) {
					out.println(value);
				}
			}
			out.close();
		}
		catch (Exception e) { }
		
		return offsets.maxSignificance();
	}

}
