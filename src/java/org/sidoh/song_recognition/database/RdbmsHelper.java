package org.sidoh.song_recognition.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RdbmsHelper {
	/**
	 * A helper method to get the global default RDBMS connection.
	 * 
	 * @param filename
	 * @return
	 */
	public static Connection getConnection(String filename) {
		return getDerbyConnection(filename);
	}
	
	public static Connection getDerbyConnection(String filename) {
		try {
			// Check if the database exists. If it doesn't, append ;create=true to
			// the URL.
			File db = new File(filename);
			boolean exists = false;
			
			if (! db.exists()) {
				// Check for files that have the same prefix in this directory
				for (File child : db.getParentFile().listFiles()) {
					if (child.getName().startsWith(db.getName())) {
						exists = true;
					}
				}
			}
			
			System.setProperty("derby.stream.error.file","error.txt");
			System.setProperty("derby.system.home", db.getParent());
			
			// Largest page size Derby allows. This will reduce the number of I/O 
			// operations and speed things up a bit.
			System.setProperty("derby.storage.pageSize","32768");
			// Larger page cache will speed things up. TODO: determine this dynamically
			// from Runtime.getRuntime().
			// 32KB page size / 10K cache size ~= 312MB of memory (not including 
			// overhead).
			//System.setProperty("derby.storage.pageCacheSize", "10000");
			
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			String url = String.format("jdbc:derby:%s", db.getAbsolutePath());
			
			if (! exists) {
				url = url.concat(";create=true");
			}
			
			return DriverManager.getConnection(url);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to load Derby db driver!", e);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		catch (InstantiationException e) {
			throw new RuntimeException("Failed to load Derby db driver!", e);
		} 
		catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to load Derby db driver!", e);
		}
	}
	
	public static Connection getH2Connection(String filename) {
		try {
			System.setProperty("h2.serverCachedObjects", "200000");
			Class.forName("org.h2.Driver");
			return DriverManager.getConnection(
				String.format("jdbc:h2:%s;DB_CLOSE_ON_EXIT=FALSE;TRACE_LEVEL_FILE=3", new File(filename).getAbsolutePath()),
				"sa",
				"");
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("ERROR: Couldn't load H2 db driver");
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Connection getHSQLConnection(String filename) {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			return DriverManager.getConnection(
				String.format("jdbc:hsqldb:file:%s;shutdown=true", new File(filename).getAbsoluteFile()),
				"SA",
				"");
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException("ERROR: Couldn't load HSQL db driver", e);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
