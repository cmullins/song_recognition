package org.sidoh.song_recognition.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.sidoh.io.ChanneledProgressNotifier;
import org.sidoh.io.IOHelper;
import org.sidoh.io.ProgressNotifier;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.benchmark.report.Report;
import org.sidoh.song_recognition.benchmark.report.ReportEntry;
import org.sidoh.song_recognition.benchmark.report.ReportServer;
import org.sidoh.song_recognition.database.HashSignatureDatabase;
import org.sidoh.song_recognition.database.RdbmsHelper;
import org.sidoh.song_recognition.database.SignatureDatabase.QueryResponse;
import org.sidoh.song_recognition.database.SongMetaData;
import org.sidoh.song_recognition.signature.StarHashExtractor;
import org.sidoh.song_recognition.signature.StarHashSignature;
import org.sidoh.song_recognition.spectrogram.ConfigurableSpectrogram;
import org.sidoh.song_recognition.spectrogram.PngSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;
import org.sidoh.song_recognition.spectrogram.SpectrogramWriter;

public class BulkTest {
	public static enum VerbosityLevel {
		FULL_VERBOSE, INCORRECT_VERBOSE, REPORT_ONLY;
	}
	
	public static class TestOptions {
		private File dbFile;
		private final File reportPath;
		private final File[] wavFiles;
		private final VerbosityLevel reportVerbosity;
		private final boolean inMemory;
		private final boolean verboseProgress;
		private final boolean forceOverwrite;
		private final boolean httpEnabled;
		private final int httpPort;
		
		public TestOptions(CommandLine cmd) throws IOException {
			if (! ClOptions.DATABASE.present(cmd)) {
				throw new IllegalArgumentException("Required argument --database is missing");
			}
			if (! ClOptions.REPORT_PATH.present(cmd)) {
				throw new IllegalArgumentException("Required argument --report-path is missing");
			}
			if (! ClOptions.WAVS_DIR.present(cmd) && ! ClOptions.WAV_FILES.present(cmd)) {
				throw new IllegalArgumentException("Need to specify one of --wavs-dir // --wav-files");
			}
			
			inMemory = ClOptions.IN_MEMORY.present(cmd);
			verboseProgress = ClOptions.VERBOSE.present(cmd);
			forceOverwrite = ClOptions.FORCE_OVERWRITE.present(cmd);
			httpEnabled = ClOptions.HTTP_SERVER.present(cmd);
			httpPort = Integer.parseInt(ClOptions.HTTP_PORT.value(cmd, "8000"));
			
			dbFile = new File(ClOptions.DATABASE.value(cmd));
			
			if (!dbFile.exists()) {
				// Check for files beginning with the prefix
				boolean found = false;
				for (File f : dbFile.getParentFile().listFiles()) {
					if (f.getName().startsWith(dbFile.getName())) {
						found = true;
					}
				}
				
				if (!found) {
					throw new IOException("Specified database file: " + ClOptions.DATABASE.value(cmd) + " doesn't exist!");
				}
			}
			
			reportPath = new File(ClOptions.REPORT_PATH.value(cmd));
			
			// Create report path
			if (reportPath.exists() && !forceOverwrite) {
				throw new IllegalArgumentException("Report path already exists!");
			}
			else if (reportPath.exists() && forceOverwrite) {
				IOHelper.deleteDir(reportPath);
			}
			
			if (!reportPath.mkdirs()) {
				throw new IOException("Couldn't create report directory '" + reportPath + "'");
			}
			
			if (! ClOptions.REPORT_VERBOSITY.present(cmd)) {
				reportVerbosity = VerbosityLevel.REPORT_ONLY;
			}
			else {
				int iVerb;
				try {
					iVerb = Integer.parseInt(ClOptions.REPORT_VERBOSITY.value(cmd));
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid verbosity level");
				}
				
				switch (iVerb) {
				case 0:
					reportVerbosity = VerbosityLevel.REPORT_ONLY;
					break;
				case 1:
					reportVerbosity = VerbosityLevel.INCORRECT_VERBOSE;
					break;
				case 2:
					reportVerbosity = VerbosityLevel.FULL_VERBOSE;
					break;
				default:
					throw new IllegalArgumentException("Invalid verbosity level");
				}
			}
			
			if (ClOptions.WAVS_DIR.present(cmd)) {
				File wavDir = new File(ClOptions.WAVS_DIR.value(cmd));
				
				if (!wavDir.exists() || !wavDir.isDirectory()) {
					throw new IllegalArgumentException("Wav directory invalid. Either doesn't exist or isn't a directory.");
				}
				
				List<File> wavFiles = new LinkedList<File>();
				for (File child : wavDir.listFiles()) {
					wavFiles.add(child);
				}
				
				this.wavFiles = wavFiles.toArray(new File[wavFiles.size()]);
			}
			else {
				String[] fileNames = ClOptions.WAV_FILES.multiValues();
				List<File> files = new ArrayList<File>(fileNames.length);
				
				for (String fileName : fileNames) {
					files.add(new File(fileName));
				}
				
				this.wavFiles = files.toArray(new File[files.size()]);
			}
		}
		
