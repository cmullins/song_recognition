package org.sidoh.collections;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestDoubleAscendingMinimaWindow {

	@Test
	public void test() {
		double[] values  = {0.1,0.2,0.3,0.4,0.1,0.1,0.4,0.7,0.1,0.1,0.1,0.2,0.1};
		double[] answers = {0.1,0.2,0.3,0.4,0.4,0.4,0.4,0.7,0.7,0.7,0.7,0.2,0.2};
		
		DoubleAscendingMinimaWindow max = DoubleAscendingMinimaWindow.maximaTracker(4);
		for (int i = 0; i < values.length; i++) {
			max.offer(values[i]);
			assertEquals(max.getMinimum(), answers[i], 0.001);
		}
	}

}
