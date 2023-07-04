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

package grondag.xm.api.connect.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Integer version of the vanilla MC AABB class.
 */
public class IntegerBox {
	public static final IntegerBox FULL_BLOCK_BOX = new IntegerBox(0, 0, 0, 1, 1, 1);

	public static class Builder {
		private int minX;
		private int minY;
		private int minZ;
		private int maxX;
		private int maxY;
		private int maxZ;
		private boolean seenFirst = false;

		public void add(int x1, int y1, int z1, int x2, int y2, int z2) {
			if (seenFirst) {
				minX = (minX < x1) ? (minX < x2 ? minX : x2) : (x1 < x2 ? x1 : x2);
				minY = (minY < y1) ? (minY < y2 ? minY : y2) : (y1 < y2 ? y1 : y2);
				minZ = (minZ < z1) ? (minZ < z2 ? minZ : z2) : (z1 < z2 ? z1 : z2);
				maxX = (maxX > x1) ? (maxX > x2 ? maxX : x2) : (x1 > x2 ? x1 : x2);
				maxY = (maxY > y1) ? (maxY > y2 ? maxY : y2) : (y1 > y2 ? y1 : y2);
				maxZ = (maxZ > z1) ? (maxZ > z2 ? maxZ : z2) : (z1 > z2 ? z1 : z2);
			} else {
				seenFirst = true;
				minX = (x1 <= x2) ? x1 : x2;
				minY = (y1 <= y2) ? y1 : y2;
				minZ = (z1 <= z2) ? z1 : z2;
				maxX = (x1 >= x2) ? x1 : x2;
				maxY = (y1 >= y2) ? y1 : y2;
				maxZ = (z1 >= z2) ? z1 : z2;
			}
		}

		public void add(BlockPos pos) {
			this.add(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
		}

		public IntegerBox build() {
			return seenFirst ? new IntegerBox(minX, minY, minZ, maxX, maxY, maxZ)
				: null;
		}
	}

	protected final int minX;
	protected final int minY;
	protected final int minZ;
	protected final int maxX;
	protected final int maxY;
	protected final int maxZ;

	public IntegerBox(int x1, int y1, int z1, int x2, int y2, int z2) {
		minX = Math.min(x1, x2);
		minY = Math.min(y1, y2);
		minZ = Math.min(z1, z2);
		maxX = Math.max(x1, x2);
		maxY = Math.max(y1, y2);
		maxZ = Math.max(z1, z2);
	}

	public IntegerBox(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
	}

	/**
	 * Differs from normal AABB in that it ensures the AABB includes both positions.
	 * Order does not matter.
	 */
	public IntegerBox(BlockPos pos1, BlockPos pos2) {
		minX = Math.min(pos1.getX(), pos2.getX());
		minY = Math.min(pos1.getY(), pos2.getY());
		minZ = Math.min(pos1.getZ(), pos2.getZ());
		maxX = Math.max(pos1.getX(), pos2.getX()) + 1;
		maxY = Math.max(pos1.getY(), pos2.getY()) + 1;
		maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1;
	}

	private AABB aabb = null;

	public AABB toAABB() {
		AABB result = aabb;

		if (result == null) {
			result = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
			aabb = result;
		}

		return result;
	}

	private BlockPos minPos = null;

	public BlockPos minPos() {
		BlockPos result = minPos;

		if (result == null) {
			result = new BlockPos(minX, minY, minZ);
			minPos = result;
		}

		return result;
	}

	private BlockPos maxPos = null;

	public BlockPos maxPos() {
		BlockPos result = maxPos;

		if (result == null) {
			result = new BlockPos(maxX - 1, maxY - 1, maxZ - 1);
			maxPos = result;
		}

		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (this == other) {
			return true;
		} else if (!(other instanceof final IntegerBox otherBox)) {
			return false;
		} else {
			return minX == otherBox.minX & minY == otherBox.minY & minZ == otherBox.minZ
				& maxX == otherBox.maxX & maxY == otherBox.maxY & maxZ == otherBox.maxZ;
		}
	}

	@Override
	public int hashCode() {
		long i = minX;
		int j = (int) (i ^ i >>> 32);
		i = minY;
		j = 31 * j + (int) (i ^ i >>> 32);
		i = minZ;
		j = 31 * j + (int) (i ^ i >>> 32);
		i = maxX;
		j = 31 * j + (int) (i ^ i >>> 32);
		i = maxY;
		j = 31 * j + (int) (i ^ i >>> 32);
		i = maxZ;
		j = 31 * j + (int) (i ^ i >>> 32);
		return j;
	}

