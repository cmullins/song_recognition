package org.sidoh.collections;

import java.util.HashMap;

public abstract class DefaultingHashMap<K,V> extends HashMap<K,V> {
	private static final long serialVersionUID = 8428907711974132469L;

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
