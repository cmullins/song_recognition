package org.sidoh.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public abstract class DefaultingMap<K, V> implements Map<K, V> {
	
//	public static class Builder<K, V> {
//		private final UnderlyingMapBuilder mapBuilder;
//
//		private Builder(Class<?> mapType) {
//			this.mapBuilder = new UnderlyingMapBuilder(mapType);
//		}
//		
//		public MapOfSets.Builder<K, Set<V>> mapOfSets() {
//			return new MapOfSets.Builder<K, Set<V>>(mapBuilder);
//		}
//	}
	
	protected static class UnderlyingMapBuilder {
		private final Class<?> mapType;
		
		public UnderlyingMapBuilder(Class<?> mapType) {
			this.mapType = mapType;
		}
		
		@SuppressWarnings("unchecked")
		public Map<Object, Object> create() {
			try {
				return (Map<Object, Object>)mapType.getConstructor().newInstance();
			}
			catch (Exception e) { 
				throw new RuntimeException(e);
			}
		}
	}
	
	protected Map<K, V> underlyingMap;
	
	protected DefaultingMap(Map<K, V> underlyingMap) {
		this.underlyingMap = underlyingMap;
	}
	
	protected abstract V getDefaultValue();

	@Override
	public void clear() {
		underlyingMap.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return underlyingMap.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return underlyingMap.containsKey(arg0);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return underlyingMap.entrySet();
	}

	@Override
	public V get(Object arg0) {
		V value = underlyingMap.get(arg0);
		
		if (value == null) {
			value = getDefaultValue();
			put((K)arg0, value);
		}
		
		return value;
	}

	@Override
	public boolean isEmpty() {
		return underlyingMap.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return underlyingMap.keySet();
	}

	@Override
	public V put(K arg0, V arg1) {
		return underlyingMap.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		underlyingMap.putAll(arg0);
	}

	@Override
	public V remove(Object arg0) {
		return underlyingMap.remove(arg0);
	}

	@Override
	public int size() {
		return underlyingMap.size();
	}

	@Override
	public Collection<V> values() {
		return underlyingMap.values();
	}

//	public static Builder backedByHash() {
//		return new Builder(HashMap.class);
//	}
//	
//	public static Builder backedByTree() {
//		return new Builder(TreeMap.class);
//	}
}
