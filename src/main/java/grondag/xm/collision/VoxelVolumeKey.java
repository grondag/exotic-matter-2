/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.collision;

import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Generates non-intersecting collision boxes for a model within a single block
 * at 1/4 block distance (per axis).
 *
 * <p>Identifies which voxels intersects with polys in the block mesh to build a
 * shell at 1/8 resolution, then fills the shell interior and outputs 1/4 voxels
 * that are at least half full.
 *
 * <p>Output voxels sharing a face joined together by
 * {@link JoiningBoxListBuilder}. No other attempt is made to reduce box count -
 * instead relying on the low resolution to keep box counts reasonable.
 *
 * <p>During the shell identification, voxels are addressed using Octree
 * coordinates but those coordinates are never saved to state (exist only in the
 * call stack.) When leaf nodes are identified, voxel bits are set using
 * Cartesian coordinates converted from octree coordinates because Cartesian
 * representation is better (or at least as good) for the subsequent
 * simplification, fill and output operations.
 */
@Internal
class VoxelVolumeKey {
	private static final long[] EMPTY = new long[8];
	private boolean immutable = false;
	private int hash;
	private final long[] voxelBits = new long[8];

	void set(int x, int y, int z) {
		assert !immutable : "Attempt to mutate immutable voxel volume key";
		voxelBits[z] |= (1L << (x | (y << 3)));
	}

	boolean get(int x, int y, int z) {
		return (voxelBits[z] & (1L << (x | (y << 3)))) != 0;
	}

	void clear() {
		assert !immutable : "Attempt to mutate immutable voxel volume key";
		System.arraycopy(EMPTY, 0, voxelBits, 0, 8);
	}

	boolean isFull() {
		final long[] bits = voxelBits;
		return bits[0] == -1L
				&& bits[1] == -1L
				&& bits[2] == -1L
				&& bits[3] == -1L
				&& bits[4] == -1L
				&& bits[5] == -1L
				&& bits[6] == -1L
				&& bits[7] == -1L;
	}

	int count1(int x, int y, int z) {
		final long mask = 0b00001111000011110000111100001111L << (x * 4 + y * 32);
		z *= 4;
		final long[] bits = voxelBits;
		return Long.bitCount(bits[z] & mask)
				+ Long.bitCount(bits[z + 1] & mask)
				+ Long.bitCount(bits[z + 2] & mask)
				+ Long.bitCount(bits[z + 3] & mask);
	}

	int count2(int x, int y, int z) {
		final long mask = 0b0000001100000011L << (x * 2 + y * 16);
		z *= 2;
		final long[] bits = voxelBits;
		return Long.bitCount(bits[z] & mask)
				+ Long.bitCount(bits[z + 1] & mask);
	}

