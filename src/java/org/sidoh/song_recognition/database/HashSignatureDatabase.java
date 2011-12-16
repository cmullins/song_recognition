package org.sidoh.song_recognition.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sidoh.collections.DefaultingHashMap;
import org.sidoh.collections.HashOfSets;
import org.sidoh.io.ProgressNotifier;
import org.sidoh.io.ProgressNotifier.Builder;
import org.sidoh.math.Histogram;
import org.sidoh.song_recognition.benchmark.Settings;
import org.sidoh.song_recognition.signature.HistogramScorer;
import org.sidoh.song_recognition.signature.LoggingScorer;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class HashSignatureDatabase extends SignatureDatabase<StarHashSignature> {
	private static final long serialVersionUID = 3430533360857830026L;

	protected static class DbHelper {
		public static final String SONG_HASHES_DEFN =
			"CREATE TABLE song_hashes (" +
				"HASH_VALUE INT, " +
				"TIME_OFFSET INT, " +
				"SONG_ID INT)";

		public static final String INDEX_DEFN =
			"CREATE INDEX song_hashes_hash_value ON song_hashes(hash_value)";
		
		public static final String SONGS_DEFN =
			"CREATE TABLE songs (" +
				"ID INT NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,"+
				"NAME varchar(255))";
		
		public static final String INSERT_HASHES_QUERY =
			"INSERT INTO song_hashes (hash_value, time_offset, song_id) VALUES (?,?,?)";
		
		public static final String INSERT_SONG_QUERY =
			"INSERT INTO songs (name) VALUES (?)";
		
		public static final String FIND_MATCHING_HASHES_QUERY =
			"SELECT * FROM song_hashes WHERE hash_value = ?";
		
		public static final String FIND_SONG_QUERY =
			"SELECT * FROM songs WHERE id = ?";
		
		private final Connection db;
		private PreparedStatement insertHashesStatement;
		private PreparedStatement insertSongStatement;
		private PreparedStatement findHashesStatement;
		private PreparedStatement findSongStatement;
		private PreparedStatement indexStatement;
		
		private final int timeResolution;
		private final ProgressNotifier.Builder progress;
		
		protected static class CacheEntry {
			int timeOffset;
			byte songId;
			
			public CacheEntry(int timeOffset, int songId) {
				this.timeOffset = timeOffset;
				this.songId     = (byte)songId;
			}
			
			public int getTimeOffset() {
				return timeOffset;
			}
			
			public int getSongId() {
				return songId;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + songId;
				result = prime * result + timeOffset;
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				CacheEntry other = (CacheEntry) obj;
				if (songId != other.songId)
					return false;
				if (timeOffset != other.timeOffset)
					return false;
				return true;
			}
		}

		private HashOfSets<Integer, CacheEntry> cache;
		private Map<Integer, String> songCache;
		private ArrayList<Histogram> histograms;

		private boolean inMemory = false;
		
		public DbHelper(Connection db, int timeResolution, ProgressNotifier.Builder progress) throws SQLException {
			this.db = db;
			this.timeResolution = timeResolution;
			this.progress = progress;
			this.cache = new HashOfSets<Integer, CacheEntry>();
			this.songCache = new HashMap<Integer, String>();
			this.histograms = new ArrayList<Histogram>();
			
			initDb();
			
			insertHashesStatement = db.prepareStatement(INSERT_HASHES_QUERY);
			insertSongStatement = db.prepareStatement(INSERT_SONG_QUERY, Statement.RETURN_GENERATED_KEYS);
			findHashesStatement = db.prepareStatement(FIND_MATCHING_HASHES_QUERY);
			findSongStatement = db.prepareStatement(FIND_SONG_QUERY);
			indexStatement = db.prepareStatement(INDEX_DEFN);
		}
		
		public void shutdown() {
			try {
				if (! db.isClosed()) {
					DriverManager.getConnection("jdbc:derby:;shutdown=true");
				}
				else {
					System.out.println("DB already closed!");
				}
			}
			catch (SQLException e) {
				// Expect this to happen 'cause Derby throws an SQL exception when it's shut down...
			}
		}
		
		public void initDb() throws SQLException {
			DatabaseMetaData metaData = db.getMetaData();
			Statement smt = db.createStatement();
			
			if (! metaData.getTables(null, null, "SONG_HASHES", null).next()) {
				smt.execute(SONG_HASHES_DEFN);
			}
			if (! metaData.getTables(null, null, "SONGS", null).next()) {
				smt.execute(SONGS_DEFN);
			}
			
			db.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			db.setAutoCommit(false);
			
			// Cache songs. It's very cheap and we can avoid any concurrency issues
			// by putting them in memory.
			ResultSet songs = db.prepareStatement("SELECT * FROM SONGS").executeQuery();
			
			while (songs.next()) {
				songCache.put(songs.getInt("ID"), songs.getString("NAME"));
			}
			
			for (int i = 0; i < songCache.keySet().size(); i++) {
				histograms.add(new Histogram(10000));
			}
			
			songs.close();
		}
		
		public void createIndexes() throws SQLException {
			indexStatement.execute();
			db.commit();
		}
		
		public synchronized void addSongToDb(SongMetaData song, StarHashSignature sig) throws SQLException {
			insertSongStatement.setString(1, song.getName());
			insertSongStatement.executeUpdate();
			ResultSet r = insertSongStatement.getGeneratedKeys();
			r.next();
			
			int id = r.getInt(1);
			
			Map<Integer, Set<Integer>> hashes = sig.getStarHashes();

			for (Integer hash : hashes.keySet()) {
				for (Integer offset : hashes.get(hash)) {
					insertHashesStatement.setInt(1, hash);
					insertHashesStatement.setInt(2, offset);
					insertHashesStatement.setInt(3, id);
					insertHashesStatement.execute();
				}
			}
			
			db.commit();
		}
		
		public void loadIntoMemory() throws SQLException {
			ResultSet counts = db.prepareStatement("SELECT COUNT(*) FROM song_hashes").executeQuery();
			counts.next();
			int count = counts.getInt(1);
			
			ProgressNotifier notifier = progress.create("Loading hash offsets into memory...", count);
			
			this.cache = new HashOfSets<Integer, CacheEntry>((int)Math.floor(2*count));
			
			ResultSet hashes = db.prepareStatement("SELECT * FROM song_hashes").executeQuery();
			int i = 0;
			while (hashes.next()) {
				// TODO: figure out what's going on here.
				if (hashes.getInt("SONG_ID") < 1) {
					System.err.println("Song ID for hash value is < 1 at row #" + i + " / " + count + ". This shouldn't happen: ("
						+ hashes.getInt(1) + ","+hashes.getInt(2) + ","+hashes.getInt(3) + ")");
				}
				else {
					cache.addFor(hashes.getInt("HASH_VALUE"),
							new CacheEntry(hashes.getInt("TIME_OFFSET"), hashes.getInt("SONG_ID")));
					notifier.update();
				}
				i++;
			}
			notifier.complete();
			
			this.inMemory = true;
			shutdown();
		}
		
		public synchronized List<Histogram> constructOffsetHistograms(StarHashSignature sig) throws SQLException {
			for (int songId : songCache.keySet()) {
				histograms.get(songId-1).reset();
			}
			
			for (Entry<Integer, Set<Integer>> entry : sig.getStarHashes().entrySet()) {
				Iterable<CacheEntry> matches = getMatchingHashes(entry.getKey());
				
				for (CacheEntry dbMatch : matches) {
					for (int offset : entry.getValue()) {
						histograms.get(dbMatch.getSongId()-1).addValue(dbMatch.getTimeOffset() - offset);
					}
				}
			}
			
			return histograms;
		}
		
		public SongMetaData getSongById(int id) throws SQLException {
			String name;
			if (songCache.containsKey(id)) {
				name = songCache.get(id);
			}
			else if (! db.isClosed()) {
				findSongStatement.setInt(1, id);
				ResultSet results = findSongStatement.executeQuery();
				
				if (! results.next()) {
					throw new RuntimeException("Coulding find song with ID = " + id + "!");
				}
				
				name = results.getString("NAME");
			}
			else {
				name = "UNKNOWN";
				System.err.println("WARNING: DB is closed and song with id `" + id + "' isn't cached!");
			}
			
			return new SongMetaData(name, name, id);
		}
		
		protected Iterable<CacheEntry> getMatchingHashes(int hashValue) throws SQLException {
			if (!db.isClosed() && db.getAutoCommit()) {
				db.setAutoCommit(false);
			}
			if (!db.isClosed() && !db.isReadOnly()) {
				db.setReadOnly(true);
			}
			
			if (cache.containsKey(hashValue)) {
				return cache.get(hashValue);
			}
			else if (! inMemory) {
				ResultSet rs = null;
				synchronized (this) {
					findHashesStatement.setInt(1, hashValue);
					rs = findHashesStatement.executeQuery();
					return new HashResultIterator(rs);
				}
			}
			else {
				return Collections.emptyList();
			}
		}
		
		protected static class HashResultIterator implements Iterable<CacheEntry> {
			private List<CacheEntry> items;

			public HashResultIterator(ResultSet results) throws SQLException {
				this.items = new LinkedList<CacheEntry>();
				
				while (results.next()) {
					items.add(new CacheEntry(
						results.getInt("TIME_OFFSET"),
						results.getInt("SONG_ID")));
				}
				
				results.close();
			}

			@Override
			public Iterator<CacheEntry> iterator() {
				return items.iterator();
			}
		}
	}
	
	private final Connection db;
	private DbHelper dbHelper;
	private final Settings settings;
	private final Builder progress;
	
	public HashSignatureDatabase(Connection db, Settings settings) throws SQLException {
		this(db, settings.getProgressNotifer(), settings);
	}

	public HashSignatureDatabase(Connection db, ProgressNotifier.Builder progress, Settings settings) throws SQLException {
		super(settings.getMaxNumThreads());
		this.db = db;
		this.progress = progress;
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
	protected void addSongInner(SongMetaData song, StarHashSignature sig) {
		try {
			dbHelper.addSongToDb(song, sig);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized QueryResponse<StarHashSignature> findSong(StarHashSignature signature) {
		try {
			List<Histogram> histograms = dbHelper.constructOffsetHistograms(signature);
			HistogramScorer scorer = settings.getHistogramScorer();
			
			int bestId = -1;
			double bestScore = Double.NEGATIVE_INFINITY;
			
			for (int i = 0; i < histograms.size(); i++) {
				double score = scorer.score(histograms.get(i));
				
				if (score > bestScore) {
					bestScore = score;
					bestId = i;
				}
			}
			
			if (bestId == -1) {
				return null;
			}
			
			return new QueryResponse<StarHashSignature>(bestScore,
				dbHelper.getSongById(bestId+1),
				signature);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void shutdown() {
		dbHelper.shutdown();
	}

	/**
	 * Does the same thing as {@link #findSong(StarHashSignature)}, but outputs the histograms used to
	 * generate the confidence values to the specified directory. The files will be named after the 
	 * names of the candidates (as in {@link SongMetaData#getName()}) and will include the score they
	 * were assigned by the histogram scorer in use.
	 * 
	 * @param signature
	 * @param histogramOutputDir
	 * @return
	 * @throws SQLException
	 */
	public synchronized QueryResponse<StarHashSignature> findSongWithDebugInfo(
		StarHashSignature signature,
		File histogramOutputDir,
		boolean quiet) throws SQLException {

		try {
			List<Histogram> histograms = dbHelper.constructOffsetHistograms(signature);
			
			int bestId = -1;
			double bestScore = Double.NEGATIVE_INFINITY;

			for (int i = 0; i < histograms.size(); i++) {
				String histogramFile = new File(histogramOutputDir, 
					dbHelper.getSongById(i+1).getName()).getAbsolutePath();
				HistogramScorer scorer = new LoggingScorer(histogramFile, settings.getHistogramScorer());
				
				double score = scorer.score(histograms.get(i));
				
				if (score > bestScore) {
					bestScore = score;
					bestId = i;
				}
			}
			
			if (bestId == -1) {
				return null;
			}
			
			return new QueryResponse<StarHashSignature>(bestScore,
				dbHelper.getSongById(bestId),
				signature);
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
