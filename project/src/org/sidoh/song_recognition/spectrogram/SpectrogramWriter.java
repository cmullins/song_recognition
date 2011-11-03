package org.sidoh.song_recognition.spectrogram;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A spectrogram writer puts an entire spectrogram somewhere. This differs from
 * {@link SpectrogramStorage} in that it's not meant to be transient.
 * 
 * @author chris
 *
 */
public interface SpectrogramWriter {
	public void write(OutputStream out, Spectrogram spec) throws IOException;
}
