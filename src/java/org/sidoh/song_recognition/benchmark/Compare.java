package org.sidoh.song_recognition.benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.signature.LoggingScorer;
import org.sidoh.song_recognition.signature.StarHashComparator;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram;
import org.sidoh.song_recognition.spectrogram.PgmSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class Compare {
	public static void main(String[] args) throws IOException, WavFileException {
		if (args.length < 2) {
			System.err.println("Syntax is: Compare <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		Settings settings = Settings.defaults();
		settings
			.setHistogramScorer(new LoggingScorer("/tmp/histograms", settings.getHistogramScorer()))
			.setProgressNotifer(ProgressNotifier.nullNotifier())
		;
		
		System.out.println("Training on " + args[0] + "...");
		
		FrameBuffer.Builder frameBuilder           = settings.getBufferBuilder();
		StarHashExtractor extractor                = settings.getStarHashExtractor();
		Spectrogram.Builder specBuilder = settings.getSpectrogramBuilder();
		
		Spectrogram mainSpectrogram      = specBuilder.create(frameBuilder.fromWavFile(args[0]));
		StarHashSignature mainSignature  = extractor.extractSignature(mainSpectrogram);
		StarHashComparator comparator    = settings.getStarHashComparator();
		PgmSpectrogramConstellationWriter writer 
			= new PgmSpectrogramConstellationWriter(
					settings.getConstellationExtractor(),
					settings.getProgressNotifer());
		
		OutputStream img = new FileOutputStream("/tmp/spectrograms/" + new File(args[0]).getName());
		writer.write(img, new ConfigurableSpectrogram(specBuilder.create(frameBuilder.fromWavFile(args[0]))).setContrast(200));
		img.close();
		
		for (int i = 1; i < args.length; i++) {
			System.out.println("Comparing " + args[i] + "...");
			
			Spectrogram otherSpectrogram = specBuilder.create(frameBuilder.fromWavFile(args[i]));
			StarHashSignature other      = extractor.extractSignature(otherSpectrogram);
			System.out.println("---> " + comparator.similarity(mainSignature, other));

			img = new FileOutputStream("/tmp/spectrograms/" + new File(args[i]).getName());
			writer.write(img, new ConfigurableSpectrogram(specBuilder.create(frameBuilder.fromWavFile(args[i]))).setContrast(200));
			img.close();
		}
	}
}
