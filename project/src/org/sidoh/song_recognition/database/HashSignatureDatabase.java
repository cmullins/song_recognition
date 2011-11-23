package org.sidoh.song_recognition.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sidoh.collections.DefaultingHashMap;
import org.sidoh.collections.HashOfSets;
import org.sidoh.collections.Pair;
import org.sidoh.collections.TreeMapOfSets;
import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.benchmark.Settings;
import org.sidoh.song_recognition.signature.ReconstructedStarHashSignature;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class HashSignatureDatabase extends SignatureDatabase<StarHashSignature> {
	private static final long serialVersionUID = 3430533360857830026L;

	protected static class DbHelper {
		public static final String SONG_HASHES_DEFN =
			"CREATE TABLE IF NOT EXISTS song_hashes (" +
				"hash_value INT, " +
				"time_offset INT, " +
				"song_id INT);";

		public static final String INDEX_DEFN =
			"CREATE INDEX IF NOT EXISTS song_hashes_hash_value ON song_hashes(hash_value);";
		
		public static final String SONGS_DEFN =
			"CREATE TABLE IF NOT EXISTS songs (" +
				"id INT PRIMARY KEY AUTO_INCREMENT, " +
				"name varchar(255), " +
				"artist varchar(255), " +
				"genre varchar(255));";
		
		public static final String INSERT_HASHES_QUERY =
			"INSERT INTO song_hashes (hash_value, time_offset, song_id) VALUES (?,?,?);";
		
		public static final String INSERT_SONG_QUERY =
			"INSERT INTO songs (name,artist,genre) VALUES (?,?,?);";
		
		public static final String FIND_MATCHING_HASHES_QUERY =
			"SELECT * FROM song_hashes WHERE hash_value = ?;";
		
		public static final String FIND_SONG_QUERY =
			"SELECT * FROM songs WHERE id = ?;";
		
		public static final String GET_LAST_ID_QUERY =
			"CALL IDENTITY();";
		
		private final Connection db;
		private PreparedStatement insertHashesStatement;
		private PreparedStatement insertSongStatement;
		private PreparedStatement findHashesStatement;
		private PreparedStatement findSongStatement;
		private PreparedStatement getLastIdStatement;
		private PreparedStatement indexStatement;
		
		private final int timeResolution;
		private final ProgressNotifier.Builder progress;

		private HashOfSets<Integer, Pair<Integer, Integer>> cache;
		
		public DbHelper(Connection db, int timeResolution, ProgressNotifier.Builder progress) throws SQLException {
			this.db = db;
			this.timeResolution = timeResolution;
			this.progress = progress;
			this.cache = new HashOfSets<Integer, Pair<Integer, Integer>>();
			
			initDb();
			
			insertHashesStatement = db.prepareStatement(INSERT_HASHES_QUERY);
			insertSongStatement = db.prepareStatement(INSERT_SONG_QUERY);
			findHashesStatement = db.prepareStatement(FIND_MATCHING_HASHES_QUERY);
			findSongStatement = db.prepareStatement(FIND_SONG_QUERY);
			getLastIdStatement = db.prepareStatement(GET_LAST_ID_QUERY);
			indexStatement = db.prepareStatement(INDEX_DEFN);
		}
		
		public void initDb() throws SQLException {
			Statement smt = db.createStatement();
			smt.execute(SONG_HASHES_DEFN);
			smt.execute(SONGS_DEFN);
			smt.close();
		}
		
		public void createIndexes() throws SQLException {
			indexStatement.execute();
		}
		
		public void addSongToDb(SongMetaData song, StarHashSignature sig) throws SQLException {
			insertSongStatement.setString(1, song.getName());
			insertSongStatement.setString(2, "");
			insertSongStatement.setString(3, "");
			insertSongStatement.execute();
			int id = getLastId();
			
			Map<Integer, Set<Integer>> hashes = sig.getStarHashes();
			ProgressNotifier progressNotifier 
				= progress.create("Adding hash values to database...", hashes.keySet().size());
			int i = 1;

			for (Integer hash : hashes.keySet()) {
				for (Integer offset : hashes.get(hash)) {
					insertHashesStatement.setInt(1, hash);
					insertHashesStatement.setInt(2, offset);
					insertHashesStatement.setInt(3, id);
					insertHashesStatement.execute();
				}
				
				progressNotifier.update(i++);
			}
			
			progressNotifier.complete();
		}
		
		public void loadIntoMemory() throws SQLException {
			ResultSet counts = db.prepareStatement("SELECT COUNT(*) FROM song_hashes").executeQuery();
			counts.next();
			int count = counts.getInt(1);
			counts.close();
			
			ProgressNotifier notifier = progress.create("Loading hash offsets into memory...", count);
			
			this.cache = new HashOfSets<Integer, Pair<Integer, Integer>>((int)Math.floor(2*count));
			
			ResultSet hashes = db.prepareStatement("SELECT * FROM song_hashes").executeQuery();
			int i = 0;
			while (hashes.next()) {
				cache.addFor(hashes.getInt("hash_value"),
						Pair.create(hashes.getInt("song_id"), hashes.getInt("time_offset")));
				notifier.update(i++);
			}
			notifier.complete();
		}
		
		public Map<Integer, ReconstructedStarHashSignature> getOffsetsByHashes(StarHashSignature sig) throws SQLException {
			ReconstructingMap songs = new ReconstructingMap();
			
			ProgressNotifier notifier = progress.create("Querying DB...", sig.getStarHashes().keySet().size());
			int i = 1;
			
			for (Integer hash : sig.getStarHashes().keySet()) {
				songs.addHashes(hash, getMatchingHashes(hash));
				
				notifier.update(i++);
			}
			
			notifier.complete();
			
			return songs;
		}
		
		public SongMetaData getSongById(int id) throws SQLException {
			findSongStatement.setInt(1, id);
			ResultSet results = findSongStatement.executeQuery();
			results.next();
			
			return new SongMetaData(
				results.getString("name"),
				results.getString("name"),
				id);
		}
		
		public int getLastId() throws SQLException {
			ResultSet results = getLastIdStatement.executeQuery();
			results.next();
			return results.getInt(1);
		}
		
		protected Iterable<Pair<Integer, Integer>> getMatchingHashes(int hashValue) throws SQLException {
			if (cache.containsKey(hashValue)) {
				return cache.get(hashValue);
			}
			else {
				findHashesStatement.setInt(1, hashValue);
				return new HashResultIterator(findHashesStatement.executeQuery()); 
			}
		}
		
		protected static class HashResultIterator implements Iterable<Pair<Integer, Integer>>, Iterator<Pair<Integer,Integer>> {
			private final ResultSet results;

			public HashResultIterator(ResultSet results) {
				this.results = results;
				
			}

			@Override
			public boolean hasNext() {
				try {
					return results.next();
				}
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Pair<Integer, Integer> next() {
				try {
					return Pair.create(results.getInt("song_id"), results.getInt("time_offset"));
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<Pair<Integer, Integer>> iterator() {
				return this;
			}
			
		}
		
		protected static class ReconstructingMap extends DefaultingHashMap<Integer, ReconstructedStarHashSignature> {
			private static final long serialVersionUID = 8218976556676720258L;

			@Override
			protected ReconstructedStarHashSignature getDefaultValue() {
				return new ReconstructedStarHashSignature(new TreeMapOfSets<Integer, Integer>());
			}
			
			@Override
			public ReconstructedStarHashSignature get(Object key) {
				ReconstructedStarHashSignature value = super.get(key);
				
				if (value.getId() == 0) {
					value.setId((Integer)key);
				}
				
				return value;
			}
			
			protected void addHashes(int hash, Iterable<Pair<Integer, Integer>> vals) {
				for (Pair<Integer, Integer> val : vals) {
					addHash(val.getV1(), hash, val.getV2());
				}
			}
			
			protected void addHash(int song, int hash, int offset) {
				((TreeMapOfSets<Integer, Integer>)this.get(song).getStarHashes()).addFor(hash, offset);
			}
			
		}
	}
	
	private final Connection db;
	private DbHelper dbHelper;
	private final Settings settings;
	
	public HashSignatureDatabase(Connection db, Settings settings) throws SQLException {
		this(db, settings.getProgressNotifer(), settings);
	}

	public HashSignatureDatabase(Connection db, ProgressNotifier.Builder progress, Settings settings) throws SQLException {
		this.db = db;
		this.settings = settings;
		this.dbHelper = new DbHelper(db, settings.getTimeResolution(), progress);
	}
	
	public void createIndexes() throws SQLException {
		dbHelper.createIndexes();
	}
	
	public void loadIntoMemory() throws SQLException {
		dbHelper.loadIntoMemory();
	}

	@Override
	public void addSong(SongMetaData song, StarHashSignature sig) {
		try {
			dbHelper.addSongToDb(song, sig);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public QueryResponse<StarHashSignature> findSong(StarHashSignature signature) {
		try {
			Map<Integer, ReconstructedStarHashSignature> offsetsByHashes = dbHelper.getOffsetsByHashes(signature);
			QueryResponse<StarHashSignature> response = getResponse(settings.getStarHashComparator(), offsetsByHashes.values(), signature);
			ReconstructedStarHashSignature sig = (ReconstructedStarHashSignature) response.signature();
			
			if (sig != null) {
				response.setSong(dbHelper.getSongById(sig.getId()));
			}
			return response;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
