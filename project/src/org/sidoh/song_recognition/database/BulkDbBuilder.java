package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.benchmark.Settings;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class BulkDbBuilder {
	public static void main(String[] args) throws IOException, WavFileException, SQLException {
		Settings settings = Settings.defaults();
		
		if (args.length < 2) {
			System.err.println("Syntax is: BulkDbBuilder <db_file> <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		HashSignatureDatabase db = new HashSignatureDatabase(
				H2Helper.getConnection(args[0]),
				settings);
		WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase> dbBuilder
			= new WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase>(
					db, 
					settings.getStarHashExtractor(),
					settings.getBufferBuilder(),
					settings.getSpectrogramBuilder());

		for (int i = 1; i < args.length; i++) {
			System.out.println("Loading " + args[i] + "...");
			dbBuilder.addSong(new File(args[i]).getName(), args[i]);
			System.out.println("--------------------------------------------------------------------------------------------------------");
		}
		
		System.out.println("Creating indexes...");
		db.createIndexes();
		
		System.out.println("All done.");
	}
}
