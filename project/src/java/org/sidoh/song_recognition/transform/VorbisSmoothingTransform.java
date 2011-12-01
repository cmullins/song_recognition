package org.sidoh.song_recognition.transform;

/**
 * Applies the Vorbis Window function to smooth the data across samples
 * 
 * @author chris
 *
 */
public class VorbisSmoothingTransform implements Transform {
	
	private final Transform inner;
	private final double[] smoothingValues;

	public VorbisSmoothingTransform(Transform inner) {
		int size = inner.getBinFloors().length;
		
		this.inner = inner;
		smoothingValues = new double[size];
		for (int i = 0; i < size; i++) {
            double xx = Math.sin( (Math.PI/(2.0*size)) * (2.0 * i) );
            smoothingValues[i] = Math.sin( (Math.PI/2.0) * (xx * xx) );
		}
	}

	@Override
	public double[] transform(double[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] *= smoothingValues[i];
		}
		inner.transform(values);
		return values;
	}

	@Override
	public double[] getBinFloors() {
		return inner.getBinFloors();
	}

}
