package org.sidoh.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.MinMaxPriorityQueue;
import com.google.common.collect.MinMaxPriorityQueue.Builder;

public class TreeMapOfPriorityQueues<K extends Comparable<K>, V> extends DefaultingTreeMap<K, Collection<V>> {
	private static final long serialVersionUID = -7530672922758356908L;
	
	private final Builder<V> builder;
	private final boolean sync;
	
	public TreeMapOfPriorityQueues(MinMaxPriorityQueue.Builder<V> builder) {
		this(builder, false);
	}
	
	public TreeMapOfPriorityQueues(MinMaxPriorityQueue.Builder<V> builder, boolean sync) {
		this.builder = builder;
		this.sync = sync;
	}

	@Override
	protected Collection<V> getDefaultValue() {
		Collection<V> val = builder.create();
		if (sync) {
			val = Collections.synchronizedCollection(val);
		}
		return val;
	}

	public void addFor(K key, V value) {
		get(key).add(value);
	}
	
	public Map<K, Collection<V>> synchronize() {
		return Collections.synchronizedMap(this);
	}
}
