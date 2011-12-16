package org.sidoh.collections;

import java.util.HashSet;
import java.util.Set;

public class TreeMapOfSets<K extends Comparable<K>, V> extends DefaultingTreeMap<K, Set<V>> {
	@Override
	protected Set<V> getDefaultValue() {
		return new HashSet<V>();
	}

	public void addFor(K key, V value) {
		get(key).add(value);
	}
}
