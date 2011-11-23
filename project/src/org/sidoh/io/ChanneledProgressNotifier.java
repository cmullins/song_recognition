package org.sidoh.io;

import java.util.HashMap;
import java.util.Map;

public class ChanneledProgressNotifier extends ProgressNotifier {

	public static class Builder extends ProgressNotifier.Builder {
		private final ProgressNotifier.Builder inner;
		private final String channel;

		public Builder(ProgressNotifier.Builder inner, String channel) {
			this.inner = inner;
			this.channel = channel;
		}

		@Override
		public ProgressNotifier create(String message, int maxValue) {
			return new ChanneledProgressNotifier(channel, inner, message, maxValue);
		}
	}
	
	/**
	 * This will keep track of whether or not channels are enabled.
	 * 
	 */
	private static final Map<String, Boolean> channels = new HashMap<String, Boolean>();
	
	private final ProgressNotifier.Builder innerBuilder;
	private ProgressNotifier inner;
	private final String channel;
	
	public ChanneledProgressNotifier(String channel, ProgressNotifier.Builder innerBuilder, String message, int maxValue) {
		super(message, maxValue);
		this.channel = channel;
		this.innerBuilder = innerBuilder;
		this.inner = null;
	}

	@Override
	public void update(int value) {
		if (updatesEnabled(channel)) {
			getInner().update(value);
		}
	}

	@Override
	public void complete() {
		if (updatesEnabled(channel)) {
			getInner().complete();
		}
	}
	
	/**
	 * If inner hasn't been set yet, create it. If it has, return it.
	 * 
	 * @return
	 */
	protected ProgressNotifier getInner() {
		if (inner == null) {
			inner = innerBuilder.create(message, maxValue);
		}
		return inner;
	}
	
	/**
	 * Returns true if updates are enabled on the provided channel.
	 * 
	 * @param channel
	 * @return
	 */
	public static boolean updatesEnabled(String channel) {
		return channels.containsKey(channel) && channels.get(channel);
	}

	/**
	 * Enable progress notifications on this channel 
	 * 
	 * @param channel
	 */
	public static void enableChannel(String channel) {
		channels.put(channel, true);
	}
	
	/**
	 * Disable progress notifications on this channel
	 * 
	 * @param channel
	 */
	public static void disableChannel(String channel) {
		channels.put(channel, false);
	}
	
	/**
	 * Toggles channel on or off.
	 * 
	 * @param channel
	 */
	public static void toggleChannel(String channel) {
		channels.put(channel, !updatesEnabled(channel));
	}
}
