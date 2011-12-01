package org.sidoh.song_recognition.signature;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map.Entry;

import org.sidoh.math.Histogram;

public class LoggingScorer extends HistogramScorer {
	private static final long serialVersionUID = 8906526696511849302L;
	
	private final HistogramScorer inner;

	private final String filename;

	public LoggingScorer(String filename, HistogramScorer inner) {
		this.filename = filename;
		this.inner = inner;
	}

	@Override
	public double score(Histogram hist) {
		double answer = inner.score(hist);
		
		try {
			String file = String.format("%s_%.5f.txt", filename, answer);
			PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
			
			for (Entry<Integer, List<Double>> entry : hist.getValues().entrySet()) {
				out.printf("%d\t%d\n", entry.getKey(), entry.getValue().size());
			}
			
			out.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return answer;
	}

}
