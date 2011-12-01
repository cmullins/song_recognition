package org.sidoh.song_recognition.signature;

import org.sidoh.song_recognition.spectrogram.Spectrogram;

/**
 * Implements the Constellation Map fingerprinting discribed by Wang in:
 * http://www.ee.columbia.edu/~dpwe/papers/Wang03-shazam.pdf
 * 
 * @author chris
 *
 */
public class ConstellationMapSignature implements Signature {
	private static final long serialVersionUID = 5532263606892179599L;
	private final ConstellationMap map;
	private final transient Spectrogram spec;

	protected ConstellationMapSignature(ConstellationMap map, Spectrogram spec) {
		this.map = map;
		this.spec = spec;
	}
	
	public ConstellationMap getConstellationMap() {
		return map;
	}
	
	public Spectrogram getSpectrogram() {
		return spec;
	}
}
