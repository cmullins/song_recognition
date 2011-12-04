package org.sidoh.song_recognition.benchmark;

import java.io.Serializable;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.peak_detection.StatefulPeakDetector;
import org.sidoh.peak_detection.StatefulSdsFromMeanPeakDetector;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFile;
import org.sidoh.song_recognition.signature.ConstellationMapExtractor;
import org.sidoh.song_recognition.signature.HistogramScorer;
import org.sidoh.song_recognition.signature.Region;
import org.sidoh.song_recognition.signature.StarBuffer;
import org.sidoh.song_recognition.signature.StarHashComparator;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.spectrogram.PgmSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;
import org.sidoh.song_recognition.spectrogram.SpectrogramWriter;
import org.sidoh.song_recognition.transform.Transform;

/**
 * Defines a lot of settings to be used all over the place for the audio recognition task.
 * Provides methods to update settings and propogate the updated settings to other things
 * that are affected by them.
 * 
 * Provides default values for every setting.
 * 
 * @author chris
 */
public class Settings implements Serializable {
	private static final long serialVersionUID = 2133458107090573639L;

	private Settings() {
		
	}
	
	public static Settings defaults() {
		return new Settings();
	}
	
	/**
	 * Progress notifier to use for various things that take a while
	 */
	private ProgressNotifier.Builder progressNotifer
		= ProgressNotifier.consoleNotifier(50);
	
	/**
	 * This controls how many peaks are kept from each song
	 */
	private double starDensityFactor = 0.2d;
	
	/**
	 * Controls how granular time is in signatures, etc.
	 */
	private int timeResolution = 10;
	
	/**
	 * This value defines the percentage of a frame that should be repeated 
	 * in the next frame. A higher value means the resulting spectrogram will
	 * have more time resolution. This is generally a good thing, but also
	 * results in more space taken for hash values, and more time taken for
	 * computing matches (and more memory taken for the spectrogram).
	 */
	private double frameOverlap = 0.05d;
	
	/**
	 * This defines the number of samples in a frame. The higher the number,
	 * the more frequency resolution (and less time resolution) there will 
	 * be. Note that due to restrictions in the DCT, this must be a power of 
	 * two.
	 */
	private int frameSize = 512;
	
	/**
	 * Responsible for building transform functions. 
	 */
	private Transform.Builder transformBuilder
		= Transform.Builder.frameSize(frameSize);
	
	/**
	 * This controls how spectrograms are constructed. By default, use the
	 * same in-memory buffer for all spectrograms. Note that this forces the
	 * processing of multiple spectrograms to be serial (non-parallel).
	 */
	private Spectrogram.Builder spectrogramBuilder
		= Spectrogram.singletonStorage().progressNotifier(progressNotifer).transformBuilder(transformBuilder);
	
	/**
	 * Window size for peak detector
	 */
	private int windowSize = 250;
	
	/**
	 * Num SDs above mean.
	 */
	private double nSdsAboveMean = 3;
	
	/**
	 * Peak detection algorithm to use. Because of the way spectrograms are 
	 * processed, we require a {@link StatefulPeakDetector}. This prevents making
	 * large memory buffers necessary.
	 */
	private StatefulPeakDetector.Builder peakDetector
		= StatefulPeakDetector.sdsFromMean(windowSize, nSdsAboveMean);

	/**ll usually involve some calculation
	 * using {@link #starDensityFactor}.
	 */
	private StarBuffer.Builder starSelectionBuffer
		= StarBuffer.evenlySpreadInTime(starDensityFactor);
	
	/**
	 * The maximum number of threads to use in processes that are capable of doing 
	 * parallel processing. The default is the number o
	 * This controls how stars are selected. It wif processors on the current
	 * machine.
	 */
	private int maxNumThreads
		= Runtime.getRuntime().availableProcessors();
	
	/**
	 * This is the thing that extracts peaks from a spectrogram and puts them all
	 * into a nice, clean interface.
	 */
	private ConstellationMapExtractor constellationExtractor
		= new ConstellationMapExtractor(peakDetector, starDensityFactor, progressNotifer, starSelectionBuffer, maxNumThreads);
	
	/**
	 * This is responsible for constructing a region for a given "anchor point" in
	 * the constellation map. It controls which pairs of peaks get selected for
	 * hashing.
	 */
	private Region.Builder regionBuilder
		= Region.rectangularRegion(100, -3, 10, 300);
	
	/**
	 * This performs the same function as {@link #regionBuilder}, but is used when
	 * traversing the stars in descending order of time. This is OPTIONAL. If you
	 * don't want to use it, set it to null and reverse pairing won't be done. You
	 * will very likely want to call {@link Region.Builder#reverse()} on this param.
	 */
	private Region.Builder reverseRegionBuilder
		= null;
	
	/**
	 * This defines an algorithm for scoring a histogram generated by computing the
	 * time deltas of matching hashes. This defines exactly the value that the
	 * {@link StarHashComparator} returns, so it should be rather robust to the sorts
	 * of things that we'd like our FINAL scores to be robust to.
	 */
	private HistogramScorer histogramScorer
		= HistogramScorer.heightScorer();
	
	/**
	 * This extracts hash values from a spectrogram.
	 */
	private StarHashExtractor starHashExtractor
		= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
	
	/**
	 * This is the thing that will produce scores for potential matches. It's 
	 * essentially a wrapper around a {@link HistogramScorer}.
	 */
	private StarHashComparator starHashComparator
		= new StarHashComparator(histogramScorer);
	
	/**
	 * Defines how {@link FrameBuffer}s are constructed given a 
	 * {@link WavFile}.
	 */
	private FrameBuffer.Builder bufferBuilder
		= FrameBuffer.frameSize(frameSize).sampleOverlap(frameOverlap);

