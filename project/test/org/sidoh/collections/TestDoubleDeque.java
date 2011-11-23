package org.sidoh.collections;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.Test;

public class TestDoubleDeque {

	@Test
	public void testSimple() {
		DoubleDeque dd = new DoubleDeque(100);
		Deque<Double> jd = new LinkedList<Double>();
		
		for (int i = 0; i < 100; i++) {
			dd.addLast(i);
			jd.addLast(Double.valueOf(i));
		}
		
		while (jd.size() > 0) {
			double v1 = dd.removeFirst();
			double v2 = jd.removeFirst();
			
			assert(v1 == v2);
		}
	}
	
	@Test
	public void testComplex() {
		DoubleDeque dd = new DoubleDeque(100);
		Deque<Double> jd = new LinkedList<Double>();
		
		for (int i = 0; i < 200; i++) {
			if (i >= 100) {
				double v1 = dd.removeFirst();
				double v2 = jd.removeFirst();
				
				assert(v1 == v2);
			}
			
			dd.addLast(i);
			jd.addLast(Double.valueOf(i));
		}
		
		while (jd.size() > 0) {
			double v1 = dd.removeFirst();
			double v2 = jd.removeFirst();
			
			assert(v1 == v2);
		}
	}

	@Test
	public void testPeek() {
		DoubleDeque dd = new DoubleDeque(100);
		
		for (int i = 0; i < 100; i++) {
			dd.addLast(i);
		}
		
		assert(dd.peekFirst() == 0d);
		assert(dd.peekLast() == 99d);
	}
}
