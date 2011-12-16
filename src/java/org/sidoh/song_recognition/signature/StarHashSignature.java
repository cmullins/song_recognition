package org.sidoh.song_recognition.signature;

import java.util.Map;
import java.util.Set;

import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.spectrogram.Spectrogram;


public class StarHashSignature implements Signature {
	private static final long serialVersionUID = 1L;
	private final ConstellationMap stars;
	protected final Map<Integer, Set<Integer>> starHashes;
	
	public StarHashSignature(ConstellationMap stars, Map<Integer, Set<Integer>> hashes) {
		this.stars = stars;
		this.starHashes = hashes;
	}

	public ConstellationMap getStars() {
		return stars;
	}

	public Map<Integer, Set<Integer>> getStarHashes() {
		return starHashes;
	}
	
	public static Integer computeHash(int time1, int time2, int freqBin1, int freqBin2, Spectrogram spec) {
		return computeHash(Math.abs(time1-time2), freqBin1, freqBin2);
		//return computeHash(spec.tickToSeconds(time1), spec.tickToSeconds(time2), freqBin1, freqBin2);
	}
	
	public static Integer computeHash(double time1, double time2, int freqBin1, int freqBin2) {
		int timeDelta = (int)Math.abs(Math.floor(time1*1e6) - Math.floor(time2*1e6));

		return computeHash(timeDelta, freqBin1, freqBin2);
	}
	
	public static Integer computeHash(int timeDelta, int freqBin1, int freqBin2) {
		return (((timeDelta & 0x3FFFF) << 20) | ((freqBin1 & 0x3FF) << 10) | (freqBin2 & 0x3FF));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stars == null) ? 0 : stars.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StarHashSignature other = (StarHashSignature) obj;
		if (stars == null) {
			if (other.stars != null)
				return false;
		} else if (!stars.equals(other.stars))
			return false;
		return true;
	}
}
