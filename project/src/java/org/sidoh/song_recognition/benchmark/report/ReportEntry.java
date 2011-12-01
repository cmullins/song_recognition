package org.sidoh.song_recognition.benchmark.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sidoh.collections.IndexingDictionary;
import org.sidoh.collections.Pair;
import org.sidoh.io.Serializer;
import org.sidoh.song_recognition.database.SignatureDatabase.QueryResponse;
import org.sidoh.song_recognition.database.SongMetaData;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class ReportEntry implements Serializable {
	public static final Serializer<ReportEntry> jsonSerializer = new Serializer<ReportEntry>() {
		@Override
		public void serialize(ReportEntry object, OutputStream out) throws IOException {
			// TODO Auto-generated method stub

			BufferedWriter b = new BufferedWriter(new OutputStreamWriter(out));

			b.append("{");
			b.append(String.format("\"clipName\" : \"%s\", ", object.getClipSongPart()));
			b.append(String.format("\"clipStart\" : %d,", object.getClipStart()));
			b.append(String.format("\"clipEnd\" : %d,", object.getClipEnd()));
			b.append(String.format("\"addedNoiseId\" : \"%s\",", object.getNoiseId()));
			b.append(String.format("\"matchFound\" : %d,", object.matchFound() ? 1 : 0));
			b.append(String.format("\"matchCorrect\" : %d,", object.matchCorrect ? 1 : 0));
			b.append(String.format("\"songId\" : \"%s\",", object.getSongId()));
			b.append(String.format("\"confidence\" : %.5f,", object.getConfidence()));
			b.append(String.format("\"hasSpectrogram\" : %d,", object.hasSpectrogram() ? 1 : 0));
			b.append(String.format("\"hasHistograms\" : %d", object.hasHistograms() ? 1 : 0));
			if (object.hasSpectrogram()) {
				b.append(String.format(",\"spectrogramPath\" : \"%s\"", object.getSpectrogramPath()));
			}
			if (object.hasHistograms()) {
				b.append(String.format(",\"histogramPaths\" : ["));
				
				Iterator<Entry<String, Pair<Double,String>>> itr = object.getHistograms().entrySet().iterator();
				while (itr.hasNext()) {
					Entry<String, Pair<Double,String>> entry = itr.next();
					b.append(String.format("{\"songId\" : \"%s\", \"score\" : %.5f, \"path\" : \"%s\"}",
						entry.getKey(),
						entry.getValue().getV1(),
						entry.getValue().getV2()));
					
					if (itr.hasNext()) {
						b.append(",");
					}
				}
				
				b.append("]");
			}
			b.append("}");
			
			b.flush();
		}
	};
	
	public static final Serializer<ReportEntry> txtSerializer = new Serializer<ReportEntry>() {
		@Override
		public void serialize(ReportEntry object, OutputStream out) throws IOException {
			BufferedWriter b = new BufferedWriter(new OutputStreamWriter(out));
			
			b.append(String.format("%d,%d,%d,%s,%.5f,%d",
				object.getSongIndex(),
				object.getClipIndex(),
				object.getClipLength(),
				object.getNoiseId(),
				object.getConfidence(),
				object.isCorrect() ? 1 : 0));
			
			b.flush();
		}
	};
	
	private static IndexingDictionary<String> songIndex = new IndexingDictionary<String>();
	
	private static final long serialVersionUID = 6147611654719791675L;
	
	protected SongMetaData song;
	protected QueryResponse<StarHashSignature> response;
	
	private static final Pattern clipFilePart 
		= Pattern.compile("^([a-z0-9_]+)-([0-9]+)-to-([0-9]+)(-added-noise-([a-z0-9_]+))?.wav$");
	private static final Pattern fullFilePart 
		= Pattern.compile("/?([^./]*).wav");
	private static final Pattern histogramName
		= Pattern.compile("^([a-z0-9_]+\\.wav)_([01]\\.[0-9]+)\\.txt\\.png$");
	
	public static final String NO_NOISE = "NONE";
	
	private final int clipStart;
	private final int clipEnd;
	private final String clipAddedNoiseId;
	private final boolean matchFound;
	private final boolean matchCorrect;

	private Map<String, Pair<Double,String>> histograms;
	private String spectrogramPath;
	
	public ReportEntry(SongMetaData song, QueryResponse<StarHashSignature> response) {
		this.song = song;
		this.response = response;
		
		Matcher clipMatcher = getClipMatcher();
		
		clipStart = Integer.parseInt(clipMatcher.group(2));
		clipEnd   = Integer.parseInt(clipMatcher.group(3));
		
		if (clipMatcher.group(5) != null && !clipMatcher.group(5).isEmpty()) {
			clipAddedNoiseId = clipMatcher.group(5);
		}
		else {
			clipAddedNoiseId = NO_NOISE;
		}
		
		if (response.song() != null) {
			Matcher songMatcher = getSongMatcher();
			
			matchFound   = true;
			matchCorrect = clipMatcher.group(1).equals(songMatcher.group(1));
		}
		else {
			matchFound = false;
			matchCorrect = false;
		}
	}

	public SongMetaData getClipMetaData() {
		return song;
	}

	public QueryResponse<StarHashSignature> getResponse() {
		return response;
	}
	
	public double getConfidence() {
		return response.confidence();
	}
	
	public String getSongId() {
		if (response.song() != null) {
			return new File(response.song().getFilename()).getName();
		}
		else {
			return null;
		}
	}
	
	public String getClipId() {
		return new File(song.getFilename()).getName();
	}
	
	public String getClipSongPart() {
		return getClipMatcher().group(1);
	}
	
	public boolean matchFound() {
		return matchFound;
	}
	
	public boolean isCorrect() {
		return matchCorrect;
	}
	
	public String getNoiseId() {
		return clipAddedNoiseId;
	}
	
	public int getClipStart() {
		return clipStart;
	}
	
	public int getClipEnd() {
		return clipEnd;
	}
	
	public int getClipLength() {
		return (clipEnd - clipStart);
	}
	
	public int getSongIndex() {
		return songIndex.offer(getSongId());
	}
	
	public int getClipIndex() {
		return songIndex.offer(getClipId());
	}
	
	public String toString() {
		return String.format("%d,%d,%d,%s,%.5f,%d",
			getSongIndex(),
			getClipIndex(),
			getClipLength(),
			getNoiseId(),
			getConfidence(),
			isCorrect() ? 1 : 0);
	}
	
	public ReportEntry setHistogram(String[] paths) {
		histograms = new HashMap<String, Pair<Double, String>>();
		
		for (String file : paths) {
			Matcher m = getHistMatcher(file);
			
			String songId = m.group(1);
			double score  = Double.parseDouble(m.group(2));
			
			histograms.put(songId, Pair.create(score, file));
		}
		
		return this;
	}
	
	public ReportEntry setSpectrogram(String path) {
		this.spectrogramPath = path;
		return this;
	}
	
	public boolean hasHistograms() {
		return histograms != null;
	}
	
	public boolean hasSpectrogram() {
		return spectrogramPath != null;
	}
	
	public Map<String, Pair<Double,String>> getHistograms() {
		return histograms;
	}
	
	public String getSpectrogramPath() {
		return spectrogramPath;
	}
	
	private Matcher getHistMatcher(String name) {
		Matcher m = histogramName.matcher(new File(name).getName());
		if (!m.matches()) {
			throw new RuntimeException("Invalid histogram name: " + name);
		}
		return m;
	}

	private Matcher getClipMatcher() {
		String baseFile = new File(getClipId()).getName();
		Matcher m = clipFilePart.matcher(baseFile);
		if (!m.matches()) {
			throw new RuntimeException("Invalid clip id: " + getClipId());
		}
		return m;
	}
	
	private Matcher getSongMatcher() {
		String baseFile = new File(getSongId()).getName();
		Matcher m = fullFilePart.matcher(baseFile);
		if (!m.matches()) {
			throw new RuntimeException("Invalid clip id: " + getSongId());
		}
		return m;
	}
}