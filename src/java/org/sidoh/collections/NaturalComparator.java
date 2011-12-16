package org.sidoh.collections;

import java.util.Comparator;

public class NaturalComparator<T extends Comparable<T>> implements Comparator<T> {

	@Override
	public int compare(T o1, T o2) {
		if (o1 == null && o2 == null) {
			return 0;
		}
		else if (o1 == null) {
			return -1;
		}
		else if (o2 == null) {
			return 1;
		}
		else {
			return o1.compareTo(o2);
		}
	}
	
	public static <T extends Comparable<T>> NaturalComparator<T> instance() {
		return new NaturalComparator<T>();
	}

}
