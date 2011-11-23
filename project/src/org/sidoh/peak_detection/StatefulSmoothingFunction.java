package org.sidoh.peak_detection;

public abstract class StatefulSmoothingFunction {
	
	public static abstract class Builder {
		public abstract StatefulSmoothingFunction create();
	}
	
	public abstract double smooth(double value);
	
	public static StatefulExponentialSmoothingFunction.Builder exponentialSmoother(double smoothingFactor) {
		return new StatefulExponentialSmoothingFunction.Builder(smoothingFactor);
	}
	
}
