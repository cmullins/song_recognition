package org.sidoh.song_recognition.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sidoh.collections.HashOfSets;
import org.sidoh.song_recognition.signature.SignatureExtractor;
import org.sidoh.song_recognition.signature.StarHashComparator;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class HashSignatureDatabase extends SignatureDatabase<StarHashSignature> {
	private static final long serialVersionUID = -2277770804799788503L;
	
	protected HashOfSets<Integer, StarHashSignature> hashes;
	protected Map<StarHashSignature, SongMetaData> songs;

	public HashSignatureDatabase() {
		super(new StarHashComparator());
		hashes = new HashOfSets<Integer, StarHashSignature>();
		songs = new HashMap<StarHashSignature, SongMetaData>();
	}
	
	@Override
	public void addSong(SongMetaData song, StarHashSignature sig) {
		songs.put(sig, song);
		
		for (Integer hash : sig.getStarHashes().keySet()) {
			hashes.addFor(hash, sig);
		}
	}

	@Override
	public QueryResponse<StarHashSignature> findSong(StarHashSignature signature) {
		Set<StarHashSignature> candidates = new HashSet<StarHashSignature>();
		
		for (Integer hash : signature.getStarHashes().keySet()) {
			candidates.addAll(hashes.get(hash));
		}
		
		QueryResponse<StarHashSignature> response = getResponse(candidates, signature);
		response.setSong(songs.get(response.signature()));
		
		return response;
	}
}
