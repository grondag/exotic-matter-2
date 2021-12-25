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

import java.util.Arrays;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.collision.Functions.Float3Consumer;
import grondag.xm.collision.Functions.Float3Test;
import grondag.xm.collision.Functions.Int3Consumer;

@Internal
class OctreeCoordinates {
	static final long FULL_BITS = 0xFFFFFFFFFFFFFFFFL;
	static final long[] ALL_FULL = new long[64];
	static final long[] ALL_EMPTY = new long[64];

	/**
	 * Indexes to face voxels in division level 4.
	 */
	static final int[] EXTERIOR_INDEX_4 = new int[1352];

	/**
	 * Indexes to face voxels in division level 3.
	 */
	static final int[] EXTERIOR_INDEX_3 = new int[1352]; //FIXME: why not 296?

	static {
		Arrays.fill(ALL_FULL, FULL_BITS);
		int exteriorIndex = 0;

		for (int i = 0; i < 4096; i++) {
			final int xyz = indexToXYZ4(i);
			final int x = xyz & 15;
			final int y = (xyz >> 4) & 15;
			final int z = (xyz >> 8) & 15;

			if (x == 0 || x == 15) {
				EXTERIOR_INDEX_4[exteriorIndex++] = xyzToIndex4(xyz);
				continue;
			}

			if (y == 0 || y == 15) {
				EXTERIOR_INDEX_4[exteriorIndex++] = xyzToIndex4(xyz);
				continue;
			}

			if (z == 0 || z == 15) {
				EXTERIOR_INDEX_4[exteriorIndex++] = xyzToIndex4(xyz);
				continue;
			}
		}

		assert exteriorIndex == 1352;

		int exteriorBottomIndex = 0;

		for (int i = 0; i < 512; i++) {
			final int xyz = indexToXYZ3(i);
			final int x = xyz & 7;
			final int y = (xyz >> 3) & 7;
			final int z = (xyz >> 6) & 7;

			if (x == 0 || x == 7) {
				EXTERIOR_INDEX_3[exteriorBottomIndex++] = xyzToIndex3(xyz);
				continue;
			}

			if (y == 0 || y == 7) {
				EXTERIOR_INDEX_3[exteriorBottomIndex++] = xyzToIndex3(xyz);
				continue;
			}

			if (z == 0 || z == 7) {
				EXTERIOR_INDEX_3[exteriorBottomIndex++] = xyzToIndex3(xyz);
				continue;
			}
		}

		assert exteriorBottomIndex == 296;
	}

	static int xyzToIndex(final int xyz, final int divisionLevel) {
		switch (divisionLevel) {
			case 0:
				return 0;

			case 1:
				return xyz;

			case 2:
				return xyzToIndex2(xyz);

			case 3:
				return xyzToIndex3(xyz);

			case 4:
				return xyzToIndex4(xyz);
		}

		return 0;
	}

	static int indexToXYZ(final int index, final int divisionLevel) {
		switch (divisionLevel) {
			case 0:
				return 0;

			case 1:
				return index;

			case 2:
				return indexToXYZ2(index);

			case 3:
				return indexToXYZ3(index);

			case 4:
				return indexToXYZ4(index);
		}

		return 0;
	}

	/**
	 * Gives octree index w/ division level 2 from packed 2-bit Cartesian coordinates.
	 */
	static int xyzToIndex2(final int xyz2) {
		final int y = xyz2 >> 1;
		final int z = xyz2 >> 2;

		return (xyz2 & 1) | (y & 2) | (z & 4) | (((xyz2 & 2) | (y & 4) | (z & 8)) << 2);
	}

	/**
	 * Gives packed 2-bit Cartesian coordinates from octree index w/ division level 2.
	 */
	static int indexToXYZ2(final int i2) {
		final int j = i2 >> 2;
		return ((i2 & 1) | (j & 2)) | (((i2 & 2) | (j & 4)) << 1) | (((i2 & 4) | (j & 8)) << 2);
	}

	/**
	 * Packed 2-bit Cartesian coordinates.
	 */
	static int packedXYZ2(int x, int y, int z) {
		return x | (y << 2) | (z << 4);
	}

	static int xyzToIndex2(int x, int y, int z) {
		return xyzToIndex2(packedXYZ2(x, y, z));
	}

	/**
	 * Gives octree index w/ division level 3 from packed 3-bit Cartesian coordinates.
	 */
	static int xyzToIndex3(final int xyz3) {
		// coordinate values are 3 bits each: xxx, yyy, zzz
		// voxel coordinates are interleaved: zyx zyx zyx

		// shift all bits of y, z at once to avoid separate shift ops later

		final int y = xyz3 >> 2;
		final int z = xyz3 >> 4;

		return (xyz3 & 1) | (y & 2) | (z & 4) | (((xyz3 & 2) | (y & 4) | (z & 8)) << 2) | (((xyz3 & 4) | (y & 8) | (z & 16)) << 4);
	}

