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

import java.util.Locale;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

@Experimental
public enum ClockwiseRotation implements StringRepresentable {
	ROTATE_NONE(0),
	ROTATE_90(90),
	ROTATE_180(180),
	ROTATE_270(270);

	public final String name;

	/**
	 * Useful for locating model file names that use degrees as a suffix.
	 */
	public final int degrees;

	/**
	 * Opposite of degress - useful for GL transforms. 0 and 180 are same, 90 and
	 * 270 are flipped
	 */
	public final int degreesInverse;

	ClockwiseRotation(int degrees) {
		name = name().toLowerCase(Locale.ROOT);
		this.degrees = degrees;
		degreesInverse = (360 - degrees) % 360;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public ClockwiseRotation clockwise() {
		switch (this) {
			case ROTATE_180:
				return ROTATE_270;
			case ROTATE_270:
				return ROTATE_NONE;
			case ROTATE_90:
				return ROTATE_180;
			case ROTATE_NONE:
			default:
				return ROTATE_90;
		}
	}

	public ClockwiseRotation clockwise(int offset) {
		int ord = (ordinal() + offset) % COUNT;

		if (ord < 0) {
			ord += COUNT;
		}

		return VALUES[ord];
	}

	private static final ClockwiseRotation[] VALUES = ClockwiseRotation.values();
	public static final int COUNT = VALUES.length;
	private static ClockwiseRotation[] FROM_HORIZONTAL_FACING = new ClockwiseRotation[6];

	static {
		FROM_HORIZONTAL_FACING[Direction.NORTH.ordinal()] = ROTATE_180;
		FROM_HORIZONTAL_FACING[Direction.EAST.ordinal()] = ROTATE_270;
		FROM_HORIZONTAL_FACING[Direction.SOUTH.ordinal()] = ROTATE_NONE;
		FROM_HORIZONTAL_FACING[Direction.WEST.ordinal()] = ROTATE_90;
		FROM_HORIZONTAL_FACING[Direction.UP.ordinal()] = ROTATE_NONE;
		FROM_HORIZONTAL_FACING[Direction.DOWN.ordinal()] = ROTATE_NONE;
	}

	/**
	 * Gives the rotation with horiztonalFace matching the given NSEW face For up
	 * and down will return ROTATE_NONE.
	 */
	public static ClockwiseRotation fromHorizontalFacing(Direction face) {
		return FROM_HORIZONTAL_FACING[face.ordinal()];
	}

	public static ClockwiseRotation fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<ClockwiseRotation> consumer) {
		for (final ClockwiseRotation val : VALUES) {
			consumer.accept(val);
		}
	}
}
