package org.sidoh.collections;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

public class CountingMap<T> {
	protected SortedMap<T, Integer> counter;
	
	public CountingMap() {
		counter = new TreeMap<T, Integer>();
	}
	
	public CountingMap(Comparator<T> comparator) {
		counter = new TreeMap<T, Integer>(comparator);
	}
	
	public void add(T value) {
		if (! counter.containsKey(value)) {
			counter.put(value, 1);
		}
		else {
			counter.put(value, counter.get(value)+1);
		}
	}
	
	public void remove(T value) {
		int newValue = counter.get(value)-1;
		
		if (newValue == 0) {
			counter.remove(value);
		}
		else {
			counter.put(value, newValue);
		}
	}
	
	public T min() {
		return counter.firstKey();
	}
	
	public T max() {
		return counter.lastKey();
	}
}
