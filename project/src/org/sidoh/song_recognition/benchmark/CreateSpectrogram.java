package org.sidoh.song_recognition.benchmark;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.spectrogram.PgmSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class CreateSpectrogram {
	public static void main(String[] args) throws FileNotFoundException, IOException, WavFileException {
		if (args.length < 1) {
			System.err.println("Syntax is: CreateSpectrogram <wav_file> [output_filename=wav_file.pgm]");
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
			: String.format("%s.pgm", args[0]));
		
		PgmSpectrogramConstellationWriter writer
			= new PgmSpectrogramConstellationWriter(
					settings.getConstellationExtractor(),
					settings.getProgressNotifer());
		Spectrogram.Builder specBuilder = settings.getSpectrogramBuilder().contrast(100);
		FrameBuffer.Builder bufferBuilder = settings.getBufferBuilder();
		
		System.out.println(out.getAbsolutePath());
		writer.write(new FileOutputStream(out), 
			specBuilder.create(bufferBuilder.fromWavFile(wav.getAbsolutePath())));
	}
}
