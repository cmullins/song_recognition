package org.sidoh.collections;

public class DoubleAscendingMinimaWindow {
	
	private final IntDeque indexes;
	private final DoubleDeque values;
	private final int windowSize;
	private final boolean reverse;
	private int valuesSeen;
	
	private DoubleAscendingMinimaWindow(int windowSize, boolean reverse) {
		this.windowSize = windowSize;
		this.reverse = reverse;
		this.valuesSeen = 0;
		this.indexes = new IntDeque(windowSize);
		this.values = new DoubleDeque(windowSize);
	}
	
	public void offer(double value) {
		while (! values.isEmpty() && lessThan(values.peekLast(), value)) {
			values.removeLast();
			indexes.removeLast();
		}
		
		// Shift the old value out if needed
		if (!indexes.isEmpty() && (valuesSeen - windowSize) >= indexes.peekFirst()) {
			values.removeFirst();
			indexes.removeFirst();
		}
		
		values.addLast(value);
		indexes.addLast(valuesSeen);
		
		valuesSeen++;
	}
	
	public double getMinimum() {
		return values.peekFirst();
	}
	
	public boolean lessThan(double v1, double v2) {
		if (!reverse) {
			return v2 < v1;
		}
		else {
			return v1 < v2;
		}
	}
	
	public static DoubleAscendingMinimaWindow minimaTracker(int windowSize) {
		return new DoubleAscendingMinimaWindow(windowSize, false);
	}
	
	public static DoubleAscendingMinimaWindow maximaTracker(int windowSize) {
		return new DoubleAscendingMinimaWindow(windowSize, true);
	}
}