		public boolean httpEnabled() {
			return httpEnabled;
		}
		
		public int httpPort() {
			return httpPort;
		}
		
		public boolean inMemory() {
			return inMemory;
		}
		
		public File getDbFile() {
			return dbFile;
		}

		public File getReportPath() {
			return reportPath;
		}

		public File[] getWavFiles() {
			return wavFiles;
		}

		public VerbosityLevel getVerbosity() {
			return reportVerbosity;
		}
		
		public boolean verboseProgress() {
			return verboseProgress;
		}
		
		public boolean forceOverwrite() {
			return forceOverwrite;
		}

		public static TestOptions getOptions(String[] cmdLineArgs) throws IOException, ParseException {
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(ClOptions.allOptions(), cmdLineArgs);
			
			return new TestOptions(cmd);
		}
	}
	
	@SuppressWarnings("static-access") // what a crappy builder. apache-cli disappoints.
	public static enum ClOptions {
		DATABASE(new Option("d", "database", true, "Derby database file")),
		WAVS_DIR(new Option("vd", "wavs-dir", true, "Path to directory containing WAV test files (use instead of -vf)")),
		WAV_FILES(OptionBuilder.hasArg()
			.withArgName("vf")
			.withLongOpt("wav-files")
			.withDescription("List of WAV files, separated by commas (use instead of -vd)")
			.withValueSeparator(',')
			.create()),
		REPORT_PATH(new Option("r", "report-path", true, "Path to use for report")),
		REPORT_VERBOSITY(new Option("rv", "report-verbosity", true, "Report verbosity level: 0 - report only, 1 - verbose on incorrect, 2 - full verbose")),
		FORCE_OVERWRITE(new Option("f", "force-overwrite", false, "Force overwriting of files/dirs where appropriate")),
		VERBOSE(new Option("v", "verbose", false, "Verbose. If set, print out lots of progress notifications")),
		IN_MEMORY(new Option("m", "in-memory", false, "If set, load hash value database into memory")),
		HTTP_SERVER(new Option("h", "enable-http", false, "If set, serve stats on an HTTP server. If not set, save stats to files.")),
		HTTP_PORT(new Option("p", "http-port", true, "Port to run HTTP server on. Default = 8000"));
		
		private final Option option;

		private ClOptions(Option option) {
			this.option = option;
		}
		
		public Option option() {
			return option;
		}
		
		public String value(CommandLine cmd, String defaultValue) {
			if (! present(cmd)) {
				return defaultValue;
			}
			else { 
				return value(cmd);
			}
		}
		
		public String value(CommandLine cmd) {
			return cmd.getOptionValue(option.getOpt());
		}
		
		public String[] multiValues() {
			return option.getValues();
		}
		
		public boolean present(CommandLine cmd) {
			return cmd.hasOption(option.getOpt());
		}
		
		public static Options allOptions() {
			Options ops = new Options();
			
			ClOptions[] cos = ClOptions.values();
			for (int i = 0; i < cos.length; i++) {
				ops.addOption(cos[i].option());
			}
			
			return ops;
		}
	}
	
	public static void printHelpAndDie(Exception e) {
		System.err.println(e.getMessage());
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("BulkTest", ClOptions.allOptions());
		
		System.exit(1);
		return;
	}
	
