package org.sidoh.io;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
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
		
		/**
		 * Makes {@link ChanneledProgressNotifier} instances created by this builder
		 * controllable by stdin. Pressing any key will toggle the channel on or off.
		 * 
		 * @return
		 */
		public Builder controlledByStdin() {
			StdinChannelToggle.launch(channel);
			return this;
		}

		@Override
		public ChanneledProgressNotifier create(String message, int maxValue) {
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
	protected void render(int value) {
		if (updatesEnabled(channel)) {
			getInner().render(value);
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
	
	/**
	 * This class runs in a thread that toggles a channel as keys are pressed.
	 * 
	 */
	protected static class StdinChannelToggle implements Runnable {
		private static StdinChannelToggle instance = null;
		
		private final String channel;
		private final Reader in;

		public StdinChannelToggle(String channel, Reader in) {
			this.channel = channel;
			this.in = in;
		}
		
		@Override
		public void run() {
			while (true) {
				if (! Thread.interrupted()) {
					try {
						if (System.in.available() > 0) {
							System.in.read();
							toggleChannel(channel);
						}
						else {
							Thread.sleep(100);
						}
					}
					catch (Exception e) { 
						break;
					}
				}
			}
		}
//		public void run() {
//			for (;;) {
//				if (! Thread.interrupted()) {
//					try {
//						System.out.println(in.getClass());
//						in.read();
//						toggleChannel(channel);
//					}
//					catch (Exception e) { 
//						System.out.println(e);
//					}
//				}
//			}
//		}
		
		/**
		 * 
		 * @param channel
		 */
		public static void launch(String channel) {
			if (instance != null) {
				throw new IllegalStateException("Cannot have more than one stdin channel toggle!");
			}
			// This creates an stdin InputStream that can be interrupted.
			final InputStreamReader reader
				= new InputStreamReader(Channels.newInputStream(new FileInputStream(FileDescriptor.in).getChannel()));
			
			instance = new StdinChannelToggle(channel, reader);
			
			// Start it running in a new thread
			final Thread toggleThread = new Thread(instance);
			
			toggleThread.setDaemon(true);
			toggleThread.start();
			
			final Runnable killTask = new Runnable() {
				@Override
				public void run() {
					toggleThread.interrupt();
				}
			};
			
			// Add a shutdown hook that will stop the toggleThread if the program stops
			// unexpectedly.
			Runtime.getRuntime().addShutdownHook(new Thread(killTask));
		}
	}
}
