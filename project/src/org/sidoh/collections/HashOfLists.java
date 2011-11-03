package org.sidoh.collections;

import java.util.LinkedList;
import java.util.List;

public class HashOfLists<K, V> extends DefaultingHashMap<K, List<V>> {
	private static final long serialVersionUID = 8941935772174902962L;

	@Override
	protected List<V> getDefaultValue() {
		return new LinkedList<V>();
	}
	
	public void addFor(K key, V value) {
		get(key).add(value);
	}

}
