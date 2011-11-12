package org.sidoh.song_recognition.database;

import java.io.File;
import java.io.IOException;

import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.signature.Signature;
import org.sidoh.song_recognition.signature.SpectrogramSignatureExtractor;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class WavDatabaseBuilder<S extends Signature, E extends SignatureDatabase<S>> {
	private final SpectrogramSignatureExtractor<S> extractor;
	private final E db;
	private final FrameBuffer.Builder bufferBuilder;
	private final Spectrogram.Builder specBuilder;

	public WavDatabaseBuilder(E db, 
			SpectrogramSignatureExtractor<S> extractor, 
			FrameBuffer.Builder bufferBuilder, 
			Spectrogram.Builder specBuilder) {
		this.db = db;
		this.extractor = extractor;
		this.bufferBuilder = bufferBuilder;
		this.specBuilder = specBuilder;
	}
	
	public void addSong(String name, String filename) throws IOException, WavFileException {
		WavFile wav = WavFile.openWavFile(new File(filename));
		FrameBuffer buffer = bufferBuilder.fromWavFile(wav);
		S signature = extractor.extractSignature(specBuilder.create(buffer));
		
		db.addSong(
			new SongMetaData(new File(filename).getAbsolutePath(), name, (int)(System.currentTimeMillis())),
			signature);
	}
}
