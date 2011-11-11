package org.sidoh.song_recognition.signature;

import java.util.Map;
import java.util.Set;

public class ReconstructedStarHashSignature extends StarHashSignature {
	private static final long serialVersionUID = -1764750222469645734L;
	private final Map<Integer, Set<Integer>> hashes;
	private int id;

	public ReconstructedStarHashSignature(Map<Integer, Set<Integer>> hashes) {
		super(null, hashes);
		this.id = 0;
		this.hashes = hashes;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReconstructedStarHashSignature other = (ReconstructedStarHashSignature) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	public String toString() {
		return "[ReconstructedStarHashSignature; id=" + id + "]";
	}
}
