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

package grondag.xm.api.util;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class WorldHelper {
	public static int horizontalDistanceSquared(BlockPos pos1, BlockPos pos2) {
		final int dx = pos1.getX() - pos2.getX();
		final int dz = pos1.getZ() - pos2.getZ();
		return dx * dx + dz * dz;
	}

	/**
	 * Sorts members of the BlockPos vector so that x is largest and z is smallest.
	 * Useful when BlockPos represents a volume instead of a position.
	 */
	public static BlockPos sortedBlockPos(BlockPos pos) {
		if (pos.getX() > pos.getY()) {
			if (pos.getY() > pos.getZ()) {
				// x > y > z
				return pos;
			} else if (pos.getX() > pos.getZ()) {
				// x > z > y
				return new BlockPos(pos.getX(), pos.getZ(), pos.getY());
			} else {
				// z > x > y
				return new BlockPos(pos.getZ(), pos.getX(), pos.getY());
			}
		} else if (pos.getX() > pos.getZ()) {
			// y > x > z
			return new BlockPos(pos.getY(), pos.getX(), pos.getY());
		} else if (pos.getY() > pos.getZ()) {
			// y > z > x
			return new BlockPos(pos.getY(), pos.getZ(), pos.getX());
		} else {
			// z > y >x
			return new BlockPos(pos.getZ(), pos.getY(), pos.getX());
		}
	}

	public static Direction closestAdjacentFace(Direction hitFace, double hitX, double hitY, double hitZ) {
		return closestAdjacentFace(hitFace, (float) hitX, (float) hitY, (float) hitZ);
	}

	/**
	 * Returns the closest face adjacent to the hit face that is closest to the hit
	 * location on the given face. There is probably a better way to do this. TBH, I
	 * may have been drinking when this code was written.
	 */
	public static Direction closestAdjacentFace(Direction hitFace, float hitX, float hitY, float hitZ) {
		switch (hitFace.getAxis()) {
			case X: {
				// absolute distance from center of the face along the orthogonalAxis
				final float yDist = 0.5F - hitY + Mth.floor(hitY);
				final float zDist = 0.5F - hitZ + Mth.floor(hitZ);

				if (Math.abs(yDist) > Math.abs(zDist)) {
					return yDist < 0 ? Direction.UP : Direction.DOWN;
				} else {
					return zDist < 0 ? Direction.SOUTH : Direction.NORTH;
				}
			}

			case Y: {
				// absolute distance from center of the face along the orthogonalAxis
				final float xDist = 0.5F - hitX + Mth.floor(hitX);
				final float zDist = 0.5F - hitZ + Mth.floor(hitZ);

				if (Math.abs(xDist) > Math.abs(zDist)) {
					return xDist < 0 ? Direction.EAST : Direction.WEST;
				} else {
					return zDist < 0 ? Direction.SOUTH : Direction.NORTH;
				}
			}

			case Z: {
				// absolute distance from center of the face along the orthogonalAxis
				final float yDist = 0.5F - hitY + Mth.floor(hitY);
				final float xDist = 0.5F - hitX + Mth.floor(hitX);

				if (Math.abs(yDist) > Math.abs(xDist)) {
					return yDist < 0 ? Direction.UP : Direction.DOWN;
				} else {
					return xDist < 0 ? Direction.EAST : Direction.WEST;
				}
			}

			default:
				// whatever
				return hitFace.getClockWise();
		}
	}

	public static Pair<Direction, Direction> closestAdjacentFaces(Direction hitFace, double hitX, double hitY, double hitZ) {
		return closestAdjacentFaces(hitFace, (float) hitX, (float) hitY, (float) hitZ);
	}

	/**
	 * Returns the faces adjacent to the hit face that are closest to the hit
	 * location on the given face. First item in pair is closest, and second is...
	 * you know. These faces will necessarily be adjacent to each other.
	 *
	 * <p>Logic here is adapted from {@link closestAdjacentFace} except that I was
	 * completely sober.
	 */
	// PERF: return a CubeEdge instead of a pair
	public static Pair<Direction, Direction> closestAdjacentFaces(Direction hitFace, float hitX, float hitY,
		float hitZ) {
		switch (hitFace.getAxis()) {
			case X: {
				// absolute distance from center of the face along the orthogonalAxis
				final float yDist = 0.5F - hitY + Mth.floor(hitY);
				final float zDist = 0.5F - hitZ + Mth.floor(hitZ);
				final Direction yFace = yDist < 0 ? Direction.UP : Direction.DOWN;
				final Direction zFace = zDist < 0 ? Direction.SOUTH : Direction.NORTH;
				return Math.abs(yDist) > Math.abs(zDist) ? Pair.of(yFace, zFace) : Pair.of(zFace, yFace);
			}

			case Y: {
				// absolute distance from center of the face along the orthogonalAxis
				final float xDist = 0.5F - hitX + Mth.floor(hitX);
				final float zDist = 0.5F - hitZ + Mth.floor(hitZ);
				final Direction xFace = xDist < 0 ? Direction.EAST : Direction.WEST;
				final Direction zFace = zDist < 0 ? Direction.SOUTH : Direction.NORTH;
				return Math.abs(xDist) > Math.abs(zDist) ? Pair.of(xFace, zFace) : Pair.of(zFace, xFace);
			}

			case Z:
			// can't happen, just making compiler shut up
			default: {
				// absolute distance from center of the face along the orthogonalAxis
				final float yDist = 0.5F - hitY + Mth.floor(hitY);
				final float xDist = 0.5F - hitX + Mth.floor(hitX);
				final Direction xFace = xDist < 0 ? Direction.EAST : Direction.WEST;
				final Direction yFace = yDist < 0 ? Direction.UP : Direction.DOWN;
				return Math.abs(xDist) > Math.abs(yDist) ? Pair.of(xFace, yFace) : Pair.of(yFace, xFace);
			}
		}
	}

	/**
	 * The direction that would appear as "up" adjusted if looking at an UP or DOWN
	 * face. For example, if lookup up at the ceiling and facing North, then South
	 * would be "up." For horizontal faces, is always real up.
	 */
	public static Direction relativeUp(Player player, Direction onFace) {
		switch (onFace) {
			case DOWN:
				return player.getDirection();

			case UP:
				return player.getDirection().getOpposite();

			default:
				return Direction.UP;
		}
	}

	/**
	 * The direction that would appear as "left" adjusted if looking at an UP or
	 * DOWN face. For example, if lookup up at the ceiling and facing North, then
	 * West would be "left." For horizontal faces, is always direction left of the
	 * direction <em>opposite</em> of the given face. (Player is looking at the
	 * face, not away from it.)
	 */
	public static Direction relativeLeft(Player player, Direction onFace) {
		switch (onFace) {
			case DOWN:
				return Direction.from2DDataValue((relativeUp(player, onFace).get2DDataValue() + 1) & 0x3).getOpposite();

			case UP:
				return Direction.from2DDataValue((relativeUp(player, onFace).get2DDataValue() + 1) & 0x3);

			default:
				return Direction.from2DDataValue((player.getDirection().get2DDataValue() + 1) & 0x3).getOpposite();
		}
	}

	//    /**
	//     * Convenience method to keep code more readable.
	//     * Call with replaceVirtualBlocks = true to behave as if virtual blocks not present.
	//     * Should generally be true if placing a normal block.
	//     */
	//    public static boolean isBlockReplaceable(BlockView worldIn, BlockPos pos, boolean replaceVirtualBlocks)
	//    {
	//        if(replaceVirtualBlocks)
	//        {
	//            return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos);
	//        }
	//        else
	//        {
	//            Block block = worldIn.getBlockState(pos).getBlock();
	//            return !ISuperBlock.isVirtualBlock(block) && block.isReplaceable(worldIn, pos);
	//        }
	//
	//    }

	public static boolean isOnRenderChunkBoundary(BlockPos pos) {
		int n = pos.getX() & 0xF;

		if (n == 0 || n == 0xF) {
			return true;
		}

		n = pos.getY() & 0xF;

		if (n == 0 || n == 0xF) {
			return true;
		}

		n = pos.getZ() & 0xF;

		if (n == 0 || n == 0xF) {
			return true;
		}

		return false;
	}
}
