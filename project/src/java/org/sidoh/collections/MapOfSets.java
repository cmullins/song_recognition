package org.sidoh.collections;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MapOfSets<K, V> extends DefaultingMap<K, Set<V>> {

//	public static class Builder {
//		private SetBuilder<Object> setBuilder;
//		private final org.sidoh.collections.DefaultingMap.UnderlyingMapBuilder mapBuilder;
//
//		public Builder(UnderlyingMapBuilder mapBuilder) {
//			this.mapBuilder = mapBuilder;
//			this.setBuilder = new SetBuilder<Object>(HashSet.class);
//		}
//		
//		public Builder backedByHashSet() {
//			this.setBuilder = new SetBuilder<Object>(HashSet.class);
//			return this;
//		}
//		
//		public Builder backedByTreeSet() {
//			this.setBuilder = new SetBuilder<Object>(TreeMap.class);
//			return this;
//		}
//		
//		public MapOfSets<Object, Object> create() {
//			return new MapOfSets<Object, Object>(
//				(Map<Object, Set<Object>>)mapBuilder.create(),
//				setBuilder);
//		}
//	}
	
	protected static class SetBuilder<V> {
		private final Class<?> setType;

		public SetBuilder(Class<?> setType) {
			this.setType = setType;
		}
		
		@SuppressWarnings("unchecked")
		public Set<V> create() {
			try {
				return (Set<V>)setType.getConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private final SetBuilder<V> setBuilder;
	
	private MapOfSets(Map<K, Set<V>> underlyingMap, SetBuilder<V> setBuilder) {
		super(underlyingMap);
		this.setBuilder = setBuilder;
	}

	@Override
	protected Set<V> getDefaultValue() {
		return setBuilder.create();
	}

}
