package org.sidoh.collections;

import java.util.Iterator;

import org.sidoh.math.Stats;

/**
 * Provides something like {@link CountingMap}, but uses fixed-size blocks of
 * memory. This should allow for the same {@link BufferedCountingMap} to be
 * reused.
 * 
 */
public class BufferedCountingMap {
	
	public static class Builder {
		private final int blockSize;

		public Builder(int blockSize) {
			this.blockSize = blockSize;
		}
		
		public BufferedCountingMap create() {
			return new BufferedCountingMap(blockSize);
		}
	}
	
	private int minValue;
	private int maxValue;
	private final int blockSize;
	private final BlockContainer root;
	
	private int numActiveBins;
	private double countsSum;
	private double sqCountsSum;
	private int maxCount;
	private int minCount;
	
	public BufferedCountingMap(int blockSize) {
		// round to nearest odd.
		if (blockSize % 2 == 0) blockSize--;
		
		this.blockSize = blockSize;
		this.root = new BlockContainer(0);
		this.minValue = -blockSize/2;
		this.maxValue = blockSize/2;
	}
	
	/**
	 * Returns the number of bins with non-zero counts
	 * 
	 * @return
	 */
	public int getNumBins() {
		return numActiveBins;
	}
	
	/**
	 * Returns the sum of the counts
	 * 
	 * @return
	 */
	public double getCountsSum() {
		return countsSum;
	}
	
	/**
	 * Returns the sum of the squared counts
	 * 
	 * @return
	 */
	public double getSqCountsSum() {
		return sqCountsSum;
	}
	
	/**
	 * Returns the count of the bin with the most counts
	 * 
	 * @return
	 */
	public int getMaxCount() {
		return maxCount;
	}
	
	/**
	 * Returns the count of the bin with the fewest counts
	 * 
	 * @return
	 */
	public int getMinCount() {
		return minCount;
	}
	
	/**
	 * Returns the mean of the counts
	 * 
	 * @return
	 */
	public double getMeanCount() {
		return countsSum / getNumBins();
	}
	
	/**
	 * Returns the standard deviation of the counts
	 * 
	 * @return
	 */
	public double getCountSd() {
		return Stats.sd(getNumBins(), countsSum, sqCountsSum);
	}
	
	/**
	 * Gets an Iterable of the entries in this counting map.
	 * 
	 * @return
	 */
	public Iterable<Pair<Integer, Integer>> getEntries() {
		return new EntryIterator();
	}
	
	/**
	 * Increment the count for the provided value.
	 * 
	 * @param index
	 * @return
	 */
	public int increment(int value) {
		int count = containerFor(value).increment(value);
		
		if (count == 1) {
			numActiveBins++;
		}
		
		countsSum++;
		
		// (n+1)^2 = n^2 + 2n + 1
		sqCountsSum += (2*count + 1);
		
		if (count > maxCount) {
			maxCount = count;
		}
		
		return count;
	}
	
	/**
	 * Get the count for the provided value
	 * 
	 * @param index
	 * @return
	 */
	public int count(int value) {
		return containerFor(value).count(value);
	}
	
	/**
	 * Reset all counts and statistics to 0.
	 * 
	 */
	public void reset() {
		root.reset();
		countsSum = 0;
		maxCount = 0;
		minCount = Integer.MAX_VALUE;
		numActiveBins = 0;
		sqCountsSum = 0;
	}
	
	/**
	 * Helper method to get the appropriate block
	 * 
	 * @param index
	 * @return
	 */
	protected BlockContainer containerFor(int index) {
		BlockContainer bc = root;
		if (bc.leftBoundary() > index) {
			do {
				bc = bc.getLeftChild();
			} while (bc.leftBoundary() > index);
		}
		else if (bc.rightBoundary() < index) {
			do {
				bc = bc.getRightChild();
			} while (bc.rightBoundary() < index);
		}
		
		return bc;
	}
	
	private class EntryIterator implements Iterable<Pair<Integer,Integer>>, Iterator<Pair<Integer,Integer>> {
		private int index = minValue;
		
		@Override
		public boolean hasNext() {
			return index <= maxValue;
		}

		@Override
		public Pair<Integer, Integer> next() {
			return Pair.create(index, count(index++));
		}

		@Override
		public void remove() {
			throw new RuntimeException("No.");
		}

		@Override
		public Iterator<Pair<Integer, Integer>> iterator() {
			return this;
		}
	};
	
	protected class BlockContainer {
		private int[] block;
		private int centerValue;
		private BlockContainer leftChild;
		private BlockContainer rightChild;
		
		public BlockContainer(int centerValue) {
			this.block = new int[blockSize];
			this.centerValue = centerValue;
			this.leftChild = null;
			this.rightChild = null;
		}
		
		public int count(int value) {
			return block[getBlockIndex(value)];
		}
		
		public int increment(int value) {
			return ++block[getBlockIndex(value)];
		}
		
		public void reset() {
			for (int i = 0; i < blockSize; i++) {
				block[i] = 0;
			}
			if (leftChild != null) {
				leftChild.reset();
			}
			if (rightChild != null) {
				rightChild.reset();
			}
		}
		
		public int leftBoundary() {
			return centerValue-blockSize/2;
		}
		
		public int rightBoundary() {
			return centerValue+blockSize/2;
		}
		
		public BlockContainer getLeftChild() {
			if (leftChild == null) {
				minValue = centerValue-blockSize;
				leftChild = new BlockContainer(centerValue - blockSize);
			}
			return leftChild;
		}
		
		public BlockContainer getRightChild() {
			if (rightChild == null) {
				maxValue = centerValue+blockSize;
				rightChild = new BlockContainer(centerValue + blockSize);
			}
			return rightChild;
		}
		
		protected int getBlockIndex(int value) {
			return block.length/2 + (value - centerValue);
		}
	}

	public static Builder withBlockSize(int blockSize) {
		return new Builder(blockSize);
	}
}
