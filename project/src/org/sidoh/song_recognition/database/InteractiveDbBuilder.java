package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sidoh.io.StdinPrompts;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.database.WavDatabaseBuilder.Settings;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class InteractiveDbBuilder {
	public static void main(String[] args) throws IOException, WavFileException {
		HashSignatureDatabase db;
		
		File dbFile = StdinPrompts.promptForFile("Please enter file to save DB to. If it exists, it will be loaded.", false, false);
		if (dbFile.exists()) {
			db = SignatureDatabase.load(new FileInputStream(dbFile));
		}
		else {
			db = new HashSignatureDatabase();
		}
		System.out.println("DB Loaded!");
		System.out.println();
		
		WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase> dbBuilder
			= new WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase>(db, Settings.STAR_HASH_EXTRACTOR, Settings.BUFFER_BUILDER);
		
		while (true) {
			File songFile = StdinPrompts.promptForFile("Enter a .wav file (blank to stop): ", true, true);
			
			if (songFile.getName().isEmpty()) {
				break;
			}
			
			String songName = StdinPrompts.promptForLine("Enter a name for this song:");
			dbBuilder.addSong(songName, songFile.getAbsolutePath());
		}
		
		db.save(new FileOutputStream(dbFile));
	}
}
