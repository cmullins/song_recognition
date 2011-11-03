package org.sidoh.song_recognition.signature;

import java.io.Serializable;

/**
 * Any class implementing this interface should be able compare objects
 * of the same type and return a score (between 0 and 1, inclusive) 
 * indicating how similar the two are.
 * 
 * @author chris
 *
 * @param <T>
 */
public interface LikenessComparator<T extends Signature> extends Serializable {
	public double similarity(T o1, T o2);
}
