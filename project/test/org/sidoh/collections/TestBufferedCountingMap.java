package org.sidoh.collections;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestBufferedCountingMap {
	@Test
	public void test() {
		int[] testData = {1,2,3,3,5,6,7,8,9,0,1,2,3,4,5,-100,6,7,7,3,2};
		Map<Integer, Integer> answers = new HashMap<Integer, Integer>();
		BufferedCountingMap bcm = new BufferedCountingMap(3);
		
		for (int i : testData) {
			if (! answers.containsKey(i)) {
				answers.put(i, 1);
			}
			else {
				answers.put(i, answers.get(i)+1);
			}
			bcm.increment(i);
		}
		
		for (int i : testData) {
			assertEquals((int)answers.get(i), bcm.count(i));
		}
	}
}
