package org.sidoh.peak_detection;

public class StatefulSdsFromMeanPeakDetector extends StatefulPeakDetector {
	
	public static class Builder extends StatefulPeakDetector.Builder {
		
		private final int windowWidth;
		private final double numSds;

		public Builder(int windowWidth, double numSds) {
			this.windowWidth = windowWidth;
			this.numSds = numSds;
		}

		@Override
		public StatefulSdsFromMeanPeakDetector create(PeakListener peaks) {
			return new StatefulSdsFromMeanPeakDetector(peaks, windowWidth, numSds);
		}
		
	}

	private final int windowWidth;
	private final double numSds;
	private final int k;
	private SlidingWindow leftValues;
	private SlidingWindow rightValues;
	private double xi;
	private int i;

	public StatefulSdsFromMeanPeakDetector(PeakListener peaks, int windowWidth, double numSds) {
		super(peaks);
		this.windowWidth = windowWidth;
		this.numSds = numSds;
		this.k = windowWidth/2;
		
		leftValues = new SlidingWindow(k);
		rightValues = new SlidingWindow(k);
		xi = Double.NaN;
		i = k;
	}

	@Override
	protected void handleNewInput(int index, double value) {
		if (index < k) {
			leftValues.pushValue(value);
			return;
		}
		else if (index > k && index <= (i+k)) {
			rightValues.pushValue(value);
			return;
		}
		else if (index == k) {
			xi = value;
			return;
		}
		else {
			double mean = SlidingWindow.mean(leftValues, rightValues);
			double sd   = SlidingWindow.sd(leftValues, rightValues);
			
			if (Math.abs(xi - mean) > (numSds*sd)) {
				super.offerPeak(i, xi);
				System.out.println(String.format("%d %d %f 1", count, index, value));
			}
			else {

				System.out.println(String.format("%d %d %f 0", count, index, value));
			}
			
			leftValues.pushValue(xi);
			xi = rightValues.pushValue(value);
			i++;
		}
	}

}