	public ProgressNotifier.Builder getProgressNotifer() {
		return progressNotifer;
	}

	public double getStarDensityFactor() {
		return starDensityFactor;
	}

	public int getTimeResolution() {
		return timeResolution;
	}

	public Spectrogram.Builder getSpectrogramBuilder() {
		return spectrogramBuilder;
	}

	public StatefulPeakDetector.Builder getPeakDetector() {
		return peakDetector;
	}

	public ConstellationMapExtractor getConstellationExtractor() {
		return constellationExtractor;
	}

	public Region.Builder getRegionBuilder() {
		return regionBuilder;
	}
	
	public Region.Builder getReverseRegionBuilder() {
		return reverseRegionBuilder;
	}

	public HistogramScorer getHistogramScorer() {
		return histogramScorer;
	}
	
	public int getMaxNumThreads() {
		return maxNumThreads;
	}

	public StarHashExtractor getStarHashExtractor() {
		return starHashExtractor;
	}

	public StarHashComparator getStarHashComparator() {
		return starHashComparator;
	}

	public double getFrameOverlap() {
		return frameOverlap;
	}

	public int getFrameSize() {
		return frameSize;
	}

	public FrameBuffer.Builder getBufferBuilder() {
		return bufferBuilder;
	}
	
	public int getWindowSize() {
		return windowSize;
	}
	
	public double getNSdsAboveMean() {
		return nSdsAboveMean;
	}
	
	public SpectrogramWriter getSpectrogramWriter() {
		return new PgmSpectrogramConstellationWriter(constellationExtractor, progressNotifer);
	}
	
	public StarBuffer.Builder getStarSelectionBuffer() {
		return starSelectionBuffer;
	}
	
	public Settings setNSdsAboveMean(double nSdsAboveMean) {
		this.nSdsAboveMean = nSdsAboveMean;
		if (peakDetector instanceof StatefulSdsFromMeanPeakDetector.Builder) {
			return this.setPeakDetector(StatefulPeakDetector.sdsFromMean(windowSize, nSdsAboveMean));
		}
		return this;
	}
	
	public Settings setWindowSize(int windowSize) {
		this.windowSize = windowSize;
		if (peakDetector instanceof StatefulSdsFromMeanPeakDetector.Builder) {
			return this.setPeakDetector(StatefulPeakDetector.sdsFromMean(windowSize, nSdsAboveMean));
		}
		else {
			return this.setPeakDetector(StatefulPeakDetector.meanDelta(windowSize));
		}
	}
	
	public Settings setProgressNotifer(ProgressNotifier.Builder progressNotifer) {
		this.progressNotifer = progressNotifer;
		this.constellationExtractor
			= new ConstellationMapExtractor(peakDetector, starDensityFactor, progressNotifer, starSelectionBuffer, maxNumThreads);
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		this.spectrogramBuilder = spectrogramBuilder.progressNotifier(progressNotifer);
		return this;
	}

	public Settings setStarDensityFactor(double starDensityFactor) {
		this.starDensityFactor = starDensityFactor;
		this.starSelectionBuffer = this.starSelectionBuffer.starDensityFactor(starDensityFactor);
		this.constellationExtractor
			= new ConstellationMapExtractor(peakDetector, starDensityFactor, progressNotifer, starSelectionBuffer, maxNumThreads);
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}

	public Settings setTimeResolution(int timeResolution) {
		this.timeResolution = timeResolution;
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}

	public Settings setPeakDetector(StatefulPeakDetector.Builder peakDetector) {
		this.peakDetector = peakDetector;
		this.constellationExtractor
			= new ConstellationMapExtractor(peakDetector, starDensityFactor, progressNotifer, starSelectionBuffer, maxNumThreads);
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}

	public Settings setRegionBuilder(Region.Builder regionBuilder) {
		this.regionBuilder = regionBuilder;
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}
	
	public Settings setReverseRegionBuilder(Region.Builder reverseRegionBuilder) {
		this.reverseRegionBuilder = reverseRegionBuilder;
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}

	public Settings setHistogramScorer(HistogramScorer histogramScorer) {
		this.histogramScorer = histogramScorer;
		this.starHashComparator = new StarHashComparator(histogramScorer);
		return this;
	}

	public Settings setFrameOverlap(double frameOverlap) {
		this.frameOverlap = frameOverlap;
		this.bufferBuilder = this.bufferBuilder.sampleOverlap(frameOverlap);
		return this;
	}

	public Settings setFrameSize(int frameSize) {
		this.frameSize = frameSize;
		this.bufferBuilder = this.bufferBuilder.frameSize(frameSize);
		return this;
	}
	
	public Settings setStarSelectionBuffer(StarBuffer.Builder starSelectionBuffer) {
		this.starSelectionBuffer = starSelectionBuffer;
		this.constellationExtractor
			= new ConstellationMapExtractor(peakDetector, starDensityFactor, progressNotifer, starSelectionBuffer, maxNumThreads);
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}
	
	public Settings setMaxNumThreads(int maxNumThreads) {
		this.maxNumThreads = maxNumThreads;
		this.constellationExtractor
			= new ConstellationMapExtractor(peakDetector, starDensityFactor, progressNotifer, starSelectionBuffer, maxNumThreads);
		this.starHashExtractor
			= new StarHashExtractor(constellationExtractor, regionBuilder, reverseRegionBuilder, timeResolution, progressNotifer);
		return this;
	}
	
	public Settings setSpectrogramBuilder(Spectrogram.Builder builder) {
		this.spectrogramBuilder = builder.progressNotifier(progressNotifer).transformBuilder(transformBuilder);
		return this;
	}
}