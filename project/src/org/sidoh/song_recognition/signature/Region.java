package org.sidoh.song_recognition.signature;

public abstract class Region {
	protected final int x;
	protected final int y;
	
	public static enum Response {
		BEFORE_REGION,
		AFTER_REGION,
		IN_REGION;
	}

	public static abstract class Builder {
		public abstract Region create(int x, int y);
	}
	
	protected Region(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public abstract Response isInRegion(int x, int y);

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public static Builder rectangularRegion(int offsetX, int offsetY, int h, int w) {
		return new RectangularRegion.RectangularRegionBuilder(offsetX, offsetY, h, w);
	}
}
