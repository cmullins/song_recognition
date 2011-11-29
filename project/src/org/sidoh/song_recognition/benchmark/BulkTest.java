package org.sidoh.song_recognition.benchmark;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sidoh.collections.IndexingDictionary;
import org.sidoh.io.ChanneledProgressNotifier;
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
	private static enum _ReportType {
		REPORT_ONLY, VERBOSE_INCORRECT, VERBOSE_ALL
	}
	private static final _ReportType reportType = _ReportType.VERBOSE_INCORRECT;

	private static final Pattern clipFilePart = Pattern.compile("^([a-z0-9_]+)-([0-9]+)-to-([0-9]+)(-added-noise-([a-z0-9_]+))?.wav$");
	private static final Pattern fullFilePart = Pattern.compile("/?([^./]*).wav");
	
	private static final ExecutorService fileWorkers = Executors.newFixedThreadPool(1);
	
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
		private final ProgressNotifier notifier;

		public SpectrogramWritingWorker(PgmSpectrogramConstellationWriter writer, Spectrogram spec, String file, ProgressNotifier notifier) {
			this.writer = writer;
			this.spec = spec;
			this.file = file;
			this.notifier = notifier;
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
				notifier.update();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	private static class HistogramWritingWorker implements Runnable {
		
		private final String directory;
		private final ProgressNotifier notifier;

		public HistogramWritingWorker(String directory, ProgressNotifier notifier) {
			this.directory = directory;
			this.notifier = notifier;
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
								"png(filename=\"%s\",width=1500,height=400);d<-read.table(\"%s\");barplot(d[,2]);dev.off();",
								out, fn);
						
						Process exec = Runtime.getRuntime().exec("R --vanilla");
						BufferedWriter write = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));
						write.write(script);
						write.close();
					}
				}
				
				notifier.update();
			}
			catch (Exception e) { throw new RuntimeException(e); }
		}
		
	}
	
	private static final IndexingDictionary<String> songIds = new IndexingDictionary<String>();
	
	private static void printRawResult(PrintStream out, String clipFile, QueryResponse<StarHashSignature> response) {
		String baseFile = new File(clipFile).getName();
		Matcher clipMatcher = clipFilePart.matcher(baseFile);
		if (!clipMatcher.matches()) {
			throw new RuntimeException("filename should match: " + baseFile);
		}
		
		String songName = clipMatcher.group(1);
		int clipStart   = Integer.parseInt(clipMatcher.group(2));
		int clipEnd     = Integer.parseInt(clipMatcher.group(3));
		String noise    = "NONE";
		
		if (clipMatcher.group(5) != null && !clipMatcher.group(5).isEmpty()) {
			noise = clipMatcher.group(5);
		}
		
		boolean matchFound = false;
		boolean matchCorrect = false;
		int matchId = -1;
		int clipSongId = songIds.offer(songName);
		
		if (response.song() != null) {
			matchFound = true;
			
			Matcher fullMatcher = fullFilePart.matcher(response.song().getName());
			if (!fullMatcher.find()) {
				throw new RuntimeException("should match (full): " + response.song().getName());
			}
			
			matchCorrect = clipMatcher.group(1).equals(fullMatcher.group(1));
			
			matchId = songIds.offer(fullMatcher.group(1));
		}
		
		out.printf("%d,%d,%d,%s,%.5f,%d\n",
			clipSongId,
			matchId,
			(clipEnd - clipStart),
			noise,
			response.confidence(),
			(matchFound && matchCorrect ? 1 : 0));
	}
	
	private static boolean isCorrect(String clipFile, QueryResponse<StarHashSignature> response) {
		boolean matchFound = false;
		boolean matchCorrect = false;
		
		if (response.song() != null) {
			matchFound = true;
			
			String baseFile = new File(clipFile).getName();
			
			Matcher matcher = clipFilePart.matcher(baseFile);
			if (!matcher.find()) { 
				throw new RuntimeException("should match (clip): " + baseFile);
			}
			Matcher fullMatcher = fullFilePart.matcher(response.song().getName());
			if (!fullMatcher.find()) {
				throw new RuntimeException("should match (full): " + response.song().getName());
			}
			
			matchCorrect = matcher.group(1).equals(fullMatcher.group(1));
			clipFile = baseFile;
		}
		
		return matchFound && matchCorrect;
	}
	
	private static void printResult(PrintStream out, String clipFile, QueryResponse<StarHashSignature> response, int num) {
		boolean matchFound = false;
		boolean matchCorrect = false;
		
		if (response.song() != null) {
			matchFound = true;
			
			String baseFile = new File(clipFile).getName();
			
			Matcher matcher = clipFilePart.matcher(baseFile);
			if (!matcher.find()) { 
				throw new RuntimeException("should match (clip): " + baseFile);
			}
			Matcher fullMatcher = fullFilePart.matcher(response.song().getName());
			if (!fullMatcher.find()) {
				throw new RuntimeException("should match (full): " + response.song().getName());
			}
			
			matchCorrect = matcher.group(1).equals(fullMatcher.group(1));
			clipFile = baseFile;
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
		
		out.printf("<td><a href='%d/%s.png'>%f</a></td>", num, String.valueOf(response.confidence()), response.confidence());
		
		out.printf("<td><a href='%d/spectrogram.png'>spectrogram</a></td>", num);
		out.println();
		out.println("</tr>");
	}
	
	public static void main(String[] args) throws SQLException, IOException, WavFileException, InterruptedException {
		if (args.length < 3) {
			System.err.println("Syntax is: BulkTest <db_name> <report_dir> ([<wavs_dir>], [<wavfile1> [wavfile2] ... [wavfileN]])");
			System.exit(1);
		}
		
		if (! new File(args[0] + ".h2.db").exists()) {
			System.err.println("Dbfile " + args[0] + ".h2.db doesn't exist!");
			System.exit(1);
		}
		
		File reportDir = new File(args[1]);
		if (! reportDir.exists()) {
			reportDir.mkdirs();
		}
		
		PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(reportDir, "index.html").getAbsolutePath())));
		PrintStream rawOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(new File(reportDir, "raw.txt").getAbsolutePath())));
		
		printHeader(out);
		
		ProgressNotifier.Builder noisyNotifier = ProgressNotifier
			.consoleNotifier(50)
			.channeled("noisy_updates")
			.controlledByStdin();
		
		Settings settings = Settings.defaults();
		settings
			.setProgressNotifer(noisyNotifier)
