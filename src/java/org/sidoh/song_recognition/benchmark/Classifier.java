package org.sidoh.song_recognition.benchmark;

import java.io.IOException;
import java.sql.SQLException;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.database.RdbmsHelper;
import org.sidoh.song_recognition.database.HashSignatureDatabase;
import org.sidoh.song_recognition.database.SignatureDatabase.QueryResponse;
import org.sidoh.song_recognition.signature.HistogramScorer;
import org.sidoh.song_recognition.signature.LoggingScorer;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class Classifier {
	public static void main(String[] args) throws IOException, WavFileException, SQLException {
		if (args.length < 2) {
			System.err.println("Syntax is: Classifier <db_file> <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		Settings settings = Settings.defaults()
		;
		settings = 
			settings.setProgressNotifer(
				settings.getProgressNotifer().channeled("noisy").controlledByStdin())
		;
		
		HashSignatureDatabase db = new HashSignatureDatabase(
				RdbmsHelper.getH2Connection(args[0]), settings);
		db.loadIntoMemory();
		
		StarHashExtractor extractor = settings.getStarHashExtractor();
		FrameBuffer.Builder frameBuilder = settings.getBufferBuilder();
		Spectrogram.Builder specBuilder = settings.getSpectrogramBuilder();
		
		for (int i = 1; i < args.length; i++) {
			FrameBuffer buffer = frameBuilder.fromWavFile(args[i]);
			Spectrogram spec = specBuilder.create(buffer);
			StarHashSignature clipSignature = extractor.extractSignature(spec);
			QueryResponse<StarHashSignature> response = db.findSong(clipSignature);
			
			String song = (response.song() == null ? "?" : response.song().getFilename());
			System.out.printf("%s|%s|%.16f\n", args[i], song, response.confidence());
		}
	}
}
