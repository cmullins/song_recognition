package org.sidoh.math;

public class Stats {
	/**
	 * 
	 * @param a
	 * @return
	 */
	public static double mean(Iterable<Double> a) {
		double sum = 0d;
		int count  = 0;
		for (double value : a) {
			sum += value;
			count++;
		}
		return sum/count;
	}
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	public static double variance(Iterable<Double> a) {
		double sum = 0d;
		double sq  = 0d;
		int n = 0;
		
		for (double value : a) {
			sum += value;
			sq  += value*value;
			n++;
		}
		
		return 
			(n*sq - sum*sum)
				/
			(n * (n-1));
	}
	
	/**
	 * 
	 * @param n
	 * @param sum
	 * @param sqSum
	 * @return
	 */
	public static double variance(int n, double sum, double sqSum) {
		return (n*sqSum - sum*sum) / (n * (n-1));
	}
	
	/**
	 * 
	 * @param n
	 * @param sum
	 * @param sqSum
	 * @return
	 */
	public static double sd(int n, double sum, double sqSum) {
		return Math.sqrt(variance(n,sum,sqSum));
	}
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	public static double sd(Iterable<Double> a) {
		return Math.sqrt(variance(a));
	}
}
