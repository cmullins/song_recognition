package org.sidoh.collections;

import java.util.TreeMap;

public abstract class DefaultingTreeMap<K extends Comparable<K>, V> extends TreeMap<K, V> {
	private static final long serialVersionUID = -5042778605587016429L;

	/**
	 * Should return the default value one wants to use.
	 * 
	 * @return
	 */
	protected abstract V getDefaultValue();

	/**
	 * 
	 */
	@Override
	public V get(Object key) {
		V value = super.get(key);
		
		if (value == null) {
			value = getDefaultValue();
			super.put((K)key, value);
		}
		
		return value;
	}
	
	@Override
	public V remove(Object key) {
		V value = super.remove(key);
		
		return (value == null ? getDefaultValue() : value);
	}
}
