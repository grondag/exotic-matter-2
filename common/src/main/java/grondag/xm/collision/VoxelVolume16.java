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

package grondag.xm.collision;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.collision.Functions.Int3Consumer;

/**
 * Operations on Cartesian representation of 16x16x16 voxels in unit cube that
 * can happen more efficiently that way. (Filling, mostly)
 */
@Internal
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
	 * Sets bits in target if they are set in the mask. Equivalent of {@code target |=
	 * mask} Operates on 256-bit word stored as 4 long values. Index is of the
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