	/**
	 * Creates a new {@link IntegerBox} that has been contracted by the given
	 * amount, with positive changes decreasing max values and negative changes
	 * increasing min values. <br/>
	 * If the amount to contract by is larger than the length of a side, then the
	 * side will wrap.
	 *
	 * @return A new modified bounding box.
	 */
	public IntegerBox contract(int x, int y, int z) {
		int d0 = minX;
		int d1 = minY;
		int d2 = minZ;
		int d3 = maxX;
		int d4 = maxY;
		int d5 = maxZ;

		if (x < 0.0D) {
			d0 -= x;
		} else if (x > 0.0D) {
			d3 -= x;
		}

		if (y < 0.0D) {
			d1 -= y;
		} else if (y > 0.0D) {
			d4 -= y;
		}

		if (z < 0.0D) {
			d2 -= z;
		} else if (z > 0.0D) {
			d5 -= z;
		}

		return new IntegerBox(d0, d1, d2, d3, d4, d5);
	}

	/**
	 * Creates a new {@link IntegerBox} that has been expanded by the given amount,
	 * with positive changes increasing max values and negative changes decreasing
	 * min values.
	 *
	 * @return A modified bounding box that will always be equal or greater in
	 *         volume to this bounding box.
	 */
	public IntegerBox expand(int x, int y, int z) {
		int d0 = minX;
		int d1 = minY;
		int d2 = minZ;
		int d3 = maxX;
		int d4 = maxY;
		int d5 = maxZ;

		if (x < 0.0D) {
			d0 += x;
		} else if (x > 0.0D) {
			d3 += x;
		}

		if (y < 0.0D) {
			d1 += y;
		} else if (y > 0.0D) {
			d4 += y;
		}

		if (z < 0.0D) {
			d2 += z;
		} else if (z > 0.0D) {
			d5 += z;
		}

		return new IntegerBox(d0, d1, d2, d3, d4, d5);
	}

	/**
	 * Creates a new {@link IntegerBox} that has been contracted by the given
	 * amount in both directions. Negative values will shrink the AABB instead of
	 * expanding it. <br/>
	 * Side lengths will be increased by 2 times the value of the parameters, since
	 * both min and max are changed. <br/>
	 * If contracting and the amount to contract by is larger than the length of a
	 * side, then the side will wrap.
	 *
	 * @return A modified bounding box.
	 */
	public IntegerBox grow(int x, int y, int z) {
		final int d0 = minX - x;
		final int d1 = minY - y;
		final int d2 = minZ - z;
		final int d3 = maxX + x;
		final int d4 = maxY + y;
		final int d5 = maxZ + z;
		return new IntegerBox(d0, d1, d2, d3, d4, d5);
	}

	/**
	 * Creates a new {@link IntegerBox} that is expanded by the given value in all
	 * directions. Equivalent to {@link #grow(int, int, int)} with the given value
	 * for all 3 params. Negative values will shrink the AABB. <br/>
	 * Side lengths will be increased by 2 times the value of the parameter, since
	 * both min and max are changed. <br/>
	 * If contracting and the amount to contract by is larger than the length of a
	 * side, then the side will wrap.
	 *
	 * @return A modified AABB.
	 */
	public IntegerBox grow(int value) {
		return this.grow(value, value, value);
	}

	public IntegerBox intersect(IntegerBox other) {
		final int d0 = Math.max(minX, other.minX);
		final int d1 = Math.max(minY, other.minY);
		final int d2 = Math.max(minZ, other.minZ);
		final int d3 = Math.min(maxX, other.maxX);
		final int d4 = Math.min(maxY, other.maxY);
		final int d5 = Math.min(maxZ, other.maxZ);
		return new IntegerBox(d0, d1, d2, d3, d4, d5);
	}

	public IntegerBox union(IntegerBox other) {
		final int d0 = Math.min(minX, other.minX);
		final int d1 = Math.min(minY, other.minY);
		final int d2 = Math.min(minZ, other.minZ);
		final int d3 = Math.max(maxX, other.maxX);
		final int d4 = Math.max(maxY, other.maxY);
		final int d5 = Math.max(maxZ, other.maxZ);
		return new IntegerBox(d0, d1, d2, d3, d4, d5);
	}

	/**
	 * Offsets the current bounding box by the specified amount.
	 */
	public IntegerBox offset(int x, int y, int z) {
		return new IntegerBox(minX + x, minY + y, minZ + z, maxX + x, maxY + y,
			maxZ + z);
	}

	public IntegerBox offset(BlockPos pos) {
		return new IntegerBox(minX + pos.getX(), minY + pos.getY(), minZ + pos.getZ(),
			maxX + pos.getX(), maxY + pos.getY(), maxZ + pos.getZ());
	}

	public IntegerBox offset(Vec3i vec) {
		return this.offset(vec.getX(), vec.getY(), vec.getZ());
	}

