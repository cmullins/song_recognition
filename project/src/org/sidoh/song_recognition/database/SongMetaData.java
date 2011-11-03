package org.sidoh.song_recognition.database;

import java.io.Serializable;

public class SongMetaData implements Serializable {
	private static final long serialVersionUID = -2438398130255866971L;
	
	private String filename;
	private String name;
	private int id;

	public SongMetaData(String filename, String name, int id) {
		this.filename = filename;
		this.name = name;
		this.id = id;
	}
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
		
}
