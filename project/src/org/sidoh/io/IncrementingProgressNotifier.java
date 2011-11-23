package org.sidoh.io;

public class IncrementingProgressNotifier extends ProgressNotifier {

	public static class Builder extends ProgressNotifier.Builder {
		private final ProgressNotifier.Builder innerBuilder;

		public Builder(ProgressNotifier.Builder innerBuilder) {
			this.innerBuilder = innerBuilder;
		}
		
		@Override
		public IncrementingProgressNotifier create(String message, int maxValue) {
			return new IncrementingProgressNotifier(innerBuilder.create(message, maxValue));
		}
	}
	
	private final ProgressNotifier inner;
	private int counter;
	
	public IncrementingProgressNotifier(ProgressNotifier inner) {
		super(inner.message, inner.maxValue);
		this.inner = inner;
		this.counter = 0;
	}

	@Override
	public void update(int value) {
		counter = value;
		inner.update(value);
	}
	
	public void update() {
		inner.update(counter++);
	}

	@Override
	public void complete() {
		inner.complete();
	}

}
