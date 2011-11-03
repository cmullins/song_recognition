package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.database.SignatureDatabase.QueryResponse;
import org.sidoh.song_recognition.signature.Signature;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram;
import org.sidoh.song_recognition.spectrogram.FrameBufferSpectrogram;
import org.sidoh.song_recognition.spectrogram.PgmSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class Classifier {
	public static void main(String[] args) throws IOException, WavFileException {
		if (args.length < 2) {
			System.err.println("Syntax is: Classifier <db_file> <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		File dbFile = new File(args[0]);
		if (! dbFile.exists()) {
			System.err.println("Db file doesn't exist. Try again.");
			System.exit(1);
		}
		
		SignatureDatabase<Signature> db = SignatureDatabase.load(new FileInputStream(dbFile));
		StarHashExtractor extractor = new StarHashExtractor(
				WavDatabaseBuilder.Settings.CONSTELLATION_EXTRACTOR,
				WavDatabaseBuilder.Settings.REGION_BUILDER);
		FrameBuffer.Builder frameBuilder = WavDatabaseBuilder.Settings.BUFFER_BUILDER;
		PgmSpectrogramConstellationWriter writer = new PgmSpectrogramConstellationWriter(
				WavDatabaseBuilder.Settings.CONSTELLATION_EXTRACTOR);
		
		for (int i = 1; i < args.length; i++) {
			System.out.println("Classifying " + args[i] + "...");
			FrameBuffer buffer = frameBuilder.fromWavFile(args[i]);
			Spectrogram spec = new FrameBufferSpectrogram(buffer);
			
			OutputStream img = new FileOutputStream(new File("/tmp/spectrograms/" + new File(args[i]).getName()));
			writer.write(img, new ConfigurableSpectrogram(spec).setContrast(1000));
			img.close();
			
			StarHashSignature clipSignature = extractor.extractSignature(spec);
			QueryResponse<Signature> response = db.findSong(clipSignature);
			
			if (response.song() != null) {
				System.out.printf("      ---> This is: %s, with confidence: %f\n", response.song().getName(), response.confidence());
			}
			else {
				System.out.println("     ---> Dunno!");
			}
		}
	}
}
