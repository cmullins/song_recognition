package org.sidoh.song_recognition.signature;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.io.ProgressNotifier.Builder;
import org.sidoh.peak_detection.PeakListener;
import org.sidoh.peak_detection.StatefulPeakDetector;
import org.sidoh.peak_detection.StatelessPeakDetector;
import org.sidoh.song_recognition.signature.ConstellationMap.Star;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class ConstellationMapExtractor implements SpectrogramSignatureExtractor<ConstellationMapSignature> {
	
	private final StatefulPeakDetector.Builder peakDetector;
	private final double starDensityFactor;
	private final ProgressNotifier.Builder progressNotifer;
	private final int threadCount;
	private final StarBuffer.Builder bufferBuilder;
	
	public ConstellationMapExtractor(StatefulPeakDetector.Builder peakDetectorBuilder, 
			double starDensityFactor, 
			ProgressNotifier.Builder progressNotifer,
			StarBuffer.Builder bufferBuilder) {
		this(peakDetectorBuilder,
			starDensityFactor,
			progressNotifer,
			bufferBuilder,
			Runtime.getRuntime().availableProcessors());
	}
	
	public ConstellationMapExtractor(StatefulPeakDetector.Builder peakDetectorBuilder, 
			double starDensityFactor, 
			ProgressNotifier.Builder progressNotifer,
			StarBuffer.Builder bufferBuilder,
			int threadCount) {
		this.peakDetector = peakDetectorBuilder;
		this.starDensityFactor = starDensityFactor;
		this.progressNotifer = progressNotifer;
		this.bufferBuilder = bufferBuilder;
		this.threadCount = threadCount;
	}

	@Override
	public ConstellationMapSignature extractSignature(Spectrogram spec) {
		return new ConstellationMapSignature(findStars(spec), spec);
	}
	
	public ConstellationMapExtractor quiet() {
		return new ConstellationMapExtractor(peakDetector, starDensityFactor, ProgressNotifier.nullNotifier(), bufferBuilder, threadCount);
	}
	
	/**
	 * Helper method to populate a ConstellationMap from the peaks that the {@link StatelessPeakDetector}
	 * provides.
	 * 
	 * @param spec
	 * @return
	 */
	protected ConstellationMap findStars(Spectrogram spec) {
		ConstellationMap map = new ConstellationMap();
		StarBuffer stars = bufferBuilder.create(spec);
		
		StarFinder finder = new StarFinder(stars, spec, peakDetector, threadCount, progressNotifer);
		stars = finder.findStars();
		map.addAll(stars.flush());
		
		return map;
//		ConstellationMap map = new ConstellationMap();
//		StarBuffer stars = new SpreadStarBuffer(spec);
//		
//		double[] binFloors = spec.getBinFloors();
//		int numBins = binFloors.length;
//		
//		for (int bin = 0; bin < numBins; bin++) {
//			Iterable<Integer> peaks = peakDetector.findPeaks(new FixedFrequencyValues(spec, bin));
//			
//			for (Integer tick : peaks) {
//				stars.add(tick, getStar(spec, tick, bin));
//			}
//		}
//		
//		map.addAll(stars.flush());
//		
//		return map;
	}
	
	/**
	 * Get the maximum number of stars per second of audio. This is computed using
	 * {@link #starDensityFactor}, which is provided in the constructor.
	 * 
	 * @param spec
	 * @return
	 */
	protected int getStarsPerSecond(Spectrogram spec) {
		return (int)Math.floor(
				(starDensityFactor * spec.getMaxTick()) 
					/ 
				(double)spec.tickToSeconds(spec.getMaxTick()));
	}
	
	protected int getMaxStars(Spectrogram spec) {
		return (int)Math.floor(starDensityFactor*spec.getMaxTick());
	}
	
	
	/**
	 * 
	 * @param spec
	 * @param x
	 * @param y
	 * @return
	 */
	protected static Star getStar(final Spectrogram spec, final int x, final int y) {
		double seconds = spec.tickToSeconds(x);
		double freq    = spec.binToFrequency(y);
		return new Star(seconds, freq, spec.getIntensity(x, y), x, y);
	}
	
	/**
	 * 
	 * @param spec
	 * @param x
	 * @param y
	 * @return
	 */
	protected static Star getStar(final Spectrogram spec, final int x, final int y, final double value) {
		double seconds = spec.tickToSeconds(x);
		double freq    = spec.binToFrequency(y);
		return new Star(seconds, freq, value, x, y);
	}
	
	protected static class StarFinder {
		protected StarBuffer buffer;
		protected int maxValue;
		protected final Spectrogram spec;
		protected final Semaphore activeThreads;
		protected final int numBins;
		private final StatefulPeakDetector.Builder peakDetector;
		private final ExecutorService threadPool;
		private final int numThreads;
		private final Builder progressNotifier;
		
		public StarFinder(StarBuffer buffer, 
				Spectrogram spec, 
				StatefulPeakDetector.Builder peakDetector, 
				int numThreads,
				ProgressNotifier.Builder progressNotifier) {
			this.buffer = buffer;
			this.spec = spec;
			this.peakDetector = peakDetector;
			this.numThreads = numThreads;
			this.progressNotifier = progressNotifier;
			this.maxValue = 0;
			this.numBins = spec.getBinFloors().length;
			
			this.activeThreads = new Semaphore(0);
			this.threadPool = Executors.newFixedThreadPool(numThreads);
		}
		
		public StarBuffer findStars() {
			List<WorkerThread> workers = new LinkedList<WorkerThread>();
			int freqPerThread = (numBins / numThreads);
			for (int i = 0; i < numBins; i += freqPerThread) {
				int frstFreq = i;
				int lastFreq = Math.min(numBins, (i+freqPerThread-1));
				
				// If this is the last thread we have, and there's still some left over at the end,
				// lump the stuff at the end with this one.
				if (((numBins - 1) - lastFreq) < freqPerThread) {
					lastFreq += ((numBins - 1) - lastFreq);
					i = numBins-1;
				}
				
				WorkerThread thread = new WorkerThread(frstFreq, lastFreq);
				
				workers.add(thread);
				threadPool.execute(thread);
			}
			
			ProgressNotifier progress = progressNotifier.create("Finding peaks...", spec.getMaxTick());
			
			while (maxValue < spec.getMaxTick()) {
				try {
					activeThreads.acquire(numThreads);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				maxValue++;
				
				for (WorkerThread thread : workers) {
					synchronized (thread) {
						thread.notify();
					}
				}
				
				progress.update(maxValue);
			}
			
			progress.complete();
			
			threadPool.shutdown();
			return buffer;
		}
		
		private class PeakTracker implements PeakListener {
			private final int frequency;

			public PeakTracker(int frequency) {
				this.frequency = frequency;
			}

			@Override
			public void peakDetected(int index, double value) {
				buffer.offerStar(getStar(spec, index, frequency, value));
			}
		}
		
		private class WorkerThread implements Runnable {
			private final List<StatefulPeakDetector> detectors;
			private final int minFreq;
			private final int maxFreq;
			private int lastIndex;

			public WorkerThread(int minFreq, int maxFreq) {
				this.minFreq = minFreq;
				this.maxFreq = maxFreq;
				this.detectors = new ArrayList<StatefulPeakDetector>(maxFreq-minFreq+1);
				for (int i = minFreq; i <= maxFreq; i++) {
					detectors.add(peakDetector.create(new PeakTracker(i)));
				}
				this.lastIndex = -1;
			}

			@Override
			public void run() {
				while (lastIndex < spec.getMaxTick()) {
					for (int freq = minFreq; freq <= maxFreq; freq++) {
						int i = freq-minFreq;
						detectors.get(i).offerNewValue(spec.getIntensity((lastIndex+1), freq));
					}
					
					if ((lastIndex+1) > maxValue) {
						synchronized (this) {
							try {
								activeThreads.release();
								wait();
							} catch (InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					}
					lastIndex++;
				}
			}
		}
	}
}
