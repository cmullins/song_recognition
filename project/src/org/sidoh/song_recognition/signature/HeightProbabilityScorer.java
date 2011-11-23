package org.sidoh.song_recognition.signature;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.sidoh.math.Histogram;

public class HeightProbabilityScorer extends HistogramScorer {
	private static final long serialVersionUID = 46213618758129729L;

	@Override
	public double score(Histogram hist) {
		double mean = hist.meanCount();
		double sd   = hist.sdCount();
		
		NormalDistribution standardNormal = new NormalDistributionImpl(0,1);
		
		double xi    = (hist.getMaxCount()-mean)/sd;
		double alpha = (-mean/sd);
		double beta  = (80000-mean)/sd;
		
		try {
		return 1d - 
				((standardNormal.cumulativeProbability(xi) - standardNormal.cumulativeProbability(alpha)) 
					/ 
				(standardNormal.cumulativeProbability(beta) - standardNormal.cumulativeProbability(alpha)));
		}
		catch (MathException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected double getProbSuccess(Histogram hist) {
		return 1d / hist.getNumBins();
	}
	
}