	@Override
	public int hashCode() {
		if (immutable) return hash;

		final long[] bits = voxelBits;
		return (int) (HashCommon.mix(bits[0])
				^ HashCommon.mix(bits[1])
				^ HashCommon.mix(bits[2])
				^ HashCommon.mix(bits[3])
				^ HashCommon.mix(bits[4])
				^ HashCommon.mix(bits[5])
				^ HashCommon.mix(bits[6])
				^ HashCommon.mix(bits[7]));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VoxelVolumeKey)) return false;

		final long[] b0 = voxelBits;
		final long[] b1 = ((VoxelVolumeKey) obj).voxelBits;

		return b0[0] == b1[0]
				&& b0[1] == b1[1]
				&& b0[2] == b1[2]
				&& b0[3] == b1[3]
				&& b0[4] == b1[4]
				&& b0[5] == b1[5]
				&& b0[6] == b1[6]
				&& b0[7] == b1[7];
	}

	boolean isEmpty() {
		final long[] bits = voxelBits;
		return bits[0] == 0
				&& bits[1] == 0
				&& bits[2] == 0
				&& bits[3] == 0
				&& bits[4] == 0
				&& bits[5] == 0
				&& bits[6] == 0
				&& bits[7] == 0;
	}

	public VoxelVolumeKey toImmutable() {
		if (immutable) return this;

		final VoxelVolumeKey result = new VoxelVolumeKey();
		System.arraycopy(voxelBits, 0, result.voxelBits, 0, 8);
		result.immutable = true;
		result.hash = hashCode();
		return result;
	}

	private static final int X_LOW = 0b01010101;
	private static final int Y_LOW = 0b00110011;
	private static final int Z_LOW = 0b00001111;
	private static final int X_HIGH = 0b10101010;
	private static final int Y_HIGH = 0b11001100;
	private static final int Z_HIGH = 0b11110000;

	final VoxelShape build() {
		//        final long start = System.nanoTime();

		VoxelShape result = Shapes.empty();

		if (!isEmpty()) {
			if (isFull()) {
				result = Shapes.block();
			} else {
				final int count000 = count1(0, 0, 0);
				final int count001 = count1(1, 0, 0);
				final int count010 = count1(0, 1, 0);
				final int count011 = count1(1, 1, 0);

				final int count100 = count1(0, 0, 1);
				final int count101 = count1(1, 0, 1);
				final int count110 = count1(0, 1, 1);
				final int count111 = count1(1, 1, 1);

				int halves = 0;

				if (count000 == 64) {
					halves |= 0b00000001;
				}

				if (count001 == 64) {
					halves |= 0b00000010;
				}

				if (count010 == 64) {
					halves |= 0b00000100;
				}

				if (count011 == 64) {
					halves |= 0b00001000;
				}

				if (count100 == 64) {
					halves |= 0b00010000;
				}

				if (count101 == 64) {
					halves |= 0b00100000;
				}

				if (count110 == 64) {
					halves |= 0b01000000;
				}

				if (count111 == 64) {
					halves |= 0b10000000;
				}

				if ((halves & Y_LOW) == Y_LOW) {
					result = Shapes.or(result, Shapes.box(0, 0, 0, 1, 0.5, 1));
					result = div1(count010, result, 0, 1, 0);
					result = div1(count011, result, 1, 1, 0);
					result = div1(count110, result, 0, 1, 1);
					result = div1(count111, result, 1, 1, 1);
				} else if ((halves & Y_HIGH) == Y_HIGH) {
					result = Shapes.or(result, Shapes.box(0, 0.5, 0, 1, 1, 1));
					result = div1(count000, result, 0, 0, 0);
					result = div1(count001, result, 1, 0, 0);
					result = div1(count100, result, 0, 0, 1);
					result = div1(count101, result, 1, 0, 1);
				} else if ((halves & X_LOW) == X_LOW) {
					result = Shapes.or(result, Shapes.box(0, 0, 0, 0.5, 1, 1));
					result = div1(count001, result, 1, 0, 0);
					result = div1(count011, result, 1, 1, 0);
					result = div1(count101, result, 1, 0, 1);
					result = div1(count111, result, 1, 1, 1);
				} else if ((halves & X_HIGH) == X_HIGH) {
					result = Shapes.or(result, Shapes.box(0.5, 0, 0, 1, 1, 1));
					result = div1(count000, result, 0, 0, 0);
					result = div1(count010, result, 0, 1, 0);
					result = div1(count100, result, 0, 0, 1);
					result = div1(count110, result, 0, 1, 1);
				} else if ((halves & Z_LOW) == Z_LOW) {
					result = Shapes.or(result, Shapes.box(0, 0, 0, 1, 1, 0.5));
					result = div1(count100, result, 0, 0, 1);
					result = div1(count101, result, 1, 0, 1);
					result = div1(count110, result, 0, 1, 1);
					result = div1(count111, result, 1, 1, 1);
				} else if ((halves & Z_HIGH) == Z_HIGH) {
					result = Shapes.or(result, Shapes.box(0, 0, 0.5, 1, 1, 1));
					result = div1(count000, result, 0, 0, 0);
					result = div1(count001, result, 1, 0, 0);
					result = div1(count010, result, 0, 1, 0);
					result = div1(count011, result, 1, 1, 0);
				} else {
					// no halves
					result = div1(count000, result, 0, 0, 0);
					result = div1(count001, result, 1, 0, 0);
					result = div1(count010, result, 0, 1, 0);
					result = div1(count011, result, 1, 1, 0);
					result = div1(count100, result, 0, 0, 1);
					result = div1(count101, result, 1, 0, 1);
					result = div1(count110, result, 0, 1, 1);
					result = div1(count111, result, 1, 1, 1);
				}
			}
		}

		//        System.out.println("built shape in " + (System.nanoTime() - start) / 1000000 + "ms");

		return result;
	}

	private static final double DIV1 = 1.0 / 2.0;

	private VoxelShape div1(int count, VoxelShape shape, int x, int y, int z) {
		if (count == 0) {
			return shape;
		} else if (count == 64) {
			final double x0 = x * DIV1;
			final double y0 = y * DIV1;
			final double z0 = z * DIV1;
			return Shapes.or(shape, Shapes.box(x0, y0, z0, x0 + DIV1, y0 + DIV1, z0 + DIV1));
		} else {
			final int x0 = x * 2;
			final int y0 = y * 2;
			final int z0 = z * 2;
			final int x1 = x0 + 1;
			final int y1 = y0 + 1;
			final int z1 = z0 + 1;

			shape = div2(shape, x0, y0, z0);
			shape = div2(shape, x0, y0, z1);
			shape = div2(shape, x0, y1, z0);
			shape = div2(shape, x0, y1, z1);
			shape = div2(shape, x1, y0, z0);
			shape = div2(shape, x1, y0, z1);
			shape = div2(shape, x1, y1, z0);
			shape = div2(shape, x1, y1, z1);
			return shape;
		}
	}

	private static final double DIV2 = 1.0 / 4.0;

	private VoxelShape div2(VoxelShape shape, int x, int y, int z) {
		final int count = count2(x, y, z);

		if (count == 0) {
			return shape;
		} else if (count == 8) {
			final double x0 = x * DIV2;
			final double y0 = y * DIV2;
			final double z0 = z * DIV2;
			return Shapes.or(shape, Shapes.box(x0, y0, z0, x0 + DIV2, y0 + DIV2, z0 + DIV2));
		} else {
			final int x0 = x * 2;
			final int y0 = y * 2;
			final int z0 = z * 2;
			final int x1 = x0 + 1;
			final int y1 = y0 + 1;
			final int z1 = z0 + 1;

			shape = div3(shape, x0, y0, z0);
			shape = div3(shape, x0, y0, z1);
			shape = div3(shape, x0, y1, z0);
			shape = div3(shape, x0, y1, z1);
			shape = div3(shape, x1, y0, z0);
			shape = div3(shape, x1, y0, z1);
			shape = div3(shape, x1, y1, z0);
			shape = div3(shape, x1, y1, z1);
			return shape;
		}
	}

	private static final double DIV3 = 1.0 / 8.0;

	private VoxelShape div3(VoxelShape shape, int x, int y, int z) {
		if (get(x, y, z)) {
			final double x0 = x * DIV3;
			final double y0 = y * DIV3;
			final double z0 = z * DIV3;
			return Shapes.or(shape, Shapes.box(x0, y0, z0, x0 + DIV3, y0 + DIV3, z0 + DIV3));
		} else {
			return shape;
		}
	}
}
