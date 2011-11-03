package org.sidoh.collections;

import java.util.HashSet;
import java.util.Set;

public class HashOfSets<K, V> extends DefaultingHashMap<K, Set<V>> {
	private static final long serialVersionUID = -1286617433032205415L;

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
