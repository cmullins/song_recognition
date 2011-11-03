package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.database.WavDatabaseBuilder.Settings;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class BulkDbBuilder {
	public static void main(String[] args) throws IOException, WavFileException {
		if (args.length < 2) {
			System.err.println("Syntax is: BulkDbBuilder <db_file> <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		File dbFile = new File(args[0]);
		if (dbFile.exists()) {
			System.err.println(args[0] + " exists! Delete it first.");
			System.err.println("Syntax is: BulkDbBuilder <db_file> <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		HashSignatureDatabase db = new HashSignatureDatabase();
		WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase> dbBuilder
			= new WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase>(db, Settings.STAR_HASH_EXTRACTOR, Settings.BUFFER_BUILDER);
		
		for (int i = 1; i < args.length; i++) {
			System.out.println("Loading " + args[i] + "...");
			dbBuilder.addSong(args[i], args[i]);
		}
		
		System.out.println("Done! Saving db...");
		db.save(new FileOutputStream(dbFile));
	}
}
