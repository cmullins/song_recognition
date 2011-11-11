package org.sidoh.song_recognition.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.database.H2Helper;
import org.sidoh.song_recognition.database.HashSignatureDatabase;
import org.sidoh.song_recognition.database.SignatureDatabase.QueryResponse;
import org.sidoh.song_recognition.signature.HistogramScorer;
import org.sidoh.song_recognition.signature.LoggingScorer;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram;
import org.sidoh.song_recognition.spectrogram.PgmSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;

public class BulkTest {
	private static final Pattern clipFilePart = Pattern.compile("/([^./-]*)[^.]+.wav");
	private static final Pattern fullFilePart = Pattern.compile("/([^.]*).wav");
	
	private static final ExecutorService fileWorkers = Executors.newFixedThreadPool(2);
	
	private static String histogramFilename(QueryResponse<StarHashSignature> response) {
		return String.valueOf(response.confidence());
	}
	
	private static String getSpectrogramLink(String clipFile) {
		return clipFile + ".png";
	}
	
	private static void printHeader(PrintStream out) {
		out.println("<table>");
	}
	
	private static void printFooter(PrintStream out) {
		out.println("</table>");
	}
	
	private static class SpectrogramWritingWorker implements Runnable {
		
		private final PgmSpectrogramConstellationWriter writer;
		private final Spectrogram spec;
		private final String file;

		public SpectrogramWritingWorker(PgmSpectrogramConstellationWriter writer, Spectrogram spec, String file) {
			this.writer = writer;
			this.spec = spec;
			this.file = file;
			
		}

		@Override
		public void run() {
			try {
				File f = new File(file);
				f.getParentFile().mkdirs();
				
				String pgm = new File(file).getAbsolutePath() + ".pgm";
				String png = new File(file).getAbsolutePath() + ".png";
				
				OutputStream img = new FileOutputStream(pgm);
				writer.write(img, new ConfigurableSpectrogram(spec).setContrast(300));
				img.close();
				
				Runtime.getRuntime().exec(new String[]{
							"convert",
							pgm,
							png
						});
				new File(pgm).deleteOnExit();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private static class HistogramWritingWorker implements Runnable {
		
		private final String directory;

		public HistogramWritingWorker(String directory) {
			this.directory = directory;
			
		}

		@Override
		public void run() {
			try {
				File file = new File(directory);
				
				for (File f : file.listFiles()) {
					if (f.getName().matches("[0-9]+[.][0-9]+")) {
						String fn = f.getAbsolutePath();
						String out = fn + ".png";
						String script = String.format(
								"png(filename=\"%s\",width=1000,height=400);d<-read.table(\"%s\");hist(d[,1],breaks=400);dev.off();",
								out, fn);
						
						Process exec = Runtime.getRuntime().exec("R --vanilla");
						BufferedWriter write = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));
						write.write(script);
						write.close();
					}
				}
			}
			catch (Exception e) { throw new RuntimeException(e); }
		}
		
	}
	
	private static void printResult(PrintStream out, String clipFile, QueryResponse<StarHashSignature> response, int num) {
		boolean matchFound = false;
		boolean matchCorrect = false;
		
		if (response.song() != null) {
			matchFound = true;
			
			Matcher matcher = clipFilePart.matcher(clipFile);
			if (!matcher.find()) { 
				throw new RuntimeException("should match (clip): " + clipFile);
			}
			Matcher fullMatcher = fullFilePart.matcher(response.song().getFilename());
			if (!fullMatcher.find()) {
				throw new RuntimeException("should match (clip): " + response.song().getFilename());
			}
			
			matchCorrect = matcher.group(1).equals(fullMatcher.group(1));
		}
		
		out.println("<tr>");
		out.println("<td>");
		if (!matchFound) {
			out.println("<span style='color: #cccc00; font-weight: bold;'>NO MATCH</span>");
		}
		else if (!matchCorrect) {
			out.printf("<span style='color: #cc0000; font-weight: bold;'>%s</span>\n", response.song().getName());
		}
		else {
			out.printf("<span style='color: #00cc00; font-weight: bold;'>%s</span>\n", response.song().getName());
		}
		out.println("</td>");
		out.printf("<td>%s</td>", clipFile);
		
		if (matchFound) {
			out.printf("<td><a href='%d/%s.png'>%f</a></td>", num, String.valueOf(response.confidence()), response.confidence());
		}
		else {
			out.println("<td>-</td>");
			out.println("<td>-</td>");
		}
		
		out.printf("<td><a href='%d/spectrogram.png'>spectrogram</a></td>", num);
		out.println();
		out.println("</tr>");
	}
	
	public static void main(String[] args) throws SQLException, IOException, WavFileException, InterruptedException {
		if (args.length < 2) {
			System.err.println("Syntax is: BulkTest <db_name> <wavfile1> <wavfile2> ... <wavfileN>");
			System.exit(1);
		}
		
		if (! new File(args[0] + ".h2.db").exists()) {
			System.err.println("Dbfile " + args[0] + ".h2.db doesn't exist!");
			System.exit(1);
		}
		
		String reportDir = "report";
		
		PrintStream out = new PrintStream(new FileOutputStream(reportDir + "/index.html"));
		printHeader(out);
		
		Settings settings = Settings.defaults();
		settings
			.setProgressNotifer(ProgressNotifier.nullNotifier()) //ssh
			;
		ProgressNotifier notifier = ProgressNotifier.consoleNotifier(100)
			.create("Generating report...", args.length-1);
		
		HashSignatureDatabase db = new HashSignatureDatabase(
				H2Helper.getConnection(args[0]),
				settings);
		db.loadIntoMemory();
		
		StarHashExtractor extractor = settings.getStarHashExtractor();
		Spectrogram.Builder specBuilder = settings.getSpectrogramBuilder();
		FrameBuffer.Builder bufferBuilder = settings.getBufferBuilder();
		PgmSpectrogramConstellationWriter writer = new PgmSpectrogramConstellationWriter(
				settings.getConstellationExtractor().quiet(), ProgressNotifier.nullNotifier());
		HistogramScorer baseScorer = settings.getHistogramScorer();
		
		for (int i = 1; i < args.length; i++) {
			settings.setHistogramScorer(
				new LoggingScorer(reportDir + "/" + i, baseScorer));
			
			Spectrogram spec = specBuilder.create(bufferBuilder.fromWavFile(args[i]));
			StarHashSignature sig = extractor.extractSignature(spec);
			QueryResponse<StarHashSignature> response = db.findSong(sig);
			
			String songName = (response.song() == null ? "UNKNOWN" : response.song().getFilename());
			
			printResult(out, args[i], response, i);
			fileWorkers.execute(new SpectrogramWritingWorker(writer, spec, String.format("%s/%d/spectrogram", reportDir, i)));
			fileWorkers.execute(new HistogramWritingWorker(String.format("%s/%d", reportDir, i)));
			
			notifier.update(i);
		}
		
		notifier.complete();
		printFooter(out);
		
		fileWorkers.shutdown();
		System.out.println("Waiting for file workers to complete...");
	}
}
