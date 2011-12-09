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
			System.err.println("Syntax is: BulkDbBuilder <db_file> <wavs_dir>");
			System.exit(1);
		}
		
		HashSignatureDatabase db = new HashSignatureDatabase(
				RdbmsHelper.getConnection(args[0]),
				settings);
		WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase> dbBuilder
			= new WavDatabaseBuilder<StarHashSignature, HashSignatureDatabase>(
					db, 
					settings.getStarHashExtractor(),
					settings.getBufferBuilder(),
					settings.getSpectrogramBuilder());
		
		File wavsDir = new File(args[1]);
		if (! wavsDir.exists()) {
			System.err.println("Wav directory doesn't exist!");
			System.exit(1);
		}
		else if (! wavsDir.isDirectory()) {
			System.err.println("specified wav directory isn't a directory!");
			System.exit(1);
		}

		for (File wav : wavsDir.listFiles()) {
			String file = wav.getAbsolutePath();
			System.out.println("Processing: " + file);
			dbBuilder.addSong(wav.getName(), wav.getAbsolutePath());
			System.out.println("--------------------------------------------------------------------------------------------------------");
		}
		
		//System.out.println("Creating indexes...");
		//db.createIndexes();
	}
}
