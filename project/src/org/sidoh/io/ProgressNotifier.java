package org.sidoh.io;

public abstract class ProgressNotifier {
	public abstract static class Builder {
		public abstract ProgressNotifier create(String message, int maxValue);
	}
	
	protected final int maxValue;
	protected String message;
	
	public ProgressNotifier(String message, int maxValue) {
		this.message = message;
		this.maxValue = maxValue;
	}
	
	public abstract void update(int value);
	
	public abstract void complete();
	
	public static NullProgressNotifier.Builder nullNotifier() {
		return new NullProgressNotifier.NullProgressNotifierBuilder();
	}
	public static ConsoleProgressNotifier.Builder consoleNotifier(int maxUpdates) {
		return new ConsoleProgressNotifier.ConsoleProgressNotifierBuilder(maxUpdates);
	}
}
