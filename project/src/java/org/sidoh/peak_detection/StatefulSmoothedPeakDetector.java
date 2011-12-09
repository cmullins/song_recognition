package org.sidoh.peak_detection;

public class StatefulSmoothedPeakDetector extends StatefulPeakDetector {
	
	public static class Builder extends StatefulPeakDetector.Builder {
		
		private final StatefulSmoothingFunction.Builder smoothingFnBuilder;
		private final StatefulPeakDetector.Builder innerBuilder;

		public Builder(StatefulSmoothingFunction.Builder smoothingFnBuilder, 
				StatefulPeakDetector.Builder innerBuilder) {
			
			this.smoothingFnBuilder = smoothingFnBuilder;
			this.innerBuilder = innerBuilder;
		}

		@Override
		public StatefulPeakDetector create(PeakListener peaks) {
			return new StatefulSmoothedPeakDetector(
					smoothingFnBuilder.create(),
					innerBuilder.create(peaks));
		}
		
		@Override
		public StatefulPeakDetector.Builder withSmoothingFunction(StatefulSmoothingFunction.Builder builder) {
			return innerBuilder.withSmoothingFunction(builder);
		}
		
		public StatefulPeakDetector.Builder innerBuilder() {
			return innerBuilder;
		}
		
		public StatefulSmoothingFunction.Builder smoothingFunction() {
			return smoothingFnBuilder;
		}
	}
	
	private final StatefulPeakDetector inner;
	private final StatefulSmoothingFunction smoothingFn;

	public StatefulSmoothedPeakDetector(StatefulSmoothingFunction smoothingFn,
			StatefulPeakDetector inner) {
		// Allow the inner peak detector to notify the PeakListener or new peaks rather than
		// receiving suggestions and forwarding them ourselves. That sounds like no fun.
		super(null);
		
		this.smoothingFn = smoothingFn;
		this.inner = inner;
	}

	@Override
	protected void handleNewInput(int index, double value) {
		inner.handleNewInput(index, smoothingFn.smooth(value));
	}
}
