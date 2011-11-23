package org.sidoh.peak_detection;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSlidingWindow {

	@Test
	public void test() {
		SlidingWindow w = new SlidingWindow(2);
		
		w.pushValue(0.1);
		
		System.out.println(w + ", delta = " + w.meanDelta());
		w.pushValue(0.2);
		
		System.out.println(w + ", delta = " + w.meanDelta());
		
		w.pushValue(0.3);
		
		System.out.println(w + ", delta = " + w.meanDelta());
		
		w.pushValue(0.1);
		
		System.out.println(w + ", delta = " + w.meanDelta());
		
		w.pushValue(0.01);
		
		System.out.println(w + ", delta = " + w.meanDelta());
		
		w.pushValue(0.01);
		
		System.out.println(w + ", delta = " + w.meanDelta());
		
		w.pushValue(0.01);
		
		System.out.println(w + ", delta = " + w.meanDelta());
	}

}
