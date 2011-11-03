package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sidoh.io.StdinPrompts;
import org.sidoh.peak_detection.OutsideNSdsOfMeanPeakDetector;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.signature.ConstellationMapExtractor;
import org.sidoh.song_recognition.signature.Region;
import org.sidoh.song_recognition.signature.Signature;
import org.sidoh.song_recognition.signature.SignatureExtractor;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class WavDatabaseBuilder<S extends Signature, E extends SignatureDatabase<S>> {
	private final SignatureExtractor<S> extractor;
	private final E db;
	private final FrameBuffer.Builder bufferBuilder;
	
	protected static class Settings {
		public static final double STAR_DENSITY_FACTOR = 0.45d;
		
		public static final ConstellationMapExtractor CONSTELLATION_EXTRACTOR
			= new ConstellationMapExtractor(
				new OutsideNSdsOfMeanPeakDetector(100, 5),
				STAR_DENSITY_FACTOR);
		
		public static final Region.Builder REGION_BUILDER
			= Region.rectangularRegion(50, -10, 40, 500);
		
		public static final StarHashExtractor STAR_HASH_EXTRACTOR
			= new StarHashExtractor(CONSTELLATION_EXTRACTOR, REGION_BUILDER);
		
		public static final double FRAME_OVERLAP = 0.15d;
		
		public static final int FRAME_SIZE = 1024;
		
		public static final FrameBuffer.Builder BUFFER_BUILDER
			= FrameBuffer.Builder.frameSize(FRAME_SIZE).sampleOverlap(FRAME_OVERLAP);
	}

	public WavDatabaseBuilder(E db, SignatureExtractor<S> extractor, FrameBuffer.Builder bufferBuilder) {
		this.db = db;
		this.extractor = extractor;
		this.bufferBuilder = bufferBuilder;
	}
	
	public void addSong(String name, String filename) throws IOException, WavFileException {
		WavFile wav = WavFile.openWavFile(new File(filename));
		FrameBuffer buffer = bufferBuilder.fromWavFile(wav);
		S signature = extractor.extractSignature(buffer);
		db.addSong(
			new SongMetaData(filename, name, (int)(System.currentTimeMillis())),
			signature);
	}
}
