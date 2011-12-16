package org.sidoh.song_recognition.spectrogram;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.imageio.ImageIO;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.io.ProgressNotifier.Builder;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.signature.ConstellationMapExtractor;
import org.sidoh.song_recognition.signature.ConstellationMapSignature;

public class PngSpectrogramConstellationWriter implements SpectrogramWriter {
	private static final int DEFAULT_STAR_WIDTH = 4;
	private static final int DEFAULT_STAR_HEIGHT = 4;
	
	private static final int[] STAR_COLOR = {0xFF, 0, 0};
	private static final int[] SECOND_BARRIER_COLOR = {0, 0xFF, 0};
	// Color to use every 30 seconds
	private static final int[] SECOND_BARRIER2_COLOR = {0, 0, 0xFF};
	private final ConstellationMapExtractor extractor;
	private final int width;
	private final int height;
	private final Builder progress;
	private final boolean invert;
	
	public PngSpectrogramConstellationWriter(ConstellationMapExtractor extractor, ProgressNotifier.Builder progress) {
		this(extractor, progress, false);
	}
	
	public PngSpectrogramConstellationWriter(ConstellationMapExtractor extractor, ProgressNotifier.Builder progress, boolean invert) {
		this(extractor, progress, DEFAULT_STAR_WIDTH, DEFAULT_STAR_HEIGHT, invert);
	}
	
	public PngSpectrogramConstellationWriter(ConstellationMapExtractor extractor,
			ProgressNotifier.Builder progress,
			int width, int height, boolean invert) {
		this.extractor = extractor;
		this.progress = progress;
		this.width = width;
		this.height = height;
		this.invert = invert;
	}

	protected int valToColor(double value) {
		int val = (int)Math.max(0, Math.min(value, 255));
		if (invert) {
			val = 255 - val;
		}
		return val;
	}

	@Override
	public void write(OutputStream out, Spectrogram spec) throws IOException {
		ConstellationMapSignature signature = extractor.extractSignature(spec);
		StarRegionGenerator starRegions = new StarRegionGenerator(signature, spec);
		double[] bins = spec.getBinFloors();
		
		BufferedImage img = new BufferedImage(spec.getMaxTick(), bins.length, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = img.getRaster();
		
		ProgressNotifier notifier = progress.create("Writing image...", bins.length-1);
		
		for (int j = bins.length-1; j >= 0; j--) {
			double frequency = bins[j];
			
			for (int i = 0; i < spec.getMaxTick(); i++) {
				int[] pixel;
				
				if ((starRegions.getSecond(i) % 30) == 0 && starRegions.isSecondBarrier(i)) {
					pixel = SECOND_BARRIER2_COLOR;
				}
				else if (starRegions.isSecondBarrier(i)) {
					pixel = SECOND_BARRIER_COLOR;
				}
				else if (starRegions.isStar(i, frequency)) {
					pixel = STAR_COLOR;
				}
				else {
					int color = valToColor(spec.getIntensity(i, j));
					pixel = new int[]{color,color,color};
				}
				
				raster.setPixel(i, (bins.length-1)-j, pixel);
			}
			
			notifier.update(bins.length-j);
		}
		
		notifier.complete();
		
		ImageIO.write(img, "png", out);
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
		
		public int getSecond(int tick) {
			return (int)Math.floor(spec.tickToSeconds(tick));
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
			int bin = spec.frequencyToBin(frequency);
			
			Collection<Star> candidates = starsBySecond.get((int)Math.floor(time));
			if (candidates == null) {
				return false;
			}
			
			for (Star candidate : candidates) {
				if (Math.abs(bin - candidate.getBin()) <= wHeight
						&& Math.abs(time - candidate.getTime()) <= wWidth) {
					return true;
				}
			}
			
			return false;
		}
	}
}
