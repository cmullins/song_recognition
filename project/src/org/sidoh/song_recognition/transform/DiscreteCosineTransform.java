package org.sidoh.song_recognition.transform;

import edu.emory.mathcs.jtransforms.dct.DoubleDCT_1D;

public class DiscreteCosineTransform implements Transform {
	
	private final DoubleDCT_1D dct;
	private final double sampleRate;
	private final int size;

	public DiscreteCosineTransform(int size, double sampleRate) {
		this.size = size;
		this.sampleRate = sampleRate;
		dct = new DoubleDCT_1D(size);
	}

	@Override
	public double[] transform(double[] values) {
		dct.forward(values, true);
		return values;
	}

	@Override
	public double[] getBinFloors() {
		double[] floors = new double[size];
		
		for (int i = 0; i < size; i++) {
			floors[i] = ((i * sampleRate) / size);
		}
		
		return floors;
	}
	

}
