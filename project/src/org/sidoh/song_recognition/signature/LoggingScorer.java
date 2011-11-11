package org.sidoh.song_recognition.signature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;

import org.sidoh.math.Histogram;

public class LoggingScorer extends HistogramScorer {
	private static final long serialVersionUID = 8906526696511849302L;
	
	private final HistogramScorer inner;
	private final String dir;

	public LoggingScorer(String dir, HistogramScorer inner) {
		this.dir = dir;
		this.inner = inner;
	}

	@Override
	public double score(Histogram hist) {
		double answer = inner.score(hist);
		
		try {
			File file = new File(dir, String.valueOf(answer));
			
			if (! file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			
			PrintStream out = new PrintStream(new FileOutputStream(file));
			
			for (Collection<Double> values : hist.getValues().values()) {
				for (double value : values) {
					out.println(value);
				}
			}
			
			out.close();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return answer;
	}

}
