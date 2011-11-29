package org.sidoh.collections;

public class IntDeque {
	private final int capacity;
	private final int[] values;
	
	private int start;
	private int end;
	private int size;

	public IntDeque(int capacity) {
		this.capacity = capacity;
		this.values = new int[capacity];
		this.start = 0;
		this.end = 0;
		this.size = 0;
	}
	
	public int removeFirst() {
		if (size == 0) {
			throw new IllegalStateException("Trying to pop off of an empty deque");
		}
		int newStart = ((start + 1) % capacity);
		int value = values[start];
		start = newStart;
		size--;
		
		return value;
	}
	
	public int removeLast() {
		if (size == 0) {
			throw new IllegalStateException("Trying to pop off of an empty deque");
		}
		int newEnd = (end - 1) < 0 ? values.length-1 : end - 1;
		int value = values[newEnd];
		end = newEnd;
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

	public void addLast(int value) {
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
	
	public boolean isEmpty() {
		return size == 0;
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
