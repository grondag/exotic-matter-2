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

import java.util.concurrent.ArrayBlockingQueue;

// TODO: replace with IntStreamProvider
public abstract class IntStreams {
	public static final int BLOCK_SIZE = 1024;
	static final int BLOCK_MASK = BLOCK_SIZE - 1;
	static final int BLOCK_SHIFT = Integer.bitCount(BLOCK_MASK);

	private static final ArrayBlockingQueue<SimpleStream> simpleStreams = new ArrayBlockingQueue<>(256);

	private static final ArrayBlockingQueue<int[]> bigBlocks = new ArrayBlockingQueue<>(256);

	static final int[] EMPTY = new int[BLOCK_SIZE];

	static int[] claimBlock() {
		final int[] result = bigBlocks.poll();

		if (result == null) {
			return new int[BLOCK_SIZE];
		} else {
			System.arraycopy(EMPTY, 0, result, 0, BLOCK_SIZE);
			return result;
		}
	}

	static void releaseBlock(int[] block) {
		bigBlocks.offer(block);
	}

	public static IntStream claim(int sizeHint) {
		SimpleStream result = simpleStreams.poll();

		if (result == null) {
			result = new SimpleStream();
		}

		result.prepare(sizeHint);
		return result;
	}

	public static IntStream claim() {
		return claim(BLOCK_SIZE);
	}

	static void release(SimpleStream freeStream) {
		simpleStreams.offer(freeStream);
	}
}
