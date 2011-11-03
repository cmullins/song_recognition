package org.sidoh.song_recognition.signature;

import org.sidoh.song_recognition.audio_io.FrameBuffer;

/**
 * A signature extractor extracts features on a frame buffer and returns a
 * {@link Signature}.
 * 
 * @author chris
 *
 */
public interface SignatureExtractor<T extends Signature> {
	/**
	 * Extract a signature from the provided FrameBuffer.
	 * 
	 * @param frames
	 * @return
	 */
	public T extractSignature(FrameBuffer frames);
}
