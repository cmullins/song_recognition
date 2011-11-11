package org.sidoh.song_recognition.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

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
	
	/**
	 * Add a song to the database.
	 * 
	 * @param song
	 */
	public abstract void addSong(SongMetaData song, T sig);
	
	/**
	 * Find a song given a signature.
	 * 
	 * @param signature
	 * @return
	 */
	public abstract QueryResponse<T> findSong(T signature);
	
	/**
	 * Given a set of candidates, choose the best one using the comparator and
	 * construct a {@link QueryResponse}.
	 * 
	 * @param candidates
	 * @param query
	 * @return
	 */
	protected static <T extends Signature> QueryResponse<T> getResponse(
			LikenessComparator<T> comparator, 
			Collection<? extends T> candidates, 
			T query) {
		double bestScore = Double.NEGATIVE_INFINITY;
		T best = null;
		
		for (T candidate : candidates) {
			double score = comparator.similarity(candidate, query);
			
			if (score > bestScore) {
				bestScore = score;
				best  = candidate;
			}
		}
		
		return new QueryResponse<T>(bestScore, null, best);
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
