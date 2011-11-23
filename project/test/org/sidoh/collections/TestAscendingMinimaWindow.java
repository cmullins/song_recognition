package org.sidoh.collections;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class TestAscendingMinimaWindow {
	@Test
	public void test() {
		int[] testA   = { 2, 4, 5, 9, 2, 1, 3, 5, 7, 8 };
		int k         = 3;
		int answers[] = { 2, 4, 2, 1, 1, 1, 3, 5 };
		
		AscendingMinimaWindow<Integer> am = AscendingMinimaWindow.<Integer>withNaturalOrdering(k).create();
		
		for (int i = 0; i < k; i++) {
			am.offer(testA[i]);
		}
		
		for (int i = k; i <= (testA.length - k); i++) {
			assertEquals((int)am.getMinimum(), answers[i-k]);
			am.offer(testA[i]);
		}
	}
}
