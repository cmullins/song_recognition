package org.sidoh.peak_detection;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSlidingWindow {

	@Test
	public void test() {
		double[] values = {0.1,0.2,0.1,0.7,0.8,0.9,0.1,0.2,0.3};
		double[] max    = {0.1,0.2,0.2,0.7,0.8,0.9,0.9,0.9,0.9};
		double[] min    = {0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1};
		
		SlidingWindow w = new SlidingWindow(4);
		
		for (int i = 0; i < values.length; i++) {
			w.pushValue(values[i]);
			
			assertEquals(max[i], w.max(), 0.01);
			assertEquals(min[i], w.min(), 0.01);
		}
	}

}
