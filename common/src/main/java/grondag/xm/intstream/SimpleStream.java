/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.intstream;

import net.minecraft.util.Mth;

/**
 * Uses large blocks only - may be space-inefficient.
 */

//TODO: replace with IntStreamProvider
class SimpleStream implements IntStream {
	int[][] blocks = new int[16][];

	int blockCount = 0;
	int capacity = 0;
	boolean isCompact = false;

	private void checkAddress(int address) {
		if (address >= capacity) {
			if (isCompact) {
				// uncompact
				final int[] lastBlock = blocks[blockCount - 1];
				final int[] newBlock = IntStreams.claimBlock();

				System.arraycopy(lastBlock, 0, newBlock, 0, lastBlock.length);
				blocks[blockCount - 1] = newBlock;

				capacity = IntStreams.BLOCK_SIZE * blockCount;
				isCompact = false;

				// if big enough after uncompacting, then we are done
				if (address < capacity) {
					return;
				}
			}

			final int currentBlocks = capacity >> IntStreams.BLOCK_SHIFT;
			final int blocksNeeded = (address >> IntStreams.BLOCK_SHIFT) + 1;

			if (blocksNeeded > blocks.length) {
				final int newMax = Mth.smallestEncompassingPowerOfTwo(blocksNeeded);
				final int[][] newBlocks = new int[newMax][];
				System.arraycopy(blocks, 0, newBlocks, 0, blocks.length);
				blocks = newBlocks;
			}

			for (int i = currentBlocks; i < blocksNeeded; i++) {
				blocks[i] = IntStreams.claimBlock();
			}

			capacity = blocksNeeded << IntStreams.BLOCK_SHIFT;
			blockCount = blocksNeeded;
		}
	}

	@Override
	public int get(int address) {
		return address < capacity ? blocks[address >> IntStreams.BLOCK_SHIFT][address & IntStreams.BLOCK_MASK] : 0;
	}

	public void prepare(int sizeHint) {
		checkAddress(sizeHint - 1);
	}

	private void releaseBlocks() {
		if (blockCount > 0) {
			// don't reuse last block if it isn't a block size
			final int skipIndex = isCompact ? -1 : blockCount - 1;

			for (int i = 0; i < blockCount; i++) {
				if (i != skipIndex) {
					IntStreams.releaseBlock(blocks[i]);
				}

				blocks[i] = null;
			}
		}

		blockCount = 0;
		capacity = 0;
		isCompact = false;
	}

	@Override
	public void set(int address, int value) {
		checkAddress(address);
		blocks[address >> IntStreams.BLOCK_SHIFT][address & IntStreams.BLOCK_MASK] = value;
	}

	@Override
	public void clear() {
		// drop last block if we are compacted
		if (isCompact) {
			blockCount--;
			capacity = blockCount * IntStreams.BLOCK_SIZE;
			blocks[blockCount] = null;
			isCompact = false;
		}

		if (blockCount > 0) {
			for (int i = 0; i < blockCount; i++) {
				System.arraycopy(IntStreams.EMPTY, 0, blocks[i], 0, IntStreams.BLOCK_SIZE);
			}
		}
	}

	@Override
	public void release() {
		releaseBlocks();
		IntStreams.release(this);
	}

	@Override
	public void copyFrom(int targetAddress, IntStream source, int sourceAddress, int length) {
		// PERF: special case handling using ArrayCopy for faster transfer
		IntStream.super.copyFrom(targetAddress, source, sourceAddress, length);
	}

	@Override
	public void compact() {
		if (isCompact || blockCount == 0) {
			return;
		}

		int targetBlock = blockCount - 1;

		while (targetBlock >= 0) {
			final int[] block = blocks[targetBlock];
			int i = IntStreams.BLOCK_SIZE - 1;

			while (i >= 0 && block[i] == 0) {
				i--;
			}

			if (i == -1) {
				// release empty blocks
				IntStreams.releaseBlock(block);
				blocks[targetBlock] = null;
				blockCount--;
				capacity -= IntStreams.BLOCK_SIZE;
			} else if (i == IntStreams.BLOCK_SIZE - 1) {
				// ending on a block boundary so no need to compact
				return;
			} else {
				// partially full block
				final int shortSize = i + 1;
				final int[] shortBlock = new int[shortSize];
				System.arraycopy(block, 0, shortBlock, 0, shortSize);
				IntStreams.releaseBlock(block);
				blocks[targetBlock] = shortBlock;
				capacity = (blockCount - 1) * IntStreams.BLOCK_SIZE + shortSize;
				isCompact = true;
				return;
			}

			targetBlock--;
		}
	}

	@Override
	public int capacity() {
		return capacity;
	}
}
