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
	
	public void addStar(int time, double frequency, double intensity, int x, int y) {
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
		

		private final double time;
		private final double frequency;
		private final double intensity;
		private final int x;
		private final int y;
		
		public Star(final double time, final double frequency, final double intensity, final int x, final int y) {
			this.time = time;
			this.frequency = frequency;
			this.intensity = intensity;
			this.x = x;
			this.y = y;
		}

		public double getTime() {
			return time;
		}

		public double getFrequency() {
			return frequency;
		}

		public double getIntensity() {
			return intensity;
		}
		
		public int getTick() {
			return x;
		}
		
		public int getBin() {
			return y;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
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
			if (x != other.x)
				return false;
			if (y != other.y)
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
			if (o1 == null || o2 == null) {
				throw new IllegalArgumentException("Can't compare null - " + o1 + " // " + o2 + "!");
			}
			
			if (reverse) {
				return Double.compare(o2.getIntensity(), o1.getIntensity());
			}
			else {
				return Double.compare(o1.getIntensity(), o2.getIntensity());
			}
		}
		
	}
	
	public static class TimeStarComparator implements Comparator<Star> {
		@Override
		public int compare(Star o1, Star o2) {
			int t1 = o1.getTick();
			int t2 = o2.getTick();
			
			if (t1 == t2) {
				return 0;
			}
			else if (t1 < t2) {
				return -1;
			}
			else {
				return 1;
			}
		}
	}
}