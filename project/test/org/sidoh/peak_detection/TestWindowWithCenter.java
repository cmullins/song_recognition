package org.sidoh.peak_detection;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestWindowWithCenter {

	@Test
	public void test() {
		double[] values = {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0,1.1,1.2,1.3,1.4,1.5};
		
		WindowWithCenter w = new WindowWithCenter(5);
		for (int i = 0; i < values.length; i++) {
			w.offer(values[i]);
			
			if (i >= 4) {
				assertEquals(values[i-4], w.left().min(), 0.01);
				assertEquals(values[i-3], w.left().max(), 0.01);
				assertEquals(values[i-2], w.x(), 0.01);
				assertEquals(values[i-1], w.right().min(), 0.01);
				assertEquals(values[i], w.right().max(), 0.01);
			}
		}
	}

}