	/**
	 * Gives packed 3-bit Cartesian coordinates from octree index w/ division level 3.
	 */
	static int indexToXYZ3(final int i3) {
		// coordinate values are 3 bits each: xxx, yyy, zzz
		// voxel coordinates are interleaved: zyx zyx zyx

		final int j = i3 >> 2;
		final int k = i3 >> 4;
		return ((i3 & 1) | (j & 2) | (k & 4)) | (((i3 & 2) | (j & 4) | (k & 8)) << 2) | (((i3 & 4) | (j & 8) | (k & 16)) << 4);
	}

	static void forXYZ3(final int i3, Int3Consumer consumer) {
		final int j = i3 >> 2;
		final int k = i3 >> 4;
		consumer.accept((i3 & 1) | (j & 2) | (k & 4), ((i3 & 2) | (j & 4) | (k & 8)) >> 1, ((i3 & 4) | (j & 8) | (k & 16)) >> 2);
	}

	/**
	 * Packed 3-bit Cartesian coordinates.
	 */
	static int packedXYZ3(int x, int y, int z) {
		return x | (y << 3) | (z << 6);
	}

	static int xyzToIndex3(int x, int y, int z) {
		return xyzToIndex3(packedXYZ3(x, y, z));
	}

	/**
	 * Gives octree index w/ division level 4 from packed 4-bit Cartesian coordinates.
	 */
	static int xyzToIndex4(final int xyz4) {
		// coordinate values are 4 bits each: xxxx, yyyy, zzzz
		// voxel coordinates are interleaved: zyx zyx zyx zyx

		// shift all bits of y, z at once to avoid separate shift ops later
		// like so:
		// xxxx
		// yyyy
		// zzzz

		final int y = xyz4 >> 3;
		final int z = xyz4 >> 6;

		return (xyz4 & 1) | (y & 2) | (z & 4) | (((xyz4 & 2) | (y & 4) | (z & 8)) << 2) | (((xyz4 & 4) | (y & 8) | (z & 16)) << 4)
				| (((xyz4 & 8) | (y & 16) | (z & 32)) << 6);
	}

	static int xyzToIndex4(int x, int y, int z) {
		// PERF: avoid packing/unpacking
		return xyzToIndex4(packedXYZ4(x, y, z));
	}

	/**
	 * Gives packed 4-bit Cartesian coordinates from octree index w/ division level 4.
	 */
	static int indexToXYZ4(final int i4) {
		// coordinate values are 4 bits each: xxxx, yyyy, zzzz
		// voxel coordinates are interleaved: zyx zyx zyx zyx

		final int j = i4 >> 2;
		final int k = i4 >> 4;
		final int l = i4 >> 6;

		return ((i4 & 1) | (j & 2) | (k & 4) | (l & 8))
				| (((i4 & 2) | (j & 4) | (k & 8) | (l & 16)) << 3)
				| (((i4 & 4) | (j & 8) | (k & 16) | (l & 32)) << 6);
	}

	/**
	 * Packed 4-bit Cartesian coordinates.
	 */
	static int packedXYZ4(int x, int y, int z) {
		return x | (y << 4) | (z << 8);
	}

	static float voxelSize(int divisionLevel) {
		return 1f / (1 << divisionLevel);
	}

	static float voxelRadius(int divisionLevel) {
		return 0.5f / (1 << divisionLevel);
	}

	static void withCenter(final int index, final int divisionLevel, Float3Consumer consumer) {
		final int xyz = indexToXYZ(index, divisionLevel);
		final float d = OctreeCoordinates.voxelSize(divisionLevel);
		final int mask = (1 << divisionLevel) - 1;
		consumer.accept(((xyz & mask) + 0.5f) * d, (((xyz >> divisionLevel) & mask) + 0.5f) * d, (((xyz >> (divisionLevel * 2)) & mask) + 0.5f) * d);
	}

	static boolean testCenter(final int index, final int divisionLevel, Float3Test test) {
		final int xyz = indexToXYZ(index, divisionLevel);
		final float d = OctreeCoordinates.voxelSize(divisionLevel);
		final int mask = (1 << divisionLevel) - 1;
		return test.apply(((xyz & mask) + 0.5f) * d, (((xyz >> divisionLevel) & mask) + 0.5f) * d, (((xyz >> (divisionLevel * 2)) & mask) + 0.5f) * d);
	}

	static void withXYZ(final int index, final int divisionLevel, Int3Consumer consumer) {
		final int xyz = indexToXYZ(index, divisionLevel);
		final int mask = (1 << divisionLevel) - 1;
		consumer.accept(xyz & mask, (xyz >> divisionLevel) & mask, (xyz >> (divisionLevel * 2)) & mask);
	}
}
