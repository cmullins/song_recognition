package org.sidoh.song_recognition.signature;

public class RectangularRegion extends Region {
	
	private final int offsetX;
	private final int offsetY;
	private final int height;
	private final int width;

	protected static class Builder extends Region.Builder {
		
		private final int offsetX;
		private final int offsetY;
		private final int height;
		private final int width;

		public Builder(int offsetX, int offsetY, int height, int width) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.height = height;
			this.width = width;
		}

		@Override
		public RectangularRegion create(int x, int y) {
			return new RectangularRegion(x,y,offsetX,offsetY,height,width,reverse);
		}
		
	}
	
	private RectangularRegion(int x, int y, int offsetX, int offsetY, int height, int width, boolean reverse) {
		super(x,y,reverse);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.height = height;
		this.width = width;
	}

	@Override
	public Response abstractInRegion(int x, int y) {
		boolean withinLeft = (x >= (this.x + offsetX));
		boolean withinRight = (x <= (this.x + offsetX + width));
		boolean withinHeight = (y >= (this.y + offsetY) && y <= (this.y + offsetY + height));
		
		if (withinLeft && withinRight && withinHeight) {
			return Response.IN_REGION;
		}
		else if (!withinRight) {
			return Response.AFTER_REGION;
		}
		else {
			return Response.BEFORE_REGION;
		}
	}
}
