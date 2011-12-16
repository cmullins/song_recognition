package org.sidoh.collections;

import java.util.Comparator;

public class ReversingComparator<T extends Comparable<T>> implements Comparator<T> {

	@Override
	public int compare(T arg0, T arg1) {
		return arg1.compareTo(arg0);
	}
	
}
