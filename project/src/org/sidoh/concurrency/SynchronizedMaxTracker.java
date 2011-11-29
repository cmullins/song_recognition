package org.sidoh.concurrency;
import java.util.Comparator;

import org.sidoh.collections.NaturalComparator;


public class SynchronizedMaxTracker<K, V> {
	
	public static class Builder<K, V> {
		private final Comparator<K> comparator;

		public Builder(Comparator<K> comparator) {
			this.comparator = comparator;
		}
		
		public SynchronizedMaxTracker<K, V> create() {
			return new SynchronizedMaxTracker<K, V>(comparator);
		}
	}
	
	protected K maxKey;
	protected V maxValue;
	protected Comparator<K> comparator;
	
	protected SynchronizedMaxTracker(Comparator<K> comparator) {
		this.comparator = comparator;
	}
	
	public synchronized void offer(K key, V value) {
		if (maxKey == null || comparator.compare(maxKey, key) < 0) {
			maxKey = key;
			maxValue = value;
		}
	}
	
	public synchronized K maxKey() {
		return maxKey;
	}
	
	public synchronized V maxValue() {
		return maxValue;
	}
	
	public static <K, V> Builder<K, V> withComparator(Comparator<K> comparator) {
		return new Builder<K, V>(comparator);
	}
	
	public static <K extends Comparable<K>, V> Builder<K, V> defaultComparator() {
		return new Builder<K, V>(NaturalComparator.<K>instance());
	}
}
