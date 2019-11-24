/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.collision;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.collision.Functions.Int3Consumer;

/**
 * Operations on Cartesian representation of 16x16x16 voxels in unit cube that
 * can happen more efficiently that way. (Filling, mostly)
 */
@API(status = INTERNAL)
class VoxelVolume16 {

	// UGLY - should probably just merge this with MeshVoxelizer because data structures are tightly coupled
	public static void forEachSimpleVoxel(long[] data, int offset, final int minVoxelCount, Int3Consumer consumer) {
		for (int x = 0; x < 16; x += 2) {
			for (int y = 0; y < 16; y += 2) {
				final long mask = 0b00000000000000110000000000000011L << (x + (y & 3) * 16);

				for (int z = 0; z < 16; z += 2) {
					final int i = ((z << 2) | (y >> 2));
					final int count = Long.bitCount(data[i + offset] & mask) + Long.bitCount(data[i + offset + 4] & mask);
					if (count >= minVoxelCount) {
						consumer.accept(x >> 1, y >> 1, z >> 1);
					}
				}
			}
		}
	}

	/**
	 * Clears set bits in target if they are set in the mask. Sets clear bits im
	 * target if they are set in the mask.
	 * Operates on 256-bit word stored as 4 long values. Index is of the
	 * compound word, NOT the array index. (Array index would be x4).
	 */
	static void compoundXor(final long[] target, int targetIndex, final long[] mask, int maskIndex) {
		targetIndex *= 4;
		maskIndex *= 4;
		target[targetIndex] ^= mask[maskIndex];
		target[targetIndex + 1] ^= mask[maskIndex + 1];
		target[targetIndex + 2] ^= mask[maskIndex + 2];
		target[targetIndex + 3] ^= mask[maskIndex + 3];
	}

	/**
	 * Sets bits in target if they are set in the mask. Equivalent of <target> |=
	 * <mask> Operates on 256-bit word stored as 4 long values. Index is of the
	 * compound word, NOT the array index. (Array index would be x4).
	 */
	static void compoundSet(final long[] target, int targetIndex, final long[] mask, int maskIndex) {
		targetIndex *= 4;
		maskIndex *= 4;
		target[targetIndex] |= mask[maskIndex];
		target[targetIndex + 1] |= mask[maskIndex + 1];
		target[targetIndex + 2] |= mask[maskIndex + 2];
		target[targetIndex + 3] |= mask[maskIndex + 3];
	}
}
