package org.sidoh.collections;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;

/**
 * Implements the ascending minima datastructure, which allows one to find the
 * minimum value in a sliding window in O(n) time and O(k) memory, where n is 
 * the size of the large vector, and k is the size of the window.
 * 
 * @author chris
 *
 */
public class AscendingMinimaWindow<T> {
	
	public static class Builder<T> {
		private Comparator<T> comparator;
		private int windowSize;

		public Builder(Comparator<T> comparator, int windowSize) {
			this.comparator = comparator;
			this.windowSize = windowSize;
		}
		
		public Builder<T> windowSize(int k) {
			this.windowSize = k;
			return this;
		}
		
		public Builder<T> reverseOrder() {
			this.comparator = Collections.reverseOrder(comparator);
			return this;
		}
		
		public AscendingMinimaWindow<T> create() {
			return new AscendingMinimaWindow<T>(windowSize, comparator);
		}
	}
	
	private final Comparator<T> comparator;
	private final Deque<Pair<Integer, T>> values;
	private final int windowSize;
	private int valuesSeen;
	
	private AscendingMinimaWindow(int windowSize, Comparator<T> comparator) {
		this.windowSize = windowSize;
		this.comparator = comparator;
		this.values = new ArrayDeque<Pair<Integer, T>>(windowSize);
		this.valuesSeen = 0;
	}
	
	public void offer(T value) {
		while (! values.isEmpty() && comparator.compare(values.peekLast().getV2(), value) >= 0) {
			values.removeLast();
		}
		values.add(Pair.create(valuesSeen, value));
		
		// Shift the old value out if needed
		if ((valuesSeen - windowSize) >= values.peekFirst().getV1()) {
			values.removeFirst();
		}
		
		valuesSeen++;
	}
	
	public T getMinimum() {
		return values.peekFirst().getV2();
	}
	
	public static <T extends Comparable<T>> Builder<T> withNaturalOrdering(int windowSize) {
		return new Builder<T>(NaturalComparator.<T>instance(), windowSize);
	}
	
	public static <T> Builder<T> withComparator(Comparator<T> comparator, int windowSize) {
		return new Builder<T>(comparator, windowSize);
	}
}
