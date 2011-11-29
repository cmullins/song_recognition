package org.sidoh.song_recognition.signature;

public abstract class Region {
	public static enum Response {
		/**
		 * Means that, moving in a forward direction, the coordinates could fall within
		 * this region and it should not be purged yet.
		 */
		BEFORE_REGION,
		
		/**
		 * Means that, moving in a forward direction, the provided coordinates will never
		 * fall within this region and can safely be purged.
		 */
		AFTER_REGION,
		
		/**
		 * Means that the provided coordinates are in the region.
		 */
		IN_REGION;
	}

	public static abstract class Builder {
		protected boolean reverse = false;
		
		/**
		 * Reverses the direction that the x-coordinate moves in. It is ascending by 
		 * default. Reversing means the region should expect that it is descending. This
		 * will change the {@link Response} provided by 
		 * {@link Region#isInRegion(int, int)} by inverting {@link Response#BEFORE_REGION}
		 * and {@link Response#AFTER_REGION}.
		 * 
		 * @return
		 */
		public Builder reverse() {
			reverse = true;
			return this;
		}
		
		public abstract Region create(int x, int y);
	}
	
	protected final int x;
	protected final int y;
	protected final boolean reverse;
	
	protected Region(int x, int y, boolean reverse) {
		this.x = x;
		this.y = y;
		this.reverse = reverse;
	}
	
	/**
	 * Calls {@link #abstractInRegion(int, int)} and performs reversing if enabled.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Response isInRegion(int x, int y) {
		Response innerResponse = abstractInRegion(x, y);
		
		// Invert if reverse is enabled.
		if (! reverse) {
			return innerResponse;
		}
		else if (innerResponse == Response.BEFORE_REGION) {
			return Response.AFTER_REGION;
		}
		else if (innerResponse == Response.AFTER_REGION) {
			return Response.BEFORE_REGION;
		}
		else {
			return Response.IN_REGION;
		}
	}
	
	/**
	 * Provides the appropriate response assuming that the x-coordinate is ascending (not descending).
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public abstract Response abstractInRegion(int x, int y);

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public static Builder rectangularRegion(int offsetX, int offsetY, int h, int w) {
		return new RectangularRegion.Builder(offsetX, offsetY, h, w);
	}
}