//			.setSpectrogramBuilder(Spectrogram.inMemory())       //need separate memory for each spectrogram
			;
		
		ProgressNotifier workerNotifier = 
			ProgressNotifier
				.consoleNotifier(100)
				.channeled("worker_tasks")
			.create("Waiting for workers to finish...", 2*(args.length - 1));
		
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
		
		String[] files;
		
		File dirTest = new File(args[2]);
		if (dirTest.isDirectory()) {
			File[] children = dirTest.listFiles();
			List<String> _files = new ArrayList<String>();
			
			for (File child : children) {
				if (clipFilePart.matcher(child.getName()).matches()) {
					_files.add(child.getAbsolutePath());
				}
			}
			
			files = _files.toArray(new String[_files.size()]);
		}
		else {
			files = Arrays.copyOfRange(args, 2, args.length-1);
		}
		
		ProgressNotifier notifier = ProgressNotifier.consoleNotifier(100)
				.create("Generating report...", files.length-1);
		
		for (int i = 0; i < files.length; i++) {
//			settings.setHistogramScorer(
//				new LoggingScorer(reportDir + "/" + i, baseScorer));
			System.out.println(files[i]);
			
			Spectrogram spec = specBuilder.create(bufferBuilder.fromWavFile(files[i]));
			StarHashSignature sig = extractor.extractSignature(spec);
			QueryResponse<StarHashSignature> response = db.findSong(sig);
			
			String songName = (response.song() == null ? "UNKNOWN" : response.song().getName());
			
			printResult(out, files[i], response, i);
			printRawResult(rawOut, files[i], response);
			
			if (reportType == _ReportType.VERBOSE_ALL || (!isCorrect(files[i], response) && reportType == _ReportType.VERBOSE_INCORRECT)) {
//				fileWorkers.execute(new SpectrogramWritingWorker(writer, spec, String.format("%s/%d/spectrogram", reportDir, i), workerNotifier));
//				fileWorkers.execute(new HistogramWritingWorker(String.format("%s/%d", reportDir, i), workerNotifier));
			}
			
			notifier.update(i);
			out.flush();
			rawOut.flush();
		}
		
		notifier.complete();
		printFooter(out);
		
		fileWorkers.shutdown();
		System.out.println("Waiting for file workers to complete...");
		ChanneledProgressNotifier.enableChannel("worker_tasks");
	}
}
