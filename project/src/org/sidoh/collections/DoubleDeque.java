package org.sidoh.collections;

/**
 * This class is a functional subset of Deque<Double>. Using the 
 * alternatives when add/remove are being used incredibly frequently
 * causes a lot of boxing/unboxing of the Double wrapper objects. This
 * unnecessarily creates a lot of memory consumption.
 * 
 * This class uses an unchanging double[] to avoid this overhead. 
 * Behavior is undefined if it grows beyond its capacity.
 * 
 * @author chris
 */
public class DoubleDeque {
	private final int capacity;
	private final double[] values;
	
	private int start;
	private int end;
	private int size;

	public DoubleDeque(int capacity) {
		this.capacity = capacity;
		this.values = new double[capacity];
		this.start = 0;
		this.end = 0;
		this.size = 0;
	}
	
	public double removeFirst() {
		if (size == 0) {
			throw new IllegalStateException("Trying to pop off of an empty deque");
		}
		int newStart = ((start + 1) % capacity);
		double value = values[start];
		start = newStart;
		size--;
		
		return value;
	}
	
	public double peekFirst() {
		if (size == 0) {
			throw new IllegalStateException("Trying to peek on an empty deque");
		}
		return values[start];
	}
	
	public double peekLast() {
		if (size == 0) {
			throw new IllegalStateException("Trying to peek on an empty deque");
		}
		int ix = (end -1) < 0 ? values.length-1 :end-1;
		return values[ix];
	}

	public void addLast(double value) {
		if (size >= capacity) {
			throw new IllegalStateException("Trying to push onto a full deque");
		}
		int newEnd = ((end + 1) % capacity);
		values[end] = value;
		end = newEnd;
		size++;
	}
	
	public int size() {
		return size;
	}
	
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		
		for (int i = 0; i < size; i++) {
			b.append(values[(start + i) % capacity]);
			if ( i < (size-1) ) {
				b.append(", ");
			}
		}
		b.append("]");
		
		return b.toString();
	}
}
