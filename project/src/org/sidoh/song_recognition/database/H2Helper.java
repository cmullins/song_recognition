package org.sidoh.song_recognition.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2Helper {
	public static Connection getConnection(String filename) {
		try {
			Class.forName("org.h2.Driver");
			return DriverManager.getConnection(
				String.format("jdbc:h2:%s", new File(filename).getAbsolutePath()),
				"sa",
				"");
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
