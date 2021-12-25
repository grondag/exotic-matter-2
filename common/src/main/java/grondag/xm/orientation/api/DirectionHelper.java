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

package grondag.xm.orientation.api;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

import net.minecraft.core.Direction;

/**
 * Based on vanilla methods but reliably public and available server-side.
 */
public class DirectionHelper {
	private DirectionHelper() { }

	public static final int UP_BIT = 1 << Direction.UP.ordinal();
	public static final int DOWN_BIT = 1 << Direction.DOWN.ordinal();
	public static final int EAST_BIT = 1 << Direction.EAST.ordinal();
	public static final int WEST_BIT = 1 << Direction.WEST.ordinal();
	public static final int NORTH_BIT = 1 << Direction.NORTH.ordinal();
	public static final int SOUTH_BIT = 1 << Direction.SOUTH.ordinal();

	public static Direction clockwise(Direction face, Direction.Axis axis) {
		switch (axis) {
			case X:
				return (face != WEST && face != EAST) ? rotateXClockwise(face) : face;
			case Y:
				return (face != UP && face != DOWN) ? rotateYClockwise(face) : face;
			case Z:
				return (face != NORTH && face != SOUTH) ? rotateZClockwise(face) : face;
			default:
				throw new IllegalStateException("Unable to get CW facing for axis " + axis);
		}
	}

	public static Direction counterClockwise(Direction face, Direction.Axis axis) {
		return clockwise(face.getOpposite(), axis);
	}

	public static Direction rotateYClockwise(Direction face) {
		switch (face) {
			case NORTH:
				return EAST;
			case EAST:
				return SOUTH;
			case SOUTH:
				return WEST;
			case WEST:
				return NORTH;
			default:
				throw new IllegalStateException("Unable to get Y-rotated facing of " + face);
		}
	}

	public static Direction rotateXClockwise(Direction face) {
		switch (face) {
			case NORTH:
				return DOWN;
			case EAST:
			case WEST:
			default:
				throw new IllegalStateException("Unable to get X-rotated facing of " + face);
			case SOUTH:
				return UP;
			case UP:
				return NORTH;
			case DOWN:
				return SOUTH;
		}
	}

	public static Direction rotateZClockwise(Direction face) {
		switch (face) {
			case EAST:
				return DOWN;
			case SOUTH:
			default:
				throw new IllegalStateException("Unable to get Z-rotated facing of " + face);
			case WEST:
				return UP;
			case UP:
				return EAST;
			case DOWN:
				return WEST;
		}
	}

	public static Direction rotateYCounterclockwise(Direction face) {
		switch (face) {
			case NORTH:
				return WEST;
			case EAST:
				return NORTH;
			case SOUTH:
				return EAST;
			case WEST:
				return SOUTH;
			default:
				throw new IllegalStateException("Unable to get CCW facing of " + face);
		}
	}
}
