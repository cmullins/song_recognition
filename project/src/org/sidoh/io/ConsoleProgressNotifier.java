package org.sidoh.io;

public class ConsoleProgressNotifier extends ProgressNotifier {

	protected static class ConsoleProgressNotifierBuilder extends ProgressNotifier.Builder {
		private final int maxUpdates;

		public ConsoleProgressNotifierBuilder(int maxUpdates) {
			this.maxUpdates = maxUpdates;
		}
		
		public ConsoleProgressNotifierBuilder() {
			this(100);
		}

		@Override
		public ProgressNotifier create(String message, int maxValue) {
			return new ConsoleProgressNotifier(maxValue, message, maxUpdates);
		}

	}
	
	private final int updateRate;
	private boolean done;
	private final int maxUpdates;

	public ConsoleProgressNotifier(int maxValue, String message, int maxUpdates) {
		super(message, maxValue);
		this.maxUpdates = maxUpdates;
		this.updateRate = (maxValue / maxUpdates);
		System.out.println(message);
		done = false;
	}

	@Override
	public void update(int value) {
		if (!done && ((value % updateRate) == 0 || value >= maxValue)) {
			printProgress(value);
		}
		
		if (value >= maxValue) {
			done = true;
		}
	}

	@Override
	public void complete() {
		System.out.println();
		done = true;
	}
	
	protected void printProgress(int value) {
		System.out.print("\r");
		System.out.print("Progress: [");
		
		value = Math.min(value, maxValue);
		
		double percentComplete = maxUpdates*(value / (double)maxValue);
		
		for (int i = 0; i < maxUpdates; i++) {
			if (i <= percentComplete) {
				System.out.print("=");
			}
			else if ((i - 1) <= percentComplete) {
				System.out.print(">");
			}
			else {
				System.out.print(" ");
			}
		}
		
		System.out.printf("] (%d / %d = %.2f%%)", value, maxValue, percentComplete/maxUpdates*100);
	}

}
