package org.sidoh.collections;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IndexingDictionary<T> implements Serializable {
	private static final long serialVersionUID = 8039884288155101853L;
	
	protected Map<Integer, T> index;
	protected Map<T, Integer> reverseIndex;
	
	protected int nextIndex;
	
	public IndexingDictionary() {
		index = new HashMap<Integer, T>();
		reverseIndex = new HashMap<T, Integer>();
		nextIndex = 0;
	}
	
	public int offer(T item) {
		Integer ix = reverseIndex.get(item);
		
		if (ix == null) {
			ix = forceAdd(item);
		}
		
		return ix;
	}
	
	public int forceAdd(T item) {
		index.put(nextIndex, item);
		reverseIndex.put(item, nextIndex);
		
		return nextIndex++;
	}
	
	public T get(int i) {
		return index.get(i);
	}
	
	public int indexOf(T value) {
		return reverseIndex.get(value);
	}
	
	public int size() {
		return index.size();
	}
	
	public Set<T> getAll(Iterable<Integer> ix) {
		Set<T> result = new HashSet<T>();
		
		for (Integer index : ix) {
			T val = get(index);
			
			if (val != null) {
				result.add(val);
			}
		}
		
		return result;
	}
}
