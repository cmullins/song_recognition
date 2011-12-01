package org.sidoh.peak_detection;

/**
 * This peak detector will declare a point a peak if the window to the left of it has
 * a mean delta value that is positive, and the mean delta value of the window to the
 * right of it is negative. This very roughly approximates the derivative being 0 at
 * a point.
 * 
 * @author chris
 */
public class StatefulMeanDeltaPeakDetector extends StatefulPeakDetector {
	
	public static class Builder extends StatefulPeakDetector.Builder {
		private final int windowWidth;
		private double lambda;

		public Builder(int windowWidth) {
			this.windowWidth = windowWidth;
			this.lambda = 1;
		}
		
		public Builder lambda(double lambda) {
			this.lambda = lambda;
			return this;
		}

		@Override
		public StatefulPeakDetector create(PeakListener peaks) {
			return new StatefulMeanDeltaPeakDetector(peaks, windowWidth, lambda);
		}
	}

	private final WindowWithCenter window;
	private final int k;
	private final double lambda;

	public StatefulMeanDeltaPeakDetector(PeakListener peaks, int windowWidth, double lambda) {
		super(peaks);
		this.lambda = lambda;
		k = windowWidth/2;
		this.window = new WindowWithCenter(windowWidth);
	}

	@Override
	protected void handleNewInput(int index, double value) {
		// Don't do anything if the window isn't full
		if (window.offer(value)) {
			// Check delta values
			if (window.left().meanDelta() > 0 && window.right().meanDelta() < 0
				&& window.x() > lambda*window.left().max() && window.x() > lambda*window.right().max()) {
				offerPeak((index - k), window.x());
			}
		}
	}

}
