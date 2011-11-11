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
	
	public HashOfSets<K, V> deepCopy() {
		HashOfSets<K, V> hs = new HashOfSets<K, V>();
		
		for (K key : keySet()) {
			hs.put(key, get(key));
		}
		
		return hs;
	}
}