	public static void main(String[] args) throws IOException, ParseException, SQLException, WavFileException {
		final TestOptions options;
		try {
			options = TestOptions.getOptions(args);
		}
		catch (IllegalArgumentException e) {
			printHelpAndDie(e);
			return;
		}
		catch (IOException e) {
			printHelpAndDie(e);
			return;
		}
		
		Settings settings = Settings.defaults();
		
		// Create new report and set up the directory it's allowed to put its stuff in.
		// Start HTTP server if it's enabled.
		final Report report = new Report();
		ReportServer httpServer = null;
		if (options.httpEnabled()) {
			if (options.verboseProgress()) {
				System.out.println("Starting HTTP server on port " + options.httpPort());
			}
			httpServer = new ReportServer(report, options.getReportPath(), options.httpPort());
			
			// Force a report update on shutdown
			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println("Persisting report data...");
					updateReport(report, options, true);
				}
			}));
		}
		
		// Disable verbose updates if the verbose flag wasn't set.
		if (!options.verboseProgress()) {
			settings.setProgressNotifer(ProgressNotifier.nullNotifier());
		}
		// Otherwise, use a channeled notifier that can be toggled with stdin.
		else {
			ProgressNotifier.Builder d = settings.getProgressNotifer();
			
			settings.setProgressNotifer(d.channeled("noisy").controlledByStdin());
			ChanneledProgressNotifier.enableChannel("noisy");
		}
		
		if (options.verboseProgress()) {
			System.out.println("Loading signatures...");
		}
		
		// Set up the database
		HashSignatureDatabase db = new HashSignatureDatabase(
			RdbmsHelper.getConnection(options.getDbFile().getAbsolutePath()),
			settings.getProgressNotifer(),
			settings);	
		// Load into memory if we're told to
		if (options.inMemory()) {
			db.loadIntoMemory();
		}
		
		// Create a progress notifier for us to use to keep user updated on how many files have
		// been processed.
		ProgressNotifier fileProgress = ProgressNotifier.nullNotifier().create(null,0);
		
		for (File clip : options.getWavFiles()) {
			handleSingleClip(clip, options, settings, db, report);
			updateReport(report, options, false);
			
			fileProgress.update();
		}
		
		fileProgress.complete();
		
		if (options.verboseProgress()) {
			System.out.println("Shutting down worker threads");
			verboseWorkers.shutdown();
		}
		
		// Free some memory
		db = null;
		System.gc();
		
		// If HTTP is enabled, the application will stay open so that it can keep
		// serving the UI. We should probably tell the user about this, though...
		if (options.httpEnabled()) {
			System.out.println("Everything is finished. Keeping the HTTP server running. "
				+ "Type \"QUIT\" (Or press Ctrl-C) to quit the application.");

			BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
			while (! inReader.readLine().toLowerCase().equals("quit"));
			
			// Close HTTP server. This should cause the application to exit.
			httpServer.stop();
		}
	}
	
	private static final Semaphore reportUpdateLock = new Semaphore(1);
	private static void updateReport(final Report report, final TestOptions options, final boolean forceUpdate) {
		
		// If we're being forced to update, wait for a permit before continuing. Don't have
		// to worry about a race condition here because there's only ever one thread wanting
		// to update the files.
		if (forceUpdate) {
			try {
				reportUpdateLock.acquire();
			}
			catch (InterruptedException e) { }
			finally {
				reportUpdateLock.release();
			}
		}
		
		if (forceUpdate || reportUpdateLock.tryAcquire()) {
			final File jsonFile = new File(options.getReportPath(), "report_data.json");
			final File txtFile = new File(options.getReportPath(), "raw.txt");
			
			Runnable task = new Runnable() {
				@Override
				public void run() {
					try {
						// Don't update JSON if HTTP is enabled. It'd be kinda pointless.
						if (! options.httpEnabled() || forceUpdate) {
							File tmpJson = File.createTempFile("report_data.json", "tmp");
							OutputStream jsonOut = new FileOutputStream(tmpJson);
							Report.jsonSerializer.serialize(report, jsonOut);
							jsonOut.close();
							File tmpJson2 = new File(jsonFile.getParentFile(), "report_data.json.tmp");
							FileUtils.copyFile(tmpJson, tmpJson2);
							jsonFile.delete();
							FileUtils.moveFile(tmpJson2, jsonFile);
						}
						
						File tmpTxt  = File.createTempFile("raw.txt", "txt");
						OutputStream txtOut = new FileOutputStream(tmpTxt);
						Report.txtSerializer.serialize(report, txtOut);
						txtOut.close();

						// Move the temporary files to the report directory before renaming them.
						File tmpTxt2  = new File(txtFile.getParentFile(), "raw.txt.tmp");
						FileUtils.copyFile(tmpTxt, tmpTxt2);
						
						// Rename 'em.
						txtFile.delete();
						FileUtils.moveFile(tmpTxt2, txtFile);
					}
					catch (IOException e) {
						throw new RuntimeException(e);
					}
					finally {
						reportUpdateLock.release();
					}
				}
			};
			
			if (forceUpdate) {
				task.run();
			}
			else {
				new Thread(task).start();
			}
		}
	}

	private static int clipId = 0;
	private static void handleSingleClip(File clip, 
			TestOptions clOptions, 
			Settings settings, 
			HashSignatureDatabase db,
			Report report) throws IOException, WavFileException {
		
		// Extract some stuff from settings for convenience
		StarHashExtractor extractor = settings.getStarHashExtractor();
		Spectrogram.Builder specBuilder = settings.getSpectrogramBuilder();
		FrameBuffer.Builder bufferBuilder = settings.getBufferBuilder();
		
		// Extract signature, query database
		Spectrogram spec      = specBuilder.create(bufferBuilder.fromWavFile(clip.getAbsolutePath()));
		StarHashSignature sig = extractor.extractSignature(spec);
		QueryResponse<StarHashSignature> response = db.findSong(sig);
		
		// Add new report entry, do some verbose stuff if it's enabled.
		ReportEntry entry = report.addResult(new SongMetaData(clip.getAbsolutePath(), clip.getName(), clipId++), response);
		
		if (clOptions.getVerbosity() == VerbosityLevel.FULL_VERBOSE
			|| (clOptions.getVerbosity() == VerbosityLevel.INCORRECT_VERBOSE && !entry.isCorrect())) {
			outputVerboseInfo(entry, sig, db, clOptions, settings);
		}
	}

	// Thread pool for worker threads
	private static ExecutorService verboseWorkers = Executors.newFixedThreadPool(2);
	
	// Need a separate builder so it has its own memory buffer that it can use. Otherwise we'll
	// get all sorts of weirdness and almost definitely nothing close to what we want.
	private static Spectrogram.Builder workerSpecBuilder = Spectrogram.inMemory().progressNotifier(ProgressNotifier.nullNotifier());
	
	// Keep a copy of this around so we don't have to create it a bunch of times.
	private static SpectrogramWriter spectrogramWriter = null;
	
	// This R script will get run on a histogram file output by LoggingScorer. Should be called
	// with String.format to fill in the parameters.
	private static final String rScript 
		= "png(filename=\"%s.png\",width=1500,height=400);"
			+ "d<-read.table(\"%s\");"
			+ "barplot(d[,2]);"
			+ "dev.off();";
	
	// You can use this script to generate postscript plots instead of PNGs.
	//	private static final String rScript 
	//		= "postscript(file=\"%s.eps\",width=15,height=4,horizontal=FALSE,paper=\"special\");"
	//			+ "d<-read.table(\"%s\");"
	//			+ "r<-NULL;"
	//			+ "for (i in 1:nrow(d)) { for (j in 1:d[i,2]) { r <- rbind(r, d[i,1]); }};"
	//			+ "hist(r,breaks=sort(unique(r)), xlab=\"Time delta\");"
	//			+ "dev.off();";
	
	private static void outputVerboseInfo(
			final ReportEntry entry, 
			final StarHashSignature sig,
			final HashSignatureDatabase db, 
			final TestOptions clOptions,
			final Settings settings) {
		
		if (spectrogramWriter == null) {
			spectrogramWriter = new PngSpectrogramConstellationWriter(
					settings.getConstellationExtractor().quiet(),
					ProgressNotifier.nullNotifier());
		}
		
		verboseWorkers.execute(new Runnable() {
			@Override
			public void run() {
				try {
					SongMetaData clip = entry.getClipMetaData();
					
					// Need to re-create spectrogram. This probably isn't necessary, but since clips are
					// typically very short, this isn't such a terrible shortcut
					Spectrogram spec = 
						workerSpecBuilder
						.copy()
						.progressNotifier(ProgressNotifier.nullNotifier())
						.create(settings.getBufferBuilder().fromWavFile(clip.getFilename()));
					// Spectrograms typically need a little help being very visible.
					spec = new ConfigurableSpectrogram(spec).setContrast(300); 
					
					// Create directory to put stuff in.
					File outputDir = new File(
							clOptions.getReportPath(), // base report dir
							String.valueOf(clip.getId())); // subdirectory for extra junk.
					outputDir.mkdir();
					
					// Write PGM spectrogram, convert it to PNG.
					String pngFile = new File(outputDir, String.format("000_%s.png", new File(clip.getFilename()).getName())).getAbsolutePath();
					
					OutputStream img = new FileOutputStream(pngFile);
					spectrogramWriter.write(img, spec);
					
					// Update spectrogram for this report entry
					entry.setSpectrogram(IOHelper.getRelativePath(clOptions.getReportPath(), new File(pngFile)));
					
					// Query DB and ask it to enable debug info
					db.findSongWithDebugInfo(sig, outputDir, true);
					
					// Find any .txt files the previous method call may have found and run an R script on them.
					List<String> convertedHistFiles = new ArrayList<String>();
					for (File histFile : outputDir.listFiles()) {
						if (histFile.getName().endsWith(".txt")) {
							String inFile  = histFile.getAbsolutePath();
							String outFile = inFile.substring(0, inFile.length());
							String script  = String.format(rScript, outFile, inFile);
							
							Process exec = Runtime.getRuntime().exec("R --vanilla");
							BufferedWriter stdinWriter = new BufferedWriter(new OutputStreamWriter(exec.getOutputStream()));
							stdinWriter.write(script);
							stdinWriter.close();
							
							// Check exit status. If it failed, probably we're missing R.
							if (exec.waitFor() != 0) {
								System.err.println("WARNING: couldn't generate histogram from log file. Please install R.");
							}
							else {
								convertedHistFiles.add(IOHelper.getRelativePath(clOptions.getReportPath(), new File(outFile)));
							}
						}
					}
					
					entry.setHistogram(convertedHistFiles.toArray(new String[convertedHistFiles.size()]));
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
}
