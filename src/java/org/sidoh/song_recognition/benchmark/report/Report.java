package org.sidoh.song_recognition.benchmark.report;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sidoh.io.Serializer;
import org.sidoh.song_recognition.database.SignatureDatabase.QueryResponse;
import org.sidoh.song_recognition.database.SongMetaData;
import org.sidoh.song_recognition.signature.StarHashSignature;

public class Report {
	public static final Serializer<Report> jsonSerializer = new Serializer<Report>() {
		@Override
		public void serialize(Report object, OutputStream out) throws IOException {
			Serializer<ReportEntry> entrySerializer = ReportEntry.jsonSerializer;
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
			
			writer.append("{");
			writer.append(String.format("\"meanConfidence\" : %.5f,", object.meanConfidence()));
			writer.append(String.format("\"accuracy\" : %.5f,", object.accuracy()));
			writer.append(String.format("\"maxIncorrectConfidence\": %.5f,", object.maxIncorrectConfidence()));
			writer.append(String.format("\"lastUpdated\" : %d,", object.getLastUpdate()));
			writer.append(String.format("\"entries\":["));
			writer.flush();
			
			Iterator<ReportEntry> itr = object.getEntries().iterator();
			while (itr.hasNext()) {
				ReportEntry entry = itr.next();
				entrySerializer.serialize(entry, out);
				
				if (itr.hasNext()) {
					writer.append(",");
					writer.flush();
				}
			}
			
			writer.append("]");
			writer.append("}");
			
			writer.flush();
		}
	};
	
	public static final Serializer<Report> txtSerializer = new Serializer<Report>() {
		@Override
		public void serialize(Report object, OutputStream out) throws IOException {
			Serializer<ReportEntry> entrySerializer = ReportEntry.txtSerializer;
			OutputStreamWriter writer = new OutputStreamWriter(out);
			
			for (ReportEntry entry : object.getEntries()) {
				entrySerializer.serialize(entry, out);
				writer.append("\n");
				writer.flush();
			}
			
			writer.flush();
		}
	};
	
	private List<ReportEntry> results;
	private double confidenceSum;
	private int numCorrect;
	private double maxIncorrectConfidence;
	private long lastUpdate;
	
	public Report() {
		results = new ArrayList<ReportEntry>();
		
		confidenceSum = 0d;
		numCorrect = 0;
		maxIncorrectConfidence = -1;
		lastUpdate = 0;
	}
	
	public ReportEntry addResult(SongMetaData song, QueryResponse<StarHashSignature> result) {
		lastUpdate = System.currentTimeMillis();
		ReportEntry newEntry = new ReportEntry(song, result);
		results.add(newEntry);
		
		if (result.song() != null) {
			confidenceSum += result.confidence();
		}
		
		if (newEntry.isCorrect()) {
			numCorrect++;
		}
		else {
			maxIncorrectConfidence = Math.max(maxIncorrectConfidence, result.confidence());
		}
		
		return newEntry;
	}
	
	public long getLastUpdate() {
		return lastUpdate;
	}
	
	public Collection<ReportEntry> getEntries() {
		return results;
	}
	
	public double meanConfidence() {
		if (results.size() == 0) {
			return 0;
		}
		return confidenceSum / results.size();
	}
	
	public double accuracy() {
		if (results.size() == 0) {
			return 0;
		}
		return numCorrect / (double)results.size();
	}
	
	public double maxIncorrectConfidence() {
		return maxIncorrectConfidence;
	}
}