	/**
	 * if instance and the argument bounding boxes overlap in the Y and Z
	 * dimensions, calculate the offset between them in the X dimension. return var2
	 * if the bounding boxes do not overlap or if var2 is closer to 0 then the
	 * calculated offset. Otherwise return the calculated offset.
	 */
	public int calculateXOffset(IntegerBox other, int offsetX) {
		if (other.maxY > minY && other.minY < maxY && other.maxZ > minZ && other.minZ < maxZ) {
			if (offsetX > 0.0D && other.maxX <= minX) {
				final int d1 = minX - other.maxX;

				if (d1 < offsetX) {
					offsetX = d1;
				}
			} else if (offsetX < 0.0D && other.minX >= maxX) {
				final int d0 = maxX - other.minX;

				if (d0 > offsetX) {
					offsetX = d0;
				}
			}

			return offsetX;
		} else {
			return offsetX;
		}
	}

	/**
	 * if instance and the argument bounding boxes overlap in the X and Z
	 * dimensions, calculate the offset between them in the Y dimension. return var2
	 * if the bounding boxes do not overlap or if var2 is closer to 0 then the
	 * calculated offset. Otherwise return the calculated offset.
	 */
	public int calculateYOffset(IntegerBox other, int offsetY) {
		if (other.maxX > minX && other.minX < maxX && other.maxZ > minZ && other.minZ < maxZ) {
			if (offsetY > 0.0D && other.maxY <= minY) {
				final int d1 = minY - other.maxY;

				if (d1 < offsetY) {
					offsetY = d1;
				}
			} else if (offsetY < 0.0D && other.minY >= maxY) {
				final int d0 = maxY - other.minY;

				if (d0 > offsetY) {
					offsetY = d0;
				}
			}
		}

		return offsetY;
	}

	/**
	 * if instance and the argument bounding boxes overlap in the Y and X
	 * dimensions, calculate the offset between them in the Z dimension. return var2
	 * if the bounding boxes do not overlap or if var2 is closer to 0 then the
	 * calculated offset. Otherwise return the calculated offset.
	 */
	public int calculateZOffset(IntegerBox other, int offsetZ) {
		if (other.maxX > minX && other.minX < maxX && other.maxY > minY && other.minY < maxY) {
			if (offsetZ > 0.0D && other.maxZ <= minZ) {
				final int d1 = minZ - other.maxZ;

				if (d1 < offsetZ) {
					offsetZ = d1;
				}
			} else if (offsetZ < 0.0D && other.minZ >= maxZ) {
				final int d0 = maxZ - other.minZ;

				if (d0 > offsetZ) {
					offsetZ = d0;
				}
			}
		}

		return offsetZ;
	}

	/**
	 * Checks if the bounding box intersects with another.
	 */
	public boolean intersects(IntegerBox other) {
		return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
	}

	public boolean intersects(int x1, int y1, int z1, int x2, int y2, int z2) {
		return minX < x2 && maxX > x1 && minY < y2 && maxY > y1 && minZ < z2 && maxZ > z1;
	}

	/**
	 * Returns if the supplied Vec3D is completely inside the bounding box.
	 */
	public boolean contains(Vec3 vec) {
		if (vec.x > minX && vec.x < maxX) {
			if (vec.y > minY && vec.y < maxY) {
				return vec.z > minZ && vec.z < maxZ;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns if the supplied BlockPos is include inside the bounding box.
	 */
	public boolean contains(BlockPos pos) {
		if (pos.getX() >= minX && pos.getX() < maxX) {
			if (pos.getY() >= minY && pos.getY() < maxY) {
				return pos.getZ() >= minZ && pos.getZ() < maxZ;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns the average length of the edges of the bounding box.
	 */
	public double getAverageEdgeLength() {
		final int d0 = maxX - minX;
		final int d1 = maxY - minY;
		final int d2 = maxZ - minZ;
		return (d0 + d1 + d2) / 3.0D;
	}

	/**
	 * Creates a new {@link IntegerBox} that is expanded by the given value in all
	 * directions. Equivalent to {@link #grow(int)} with value set to the negative
	 * of the value provided here. Passing a negative value to this method values
	 * will grow the AABB. <br/>
	 * Side lengths will be decreased by 2 times the value of the parameter, since
	 * both min and max are changed. <br/>
	 * If contracting and the amount to contract by is larger than the length of a
	 * side, then the side will wrap.
	 *
	 * @return A modified AABB.
	 */
	public IntegerBox shrink(int value) {
		return this.grow(-value);
	}

	@Override
	public String toString() {
		return "box[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", "
			+ maxZ + "]";
	}

	public BlockPos getCenter() {
		return BlockPos.containing(minX + (maxX - minX) * 0.5D, minY + (maxY - minY) * 0.5D,
			minZ + (maxZ - minZ) * 0.5D);
	}
}
