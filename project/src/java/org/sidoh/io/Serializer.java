package org.sidoh.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Serializes an object into some recognizable form.
 * 
 * @param <T>
 */
public interface Serializer<T> {
	/**
	 * @param object
	 * @param out
	 * @throws IOException
	 */
	public void serialize(T object, OutputStream out) throws IOException;
}
