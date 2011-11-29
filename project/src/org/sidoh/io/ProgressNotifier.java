package org.sidoh.io;

public abstract class ProgressNotifier {
	public abstract static class Builder {
		public ChanneledProgressNotifier.Builder channeled(String channel) {
			return new ChanneledProgressNotifier.Builder(this, channel);
		}
		
		public abstract ProgressNotifier create(String message, int maxValue);
	}
	
	protected final int maxValue;
	protected String message;
	protected int lastValue;
	
	public ProgressNotifier(String message, int maxValue) {
		this.message = message;
		this.maxValue = maxValue;
		this.lastValue = 0;
	}
	
	public void update(int value) {
		render(value);
	}
	
	public synchronized void update() {
		render(++lastValue);
	}
	
	protected abstract void render(int value);
	
	public abstract void complete();
	
	public static NullProgressNotifier.Builder nullNotifier() {
		return new NullProgressNotifier.NullProgressNotifierBuilder();
	}
	public static ConsoleProgressNotifier.Builder consoleNotifier(int maxUpdates) {
		return new ConsoleProgressNotifier.ConsoleProgressNotifierBuilder(maxUpdates);
	}
}
