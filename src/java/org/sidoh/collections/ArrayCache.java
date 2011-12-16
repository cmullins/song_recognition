package org.sidoh.collections;

import java.util.ArrayList;
import java.util.List;

public class ArrayCache<T> {
	private List<T> cache = new ArrayList<T>();
	
	public boolean contains(int i) {
		return cache.size() > i && cache.get(i) != null;
	}
	
	public void add(int ix, T value) {
		for (int i = cache.size(); i <= ix; i++) {
			cache.add(null);
		}
		cache.set(ix, value);
	}
	
	public T get(int ix) {
		return cache.get(ix);
	}
}
