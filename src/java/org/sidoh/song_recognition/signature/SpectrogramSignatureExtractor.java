package org.sidoh.song_recognition.signature;

import org.sidoh.song_recognition.spectrogram.Spectrogram;

/**
 * Extracts a signature from a spectrogram.
 * 
 * @author chris
 *
 * @param <T>
 */
public interface SpectrogramSignatureExtractor<T extends Signature> extends SignatureExtractor<T> {
	/**
	 * 
	 * @param song
	 * @param spec
	 * @return
	 */
	public T extractSignature(Spectrogram spec);
}
