package org.sidoh.peak_detection;

public class StatefulExponentialSmoothingFunction extends StatefulSmoothingFunction {
	
	public static class Builder extends StatefulSmoothingFunction.Builder {
		private final double smoothingFactor;

		public Builder(double smoothingFactor) {
			this.smoothingFactor = smoothingFactor;
		}

		@Override
		public StatefulSmoothingFunction create() {
			return new StatefulExponentialSmoothingFunction(smoothingFactor);
		}
		
		public double getSmoothingFactor() {
			return smoothingFactor;
		}
	}
	
	private final double smoothingFactor;
	private final double inverseSmoothing;
	
	private double prevRaw;
	private double prevSmoothed;
	
	private int i = 0;

	public StatefulExponentialSmoothingFunction(double smoothingFactor) {
		this.smoothingFactor = smoothingFactor;
		this.inverseSmoothing = (1 - smoothingFactor);
		this.prevRaw = Double.NaN;
		this.prevSmoothed = Double.NaN;
	}

	@Override
	public double smooth(double value) {
		if (Double.isNaN(prevRaw)) {
			prevRaw = value;
			prevSmoothed = value;
			
			return value;
		}
		else {
			double smoothed = (smoothingFactor*prevRaw + inverseSmoothing*prevSmoothed);
			
			prevRaw = value;
			prevSmoothed = smoothed;
			
			return smoothed;
		}
	}

}
