package org.sidoh.song_recognition.benchmark;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.sidoh.collections.HashOfSets;
import org.sidoh.io.IOHelper;
import org.sidoh.io.ProgressNotifier;
import org.sidoh.peak_detection.StatefulPeakDetector;
import org.sidoh.peak_detection.StatefulSdsFromMeanPeakDetector;
import org.sidoh.song_recognition.audio_io.FrameBuffer;
import org.sidoh.song_recognition.audio_io.WavFileException;
import org.sidoh.song_recognition.signature.CoordinateAgnosticStarBuffer;
import org.sidoh.song_recognition.signature.EvenlySpreadInFrequencyStarBuffer;
import org.sidoh.song_recognition.signature.EvenlySpreadInTimeStarBuffer;
import org.sidoh.song_recognition.signature.StarBuffer;
import org.sidoh.song_recognition.spectrogram.PngSpectrogramConstellationWriter;
import org.sidoh.song_recognition.spectrogram.Spectrogram;
import org.sidoh.song_recognition.spectrogram.SpectrogramWriter;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SettingsTwiddler {
	public static class ParameterFilter extends Filter {

		@Override
		public String description() {
			return "Parses the requested URI for parameters";
		}

		@Override
		public void doFilter(HttpExchange exchange, Chain chain)
				throws IOException {
			parseGetParameters(exchange);
			parsePostParameters(exchange);
			chain.doFilter(exchange);
		}

		private void parseGetParameters(HttpExchange exchange)
				throws UnsupportedEncodingException {

			Map<String, Object> parameters = new HashMap<String, Object>();
			URI requestedUri = exchange.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			exchange.setAttribute("parameters", parameters);
		}

		private void parsePostParameters(HttpExchange exchange)
				throws IOException {

			if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
				@SuppressWarnings("unchecked")
				Map<String, Object> parameters = (Map<String, Object>) exchange
						.getAttribute("parameters");
				InputStreamReader isr = new InputStreamReader(
						exchange.getRequestBody(), "utf-8");
				BufferedReader br = new BufferedReader(isr);
				String query = br.readLine();
				parseQuery(query, parameters);
			}
		}

		@SuppressWarnings("unchecked")
		private void parseQuery(String query, Map<String, Object> parameters)
				throws UnsupportedEncodingException {

			if (query != null) {
				String pairs[] = query.split("[&]");

				for (String pair : pairs) {
					String param[] = pair.split("[=]");

					String key = null;
					String value = null;
					if (param.length > 0) {
						key = URLDecoder.decode(param[0],
								System.getProperty("file.encoding"));
					}

					if (param.length > 1) {
						value = URLDecoder.decode(param[1],
								System.getProperty("file.encoding"));
					}

					if (parameters.containsKey(key)) {
						Object obj = parameters.get(key);
						if (obj instanceof List<?>) {
							List<String> values = (List<String>) obj;
							values.add(value);
						} else if (obj instanceof String) {
							List<String> values = new ArrayList<String>();
							values.add((String) obj);
							values.add(value);
							parameters.put(key, values);
						}
					} else {
						parameters.put(key, value);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public static Map<String, Object> getParams(HttpExchange ex) {
			return (Map<String, Object>) ex.getAttribute("parameters");
		}
	}
	public static class StaticOptions {
		public final static String htmlDir = "/Users/chris/src/663_pattern_recognition/project/src/html/twiddle_server";
		
		public static InputStream getFileStream(String name) throws FileNotFoundException {
			return new FileInputStream(new File(htmlDir, name));
		}
	}
	public static class ClOptions {
		protected static CommandLine cmd;
		
		private static enum ClOption {
			@SuppressWarnings("static-access")
			HTTP_PORT(
				OptionBuilder
					.hasArg()
					.withDescription("Port to run HTTP server on. Default = 8000")
					.withArgName("p")
					.withLongOpt("http-port")
					.create("httpport")),
			@SuppressWarnings("static-access")
			WORK_DIR(
				OptionBuilder
					.hasArg()
					.withDescription("Tmp working directory. Default = sys tmp dir")
					.withArgName("wd")
					.withLongOpt("work-dir")
					.create("workdir")),
			@SuppressWarnings("static-access")
			SONGS_DIR(
				OptionBuilder
					.hasArg()
					.withDescription("Directory containing songs to work with")
					.withArgName("sd")
					.withLongOpt("songs-dir")
					.isRequired()
					.create("songsdir")),
			@SuppressWarnings("static-access")
			MAX_THREADS(
				OptionBuilder
					.hasArg()
					.withDescription("Maximum number of worker threads to use (default = # processors)")
					.withArgName("t")
					.withLongOpt("max-num-threads")
					.create("maxthreads"));
			
			private final Option option;
			private ClOption(Option option) {
				this.option = option;
			}
			
			public Option getOption() {
				return option;
			}
			
			public boolean isPresent() {
				return cmd.hasOption(option.getArgName());
			}
			
			public String value() {
				return cmd.getOptionValue(option.getOpt());
			}
			
			public static Options allOptions() {
				Options r = new Options();
				
				for (int i = 0; i < ClOption.values().length; i++) {
					r.addOption(ClOption.values()[i].getOption());
				}
				
				return r;
			}
		}
		
		private final int httpPort;
		private final int maxThreads;
		private final File workDir;
		private final File songsDir;
		
		public ClOptions(CommandLine cmd) throws IOException {
			ClOptions.cmd = cmd;
			
			if (! ClOption.HTTP_PORT.isPresent()) {
				httpPort = 8000;
			}
			else {
				httpPort = Integer.parseInt(ClOption.HTTP_PORT.value());
			}
			
			if (! ClOption.MAX_THREADS.isPresent()) {
				maxThreads = Runtime.getRuntime().availableProcessors();
			}
			else {
				maxThreads = Integer.parseInt(ClOption.MAX_THREADS.value());
			}
			
			if (! ClOption.WORK_DIR.isPresent()) {
				workDir = new File(FileUtils.getTempDirectory(), "SettingsTwiddler/");
				workDir.mkdirs();
			}
			else {
				workDir = new File(ClOption.WORK_DIR.value());
			}
			
			songsDir = new File(ClOption.SONGS_DIR.value());
			if (! songsDir.exists()) {
				throw new RuntimeException("Songs directory doesn't exist!");
			}
		}
		
		public int getHttpPort() {
			return httpPort;
		}

		public File getWorkDir() {
			return workDir;
		}
		
		public File getSongsDir() {
			return songsDir;
		}
		
		public int getMaxThreads() {
			return maxThreads;
		}

		public static ClOptions getOptions(String[] cmdLineArgs) throws ParseException, IOException {
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(ClOption.allOptions(), cmdLineArgs);
			
			return new ClOptions(cmd);
		}
	}
	
	public static class TwiddleServer {
		private final File workingDir;
		private final File songsDir;
		private final int port;
		private HttpServer server;
		private Settings settings;
		private ClOptions options;
		
		private HashOfSets<String, String> activeSongs;
		private Map<String, String> songNames;
		private Deque<String> finishedImages;
		private ExecutorService workerPool;

		public TwiddleServer(ClOptions options) throws IOException {
			this.workingDir = options.getWorkDir();
			this.songsDir = options.getSongsDir();
			this.port = options.getHttpPort();
			this.options = options;
			this.settings = Settings.defaults();
			this.activeSongs = new HashOfSets<String, String>();
			this.songNames = new HashMap<String, String>();
			this.workerPool = Executors.newSingleThreadExecutor();
			this.finishedImages = new LinkedList<String>();
			
			// Need separate memory for each spectrogram since we're handling them in parallel
			settings.setSpectrogramBuilder(Spectrogram.inMemory())
				.setProgressNotifer(ProgressNotifier.nullNotifier()) // quiet
				.setMaxNumThreads(options.getMaxThreads())
			;
			
			server = HttpServer.create(new InetSocketAddress(port), 0);
			HttpContext context = server.createContext("/", new TwiddleRequestHandler());
			context.getFilters().add(new ParameterFilter());
			server.setExecutor(null);
			server.start();
		}
		
		public void stopWhenCurrentThreadExists() {
			final Thread mainThread = Thread.currentThread();
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						System.out.println(mainThread.getName());
						mainThread.join();
					}
					catch (InterruptedException e) { }
					finally {
						server.stop(0);
					}
				}
			}).start();
		}
		
		/**
		 * Serve a static file on the filesystem to the client
		 * 
		 * @param file
		 * @param ex
		 * @throws IOException
		 */
		protected void serveFile(File file, HttpExchange ex) throws IOException {
			serveStream(new FileInputStream(file), ex);
		}
		
		protected void serveFile(String file, HttpExchange ex) throws IOException {
			serveFile(new File(file), ex);
		}
		
		protected void serveStream(InputStream in, HttpExchange ex) throws IOException {
				try {
					// Copy to buffer
					ByteArrayOutputStream buffer = new ByteArrayOutputStream();
					IOUtils.copy(in, buffer);
					byte[] bytes = buffer.toByteArray();
					ByteArrayInputStream transferBuffer = new ByteArrayInputStream(bytes);
					
					ex.sendResponseHeaders(200, bytes.length);
					OutputStream out = ex.getResponseBody();
					IOUtils.copy(transferBuffer, out);
					out.close();
				} catch (IOException e) {
					ex.sendResponseHeaders(500, 0);
					ex.getResponseBody().close();
				}
		}
		
		protected void serveSetting(String settingId, HttpExchange ex) throws IOException {
			ByteArrayInputStream toServe = null;
			
			if (settingId.equals("song-list")) {
				StringBuilder s = new StringBuilder("[");
				
				for (File child : options.getSongsDir().listFiles()) {
					if (child.getName().endsWith(".wav")) {
						s.append(String.format("{ \"name\" : \"%s\", \"path\" : \"%s\" },",
							child.getName(),
							child.getAbsolutePath()));
					}
				}
				
				if (s.charAt(s.length()-1) == ',') {
					s.setLength(s.length() - 1);
				}
				
				s.append("]");
				
				toServe = new ByteArrayInputStream(s.toString().getBytes());
			}
			else {
				ex.sendResponseHeaders(404, 0);
				ex.getResponseBody().close();
			}
			
			serveStream(toServe, ex);
		}
		
		protected void processSong(final String path) {
			workerPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						SpectrogramWriter writer = new PngSpectrogramConstellationWriter(
							settings.getConstellationExtractor(),
							ProgressNotifier.nullNotifier());
						FrameBuffer frames = settings.getBufferBuilder().fromWavFile(path);
						Spectrogram spec = settings.getSpectrogramBuilder()
							.copy()
							.contrast(300)
							.create(frames);
						File outputFile = IOHelper.toSequenceFile(
							new File(options.getWorkDir(),
									String.format("%s.png", new File(path).getName())));
						OutputStream out = new FileOutputStream(outputFile);
						writer.write(out, spec);
						out.close();
						activeSongs.addFor(path, outputFile.getName());
					}
					catch (WavFileException e) {
						e.printStackTrace();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		protected void addSong(final String path, final String name) {
			songNames.put(path, name);
			if (! activeSongs.containsKey(path)) {
				activeSongs.put(path, new LinkedHashSet<String>());
				processSong(path);
			}
		}
		
		protected void serveCommand(String commandId, HttpExchange ex) throws IOException {
			ByteArrayInputStream toServe = null;
			
			if (commandId.equals("add-song")) {
				Map<String, Object> params = ParameterFilter.getParams(ex);
				
				String path = params.get("path").toString();
				String name = params.get("name").toString();
				addSong(path, name);
				toServe = new ByteArrayInputStream(new byte[0]);
			}
			else {
				ex.sendResponseHeaders(404, 0);
				ex.getResponseBody().close();
			}
			
			serveStream(toServe, ex);
		}
		
		protected void serveImages(HttpExchange ex) throws IOException {
			StringBuilder s = new StringBuilder("[");
			
			for (Entry<String, Set<String>> entry : activeSongs.entrySet()) {
				String song = entry.getKey();
				s.append(String.format("{\"path\" : \"%s\", \"name\" : \"%s\", \"images\" : [", 
						song,
						songNames.get(song)));
				
				for (String img : entry.getValue()) {
					s.append(String.format("\"%s\",", img));
				}
				
				if (s.charAt(s.length()-1) == ',') {
					s.setLength(s.length()-1);
				}
				
				s.append("]},");
			}
			
			if (s.charAt(s.length()-1) == ',') {
				s.setLength(s.length()-1);
			}
			
			s.append("]");
			
			serveStream(new ByteArrayInputStream(s.toString().getBytes()), ex);
		}
		
		protected void updateImages() {
			for (String path : activeSongs.keySet()) {
				processSong(path);
			}
		}
		
		protected void updateSetting(String key, String val) {
			if (key.equals("frame-size")) {
				settings.setFrameSize(Integer.parseInt(val));
			}
			else if (key.equals("frame-overlap")) {
				settings.setFrameOverlap(Double.parseDouble(val));
			}
			else if (key.equals("peak-algorithm")) {
				if (val.equals("n-sds")) {
					settings.setPeakDetector(StatefulPeakDetector.sdsFromMean(
						settings.getWindowSize(),
						settings.getNSdsAboveMean()));
				}
				else {
					settings.setPeakDetector(StatefulPeakDetector.meanDelta(
						settings.getWindowSize()));
				}
			}
			else if (key.equals("window-size")) {
				settings.setWindowSize(Integer.parseInt(val));
			}
			else if (key.equals("n-sds")) {
				settings.setNSdsAboveMean(Double.parseDouble(val));
			}
			else if (key.equals("peak-density")) {
				settings.setStarDensityFactor(Double.parseDouble(val));
			}
			else if (key.equals("peak-retention")) {
				if (val.equals("coordinate-agnostic")) {
					settings.setStarSelectionBuffer(StarBuffer.coordinateAgnostic(
						settings.getStarDensityFactor()));
				}
				else if (val.equals("even-time")) {
					settings.setStarSelectionBuffer(StarBuffer.evenlySpreadInTime(
						settings.getStarDensityFactor()));
				}
				else {
					settings.setStarSelectionBuffer(StarBuffer.evenlySpreadInFrequency(
						settings.getStarDensityFactor()));
				}
			}
			else {
				System.err.println("Invalid setting key: " + key);
				return;
			}
			
			updateImages();
		}
		
		public String settingsToJSON() {
			StringBuilder b = new StringBuilder("{");
			
			String bufferId;
			if (settings.getStarSelectionBuffer() instanceof CoordinateAgnosticStarBuffer.Builder) {
				bufferId = "coordinate-agnostic";
			}
			else if (settings.getStarSelectionBuffer() instanceof EvenlySpreadInTimeStarBuffer.Builder) {
				bufferId = "even-time";
			}
			else {
				bufferId = "even-frequency";
			}
			
			b.append(String.format("\"frame-size\" : %d,", settings.getFrameSize()));
			b.append(String.format("\"frame-overlap\" : %.2f,", settings.getFrameOverlap()));
			b.append(String.format("\"peak-algorithm\" : \"%s\",",
				settings.getPeakDetector() instanceof StatefulSdsFromMeanPeakDetector.Builder
					? "n-sds"
					: "max"));
			b.append(String.format("\"window-size\" : %d,", settings.getWindowSize()));
			b.append(String.format("\"n-sds\" : %f,", settings.getNSdsAboveMean()));
			b.append(String.format("\"peak-density\" : %.2f,", settings.getStarDensityFactor()));
			b.append(String.format("\"peak-retention\" : \"%s\"", bufferId));
			
			b.append("}");
			
			return b.toString();
		}
		
		protected void serveString(String s, HttpExchange ex) throws IOException {
			serveStream(new ByteArrayInputStream(s.getBytes()), ex);
		}
		
		protected class TwiddleRequestHandler implements HttpHandler {
			@Override
			public void handle(HttpExchange ex) throws IOException {
				String uri = ex.getRequestURI().getPath();
				
				if (uri.equals("/")) {
					serveStream(StaticOptions.getFileStream("ui.html"), ex);
				}
				else if (uri.startsWith("/settings")) {
					String settingId = uri.substring(Math.min(uri.length(), "/settings/".length()));
					
					if (settingId.length() > 0) {
						serveSetting(settingId, ex);
					}
					else {
						serveString(settingsToJSON(), ex);
					}
				}
				else if (uri.startsWith("/update-setting/")) {
					String[] tokens = uri.split("/");
					String key = tokens[2];
					String val = tokens[3];
					
					updateSetting(key, val);
				}
				else if (uri.startsWith("/commands/")) {
					String commandId = uri.substring("/commands/".length());
					serveCommand(commandId, ex);
				}
				else if (uri.startsWith("/images")) {
					serveImages(ex);
				}
				else if (uri.startsWith("/spectrograms/")) {
					String img = uri.substring("/spectrograms/".length());
					serveFile(new File(options.getWorkDir(), img), ex);
				}
				else {
					try {
						InputStream toServe = StaticOptions.getFileStream(uri);
						serveStream(toServe, ex);
					}
					catch (FileNotFoundException e) {
						System.err.println("Unknown request: " + uri);
						ex.sendResponseHeaders(404, 0);
						ex.getResponseBody().close();
					}
					catch (IOException e) {
						ex.sendResponseHeaders(500, 0);
						ex.getResponseBody().close();
					}
				}
			}
		}
	}
	
	public static void main(String[] args) throws ParseException, IOException {
		final ClOptions clOptions = ClOptions.getOptions(args);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					new TwiddleServer(clOptions);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
