package org.sidoh.song_recognition.spectrogram;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.sidoh.peak_detection.OutsideNSdsOfMeanPeakDetector;
import org.sidoh.peak_detection.PeakDetector;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.OverlappingFrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.audio_io.WavFrameBuffer;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMapExtractor;
import org.sidoh.song_recognition.signature.ConstellationMapExtractor.FixedFrequencyValues;
import org.sidoh.song_recognition.signature.ConstellationMapSignature;
import org.sidoh.song_recognition.signature.RectangularRegion;
import org.sidoh.song_recognition.signature.Region;
import org.sidoh.song_recognition.signature.StarHashComparator;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram.Scale;

public class PgmSpectrogramConstellationWriter extends PpmSpectrogramWriter {
	private static final int DEFAULT_STAR_WIDTH = 3;
	private static final int DEFAULT_STAR_HEIGHT = 100;
	
	private static final int COLOR_MAX = 255;
	private static final byte[] PGM_MAGIC_NUM = "P6\n".getBytes();
	private static final byte[] STAR_COLOR = {(byte)0xFF, 0, 0};
	private static final byte[] SECOND_BARRIER_COLOR = {0, (byte)0xFF, 0};
	private final ConstellationMapExtractor extractor;
	private final int width;
	private final int height;
	
	public PgmSpectrogramConstellationWriter(ConstellationMapExtractor extractor) {
		this(extractor, DEFAULT_STAR_WIDTH, DEFAULT_STAR_HEIGHT);
	}
	
	public PgmSpectrogramConstellationWriter(ConstellationMapExtractor extractor, int width, int height) {
		this.extractor = extractor;
		this.width = width;
		this.height = height;
	}

	@Override
	public void write(OutputStream out, Spectrogram spec) throws IOException {
		ConstellationMapSignature signature = extractor.extractSignature(spec);
		StarRegionGenerator starRegions = new StarRegionGenerator(signature, spec);
		double[] bins = spec.getBinFloors();
		
		if (! (out instanceof BufferedOutputStream)) {
			out = new BufferedOutputStream(out);
		}
		out.write(PGM_MAGIC_NUM);
		out.write(String.format("%d %d\n", spec.getMaxTick(), bins.length).getBytes());
		out.write(String.format("%d\n", COLOR_MAX).getBytes());
		
		for (int j = bins.length-1; j >= 0; j--) {
			double frequency = bins[j];
			
			for (int i = 0; i < spec.getMaxTick(); i++) {
				if (starRegions.isSecondBarrier(i)) {
					out.write(SECOND_BARRIER_COLOR);
				}
				else if (starRegions.isStar(i, frequency)) {
					out.write(STAR_COLOR);
				}
				else {
					int color = valToColor(spec.getIntensity(i, j));
					out.write(greyToColor(color));
				}
			}
		}
		
		out.close();
	}
	
	protected byte[] greyToColor(int value) {
		byte b = (byte)value;
		return new byte[] { b, b, b };
	}

	private final class StarRegionGenerator {
		private final ConstellationMapSignature sig;
		private Collection<Star> stars;
		private Map<Integer, Collection<Star>> starsBySecond;
		private double wHeight;
		private final Spectrogram spec;
		private final double wWidth;
		private int currentSecond;

		public StarRegionGenerator(ConstellationMapSignature sig, Spectrogram spec) {
			this.sig = sig;
			this.spec = spec;
			this.stars = sig.getConstellationMap().getStars();
			this.starsBySecond = new HashMap<Integer, Collection<Star>>();
			this.currentSecond = 0;
			
			// Divide by two for convenience in #isStar.
			this.wHeight = height/2.0;
			
			// Convert to seconds
			this.wWidth = (spec.tickToSeconds(width) / 2);
			
			for (Star star : stars) {
				int second = (int)Math.floor(star.getTime());
				if (! starsBySecond.containsKey(second)) {
					starsBySecond.put(second, new HashSet<Star>());
				}
				starsBySecond.get(second).add(star);
			}
		}
		
		public boolean isSecondBarrier(int tick) {
			int s = (int)Math.floor(spec.tickToSeconds(tick));
			if (currentSecond != s) {
				currentSecond = s;
				return true;
			}
			return false;
		}
		
		public boolean isStar(int tick, double frequency) {
			double time = spec.tickToSeconds(tick);
			
			Collection<Star> candidates = starsBySecond.get((int)Math.floor(time));
			if (candidates == null) {
				return false;
			}
			
			for (Star candidate : candidates) {
				if (Math.abs(frequency - candidate.getFrequency()) <= wHeight
						&& Math.abs(time - candidate.getTime()) <= wWidth) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	public static void main(String[] args) throws IOException, WavFileException {
		WavFile wav = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/samples/lost-0-to-80.wav"));
		FrameBuffer buffer = new OverlappingFrameBuffer(new WavFrameBuffer(wav, 1024), 0.75);
		WavFile wav2 = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/samples/lost-13-to-27.wav"));
		FrameBuffer buffer2 = new OverlappingFrameBuffer(new WavFrameBuffer(wav2, 1024), 0.75);
//		WavFile wav2 = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/slam-10s-to-15s.wav"));
//		FrameBuffer buffer2 = new OverlappingFrameBuffer(new WavFrameBuffer(wav2, 1024), 0.75);
//		WavFile wav2 = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/grindwal.wav"));
//		FrameBuffer buffer2 = new WavFrameBuffer(wav2, 1024);
//		WavFile wav2 = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/chopin-33-to-53.wav"));
//		FrameBuffer buffer2 = new WavFrameBuffer(wav2, 1024);
//		WavFile wav2 = WavFile.openWavFile(new File("/Users/chris/src/663_pattern_recognition/project/songs/beethoven5-10-to-25s.wav"));
//		FrameBuffer buffer2 = new WavFrameBuffer(wav2, 1024);
		Spectrogram spec = new ConfigurableSpectrogram(
				new FrameBufferSpectrogram(buffer))
			.setContrast(1000);
		Spectrogram spec2 = new ConfigurableSpectrogram(
				new FrameBufferSpectrogram(buffer2))
			.setContrast(1000);
		PeakDetector peakFinder = new OutsideNSdsOfMeanPeakDetector(200,5);
		Region.Builder regionBuilder = Region.rectangularRegion(10, -5, 30, 100);

		ConstellationMapExtractor extractor = new ConstellationMapExtractor(peakFinder, 0.25);
		StarHashExtractor hashExtractor = new StarHashExtractor(extractor, regionBuilder);
		SpectrogramWriter writer = new PgmSpectrogramConstellationWriter(extractor);

//		writer.write(
//				new FileOutputStream("/Users/chris/src/663_pattern_recognition/project/chopin.pgm"),
//				spec);
//		writer.write(
//				new FileOutputStream("/Users/chris/src/663_pattern_recognition/project/slam-10s-to-15s.pgm"),
//				spec2);
//		writer.write(
//				new FileOutputStream("/Users/chris/src/663_pattern_recognition/project/slam.pgm"),
//				spec);
//		writer.write(
//				new FileOutputStream("/Users/chris/src/663_pattern_recognition/project/slam-120s-to-135s.pgm"),
//				spec2);
//		writer.write(
//				new FileOutputStream("/Users/chris/src/663_pattern_recognition/project/3stepoct.pgm"),
//				spec2);

		StarHashSignature sig = hashExtractor.extractSignature(spec);
		StarHashSignature sig2 = hashExtractor.extractSignature(spec2);

		StarHashComparator comparator = new StarHashComparator();
		
		System.out.println(comparator.similarity(sig, sig2));
	}
}
