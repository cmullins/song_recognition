package org.sidoh.song_recognition.spectrogram;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.OverlappingFrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.audio_io.WavFrameBuffer;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram.Scale;

public class PpmSpectrogramWriter implements SpectrogramWriter {
	
	private static final int MAX_INTENSITY = 255;
	private static final byte[] PGM_MAGIC_NUM = "P5\n".getBytes();

	@Override
	public void write(OutputStream out, Spectrogram spec) throws IOException {
		double[] bins = spec.getBinFloors();
		
		if (! (out instanceof BufferedOutputStream)) {
			out = new BufferedOutputStream(out);
		}
		out.write(PGM_MAGIC_NUM);
		out.write(String.format("%d %d\n", spec.getMaxTick(), bins.length).getBytes());
		out.write(String.format("%d\n", MAX_INTENSITY).getBytes());
		
		System.out.println(spec.getMaxTick() + " ,, " + bins.length);
		
		for (int j = bins.length-1; j >= 0; j--) {
			for (int i = 0; i < spec.getMaxTick(); i++) {
				int color = valToColor(spec.getIntensity(i, j));
				out.write(color);
			}
		}
		
		out.close();
	}

	protected static int valToColor(double value) {
		return (int)Math.max(0, Math.min(value, MAX_INTENSITY));
	}
	
	public static void main(String[] args) throws IOException, WavFileException {
		WavFile wav = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/grindwal.wav"));
		FrameBuffer buffer = new OverlappingFrameBuffer(
				new WavFrameBuffer(wav, 512),
				0.25);
		Spectrogram spec = Spectrogram.inMemory().create(buffer);
		SpectrogramWriter writer = new PpmSpectrogramWriter();
		
		writer.write(
				new FileOutputStream("/Users/chris/src/663_pattern_recognition/project/grindwal.pgm"),
				spec);
	}
}
