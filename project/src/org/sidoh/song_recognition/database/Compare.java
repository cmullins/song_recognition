package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.signature.StarHashComparator;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram;
import org.sidoh.song_recognition.spectrogram.FrameBufferSpectrogram;
import org.sidoh.song_recognition.spectrogram.PgmSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class Compare {
	public static void main(String[] args) throws IOException, WavFileException {
		if (args.length < 2) {
			System.err.println("Syntax is: Compare <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		System.out.println("Training on " + args[0] + "...");
		
		FrameBuffer.Builder frameBuilder = WavDatabaseBuilder.Settings.BUFFER_BUILDER;
		StarHashExtractor extractor      = WavDatabaseBuilder.Settings.STAR_HASH_EXTRACTOR;
		Spectrogram mainSpectrogram      = new FrameBufferSpectrogram(frameBuilder.fromWavFile(args[0]));
		StarHashSignature mainSignature  = extractor.extractSignature(mainSpectrogram);
		StarHashComparator comparator    = new StarHashComparator();
		PgmSpectrogramConstellationWriter writer = new PgmSpectrogramConstellationWriter(
				WavDatabaseBuilder.Settings.CONSTELLATION_EXTRACTOR);
		
		OutputStream img = new FileOutputStream("/tmp/spectrograms/" + new File(args[0]).getName());
		writer.write(img, new ConfigurableSpectrogram(mainSpectrogram).setContrast(1000));
		img.close();
		
		for (int i = 1; i < args.length; i++) {
			System.out.println("Comparing " + args[i] + "...");
			
			Spectrogram otherSpectrogram = new FrameBufferSpectrogram(frameBuilder.fromWavFile(args[i]));
			StarHashSignature other      = extractor.extractSignature(otherSpectrogram);
			System.out.println("---> " + comparator.similarity(mainSignature, other));

			img = new FileOutputStream("/tmp/spectrograms/" + new File(args[i]).getName());
			writer.write(img, new ConfigurableSpectrogram(otherSpectrogram).setContrast(1000));
			img.close();
		}
	}
}
