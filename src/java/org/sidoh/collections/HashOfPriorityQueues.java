package org.sidoh.collections;

import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.MinMaxPriorityQueue.Builder;

public class HashOfPriorityQueues<K, V> extends DefaultingHashMap<K, Collection<V>> {
	private static final long serialVersionUID = -9081613625997115241L;
	
	private final Builder<V> pqBuilder;
	private final boolean sync;
	
	public HashOfPriorityQueues(MinMaxPriorityQueue.Builder<V> builder) {
		this(builder, false);
	}

	public HashOfPriorityQueues(MinMaxPriorityQueue.Builder<V> pqBuilder, boolean sync) {
		this.pqBuilder = pqBuilder;
		this.sync = sync;
	}

	@Override
	protected Collection<V> getDefaultValue() {
		Collection<V> val = pqBuilder.create();
		if (sync) {
			val = Collections.synchronizedCollection(val);
		}
		return val;
	}

	public void addFor(K key, V value) {
		get(key).add(value);
	}
	
}
