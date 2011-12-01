package org.sidoh.song_recognition.benchmark.report;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ReportServer {
	private static final class Settings {
		public static final String jqueryInclude = 
			"<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.6.2/jquery.min.js\"></script>";
		public static final String jqueryUiInclude =
			"<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js\"></script>";
		
		// TODO: I'm a terrible person.
		public static final File staticFilePath = new File("/Users/chris/src/663_pattern_recognition/project/src/html");
	}
	
	private final Report report;
	private final int port;
	private final HttpServer server;
	private final File reportDir;
	
	public ReportServer(Report report, File reportDir, int port) throws IOException {
		this.report = report;
		this.reportDir = reportDir;
		this.port = port;
		
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/", new RequestHandler());
		server.setExecutor(null);
		server.start();
	}
	
	public void stop() {
		server.stop(0);
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
	
	protected class RequestHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange ex) throws IOException {
			String uri = ex.getRequestURI().getPath();
			
			if (uri.equals("/")) {
				serveFile(new File(Settings.staticFilePath, "ui.html"), ex);
			}
			else if (uri.startsWith("/static/")) {
				File realFile = new File(reportDir, uri.substring("/static/".length()));
				
				serveFile(realFile, ex);
			}
			else if (uri.equals("/report_data.json")) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				Report.jsonSerializer.serialize(report, buffer);
				
				ex.sendResponseHeaders(200, buffer.size());
				OutputStream out = ex.getResponseBody();
				IOUtils.copy(new ByteArrayInputStream(buffer.toByteArray()), out);
				out.close();
			}
			else {
				File fileToServe = new File(Settings.staticFilePath, ex.getRequestURI().getPath());
				
				serveFile(fileToServe, ex);
			}
		}
		
		private void serveFile(File file, HttpExchange ex) throws IOException {
			try {
				InputStream fileOut = new FileInputStream(file);
				ex.sendResponseHeaders(200, FileUtils.sizeOf(file));
				
				OutputStream out = ex.getResponseBody();
				
				IOUtils.copy(fileOut, out);
				
				out.close();
			}
			catch (FileNotFoundException e) {
				ex.sendResponseHeaders(404, 0);
				ex.getResponseBody().close();
			}
			catch (IOException e) {
				ex.sendResponseHeaders(500, 0);
				ex.getResponseBody().close();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("Starting server...");
		
		ReportServer server = new ReportServer(new Report(), new File("/tmp"), 8000);
	}
}
