package org.sidoh.song_recognition.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sidoh.peak_detection.StatefulPeakDetector;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.spectrogram.PngSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;
import org.sidoh.song_recognition.spectrogram.SpectrogramWriter;

public class CreateSpectrogram {
	public static void main(String[] args) throws FileNotFoundException, IOException, WavFileException {
		if (args.length < 1) {
			System.err.println("Syntax is: CreateSpectrogram <wav_file> [output_filename=wav_file.png]");
			System.exit(1);
		}
		
		Settings settings = Settings.defaults();
		
		File wav = new File(args[0]);
		if (! wav.exists()) {
			System.err.println(args[0] + " doesn't exist. Try again.");
			System.exit(1);
		}
		
		File out = new File(args.length > 1
			? args[1]
			: String.format("%s.png", args[0]));
		
		SpectrogramWriter writer
			= new PngSpectrogramConstellationWriter(
					settings.getConstellationExtractor(), 
					settings.getProgressNotifer(), true);
		Spectrogram.Builder specBuilder = settings.getSpectrogramBuilder()
				.contrast(400);
		
		FrameBuffer.Builder bufferBuilder = settings.getBufferBuilder();
		
		System.out.println(out.getAbsolutePath());
		writer.write(new FileOutputStream(out), 
			specBuilder.create(bufferBuilder.fromWavFile(wav.getAbsolutePath())));
	}
}
