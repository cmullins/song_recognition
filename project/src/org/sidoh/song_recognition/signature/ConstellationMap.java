package org.sidoh.song_recognition.signature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;


/**
 * A constellation map is essentially a list of spectrogram peaks. This
 * boils down to a collection of time, frequency pairs.
 * 
 * @author chris
 *
 */
public class ConstellationMap implements Serializable {
	private static final long serialVersionUID = 8977250784498673895L;
	private Collection<Star> stars;
	private double maxTime;
	
	public ConstellationMap() {
		stars = new ArrayList<Star>();
		maxTime = 0;
	}
	
	public Collection<Star> getStars() {
		return new ArrayList<Star>(stars);
	}
	
	public void addStar(Double time, Double frequency, Double intensity, Integer x, Integer y) {
		addStar(new Star(time, frequency, intensity, x, y));
	}
	
	public void addStar(Star star) {
		stars.add(star);
		if (star.getTime() > maxTime) {
			maxTime = star.getTime();
		}
	}
	
	public void addAll(Iterable<Star> stars) {
		for (Star s : stars) {
			addStar(s);
		}
	}
	
	public double maxTime() {
		return maxTime;
	}

	public static class Star implements Serializable {
		private static final long serialVersionUID = 1851275921474044916L;
		

		private final Double time;
		private final Double frequency;
		private final Double intensity;
		private final Integer x;
		private final Integer y;
		
		public Star(final Double time, final Double frequency, final Double intensity, final Integer x, final Integer y) {
			this.time = time;
			this.frequency = frequency;
			this.intensity = intensity;
			this.x = x;
			this.y = y;
		}

		public Double getTime() {
			return time;
		}

		public Double getFrequency() {
			return frequency;
		}

		public Double getIntensity() {
			return intensity;
		}
		
		public Integer getTick() {
			return x;
		}
		
		public Integer getBin() {
			return y;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((x == null) ? 0 : x.hashCode());
			result = prime * result + ((y == null) ? 0 : y.hashCode());
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
			Star other = (Star) obj;
			if (x == null) {
				if (other.x != null)
					return false;
			} else if (!x.equals(other.x))
				return false;
			if (y == null) {
				if (other.y != null)
					return false;
			} else if (!y.equals(other.y))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Star [x=" + x + ", y=" + y + ", intensity=" + intensity + "]";
		}
	}
	
	public static class StarEnergyComparator implements Comparator<Star> {
		
		private final boolean reverse;

		public StarEnergyComparator(boolean reverse) {
			this.reverse = reverse;
		}

		@Override
		public int compare(Star o1, Star o2) {
			if (reverse) {
				return o2.getIntensity().compareTo(o1.getIntensity());
			}
			else {
				return o1.getIntensity().compareTo(o2.getIntensity());
			}
		}
		
	}
	
	public static class TimeStarComparator implements Comparator<Star> {

		@Override
		public int compare(Star o1, Star o2) {
			return o1.getTime().compareTo(o2.getTime());
		}
		
	}
}