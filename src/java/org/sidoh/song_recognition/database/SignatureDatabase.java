package org.sidoh.song_recognition.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;

import javax.management.RuntimeErrorException;

import org.sidoh.concurrency.SynchronizedMaxTracker;
import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.signature.LikenessComparator;
import org.sidoh.song_recognition.signature.Signature;

public abstract class SignatureDatabase<T extends Signature> implements Serializable {
	private static final long serialVersionUID = 9042108607736960672L;
	
	public static class QueryResponse<T extends Signature> {
		protected double confidence;
		protected SongMetaData song;
		protected T sig;

		public QueryResponse(double confidence, SongMetaData song, T sig) {
			this.confidence = confidence;
			this.song = song;
			this.sig = sig;
		}

		public double confidence() {
			return confidence;
		}

		public SongMetaData song() {
			return song;
		}

		public T signature() {
			return sig;
		}
		
		protected QueryResponse<T> setSong(SongMetaData song) {
			this.song = song;
			return this;
		}
	}
	
	protected final ExecutorService workerPool;
	private final int maxThreads;
	
	public SignatureDatabase(int maxThreads) {
		this.maxThreads = maxThreads;
		workerPool = Executors.newSingleThreadExecutor();
		
		// Add a hook that will shut down the worker pool when the main thread dies.
		final Thread mainThread = Thread.currentThread();
		Thread workPoolKiller = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mainThread.join();
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				finally {
					System.out.println("Shutting down worker pool...");
					workerPool.shutdown();
					System.out.println("Shutting down database...");
					shutdown();
				}
			}
		}, "Worker pool shutdown action");
		workPoolKiller.start();
	}
	
	/**
	 * Add a song to the database. This method runs asynchronously.
	 * 
	 * @param song
	 */
	public final void addSong(final SongMetaData song, final T sig) {
// This is causing memory issues...
//		workerPool.execute(new Runnable() {
//			@Override
//			public void run() {
//				addSongInner(song, sig);
//			}
//		});
		addSongInner(song, sig);
	}
	
	/**
	 * Find a song given a signature.
	 * 
	 * @param signature
	 * @return
	 */
	public abstract QueryResponse<T> findSong(T signature);
	
	/**
	 * Actually add a song to the DB. This method can be blocking.
	 * 
	 * @param song
	 * @param sig
	 */
	protected abstract void addSongInner(SongMetaData song, T sig);
	
	/**
	 * This method is called automatically when the thread that created 
	 * this DB exits. Calling it again shouldn't do any harm. The default
	 * is noop.
	 */
	public void shutdown() {
		
	}
	
	/**
	 * 
	 * @param comparator
	 * @param candidates
	 * @param query
	 * @return
	 */
	protected QueryResponse<T> getResponse(
			LikenessComparator<T> comparator, 
			Collection<? extends T> candidates, 
			T query) {
		return getResponse(ProgressNotifier.nullNotifier(),
			comparator,
			candidates,
			query);
	}
	
	/**
	 * Given a set of candidates, choose the best one using the comparator and
	 * construct a {@link QueryResponse}.
	 * 
	 * @param candidates
	 * @param query
	 * @return
	 */
	protected QueryResponse<T> getResponse(
			final ProgressNotifier.Builder progressBuilder,
			final LikenessComparator<T> comparator, 
			final Collection<? extends T> candidates, 
			final T query) {
		
		final ProgressNotifier progress = progressBuilder.create("Scoring matches...", candidates.size());
		final SynchronizedMaxTracker<Double, T> maxTracker = SynchronizedMaxTracker.<Double, T>defaultComparator().create();
		final Semaphore tasks = new Semaphore(-1 * candidates.size() + 1);
		ExecutorService pool = Executors.newFixedThreadPool(maxThreads);
		
		for (final T candidate : candidates) {
			pool.execute(new Runnable() {
				@Override
				public void run() {
					double score = comparator.similarity(candidate, query);
					
					maxTracker.offer(score, candidate);
					progress.update();
					tasks.release();
				}
			});
		}
		
		pool.shutdown();
		try {
			tasks.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		progress.complete();
		
		return new QueryResponse<T>(maxTracker.maxKey(), null, maxTracker.maxValue());
	}
	
	/**
	 * Save SignatureDatabase to an OutputStream.
	 * 
	 * @param out
	 */
	public void save(OutputStream out) {
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(this);
			objOut.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Load signature database from an InputStream.
	 * 
	 * @param in
	 * @return
	 */
	public static <T extends SignatureDatabase<? extends Signature>> T load(InputStream in) {
		ObjectInputStream objIn;
		try {
			objIn = new ObjectInputStream(in);
			@SuppressWarnings("unchecked")
			T db = (T)objIn.readObject();
			objIn.close();
			
			return db;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
