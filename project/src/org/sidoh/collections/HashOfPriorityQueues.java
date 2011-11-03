package org.sidoh.collections;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.MinMaxPriorityQueue.Builder;

public class HashOfPriorityQueues<K, V> extends DefaultingHashMap<K, MinMaxPriorityQueue<V>> {
	private static final long serialVersionUID = -9081613625997115241L;
	
	private final Builder<V> pqBuilder;

	public HashOfPriorityQueues(MinMaxPriorityQueue.Builder<V> pqBuilder) {
		this.pqBuilder = pqBuilder;
	}

	@Override
	protected MinMaxPriorityQueue<V> getDefaultValue() {
		return pqBuilder.create();
	}

	public void addFor(K key, V value) {
		get(key).add(value);
	}
	
}
