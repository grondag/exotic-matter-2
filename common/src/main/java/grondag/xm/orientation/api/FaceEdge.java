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

import java.util.Locale;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import grondag.xm.orientation.impl.FaceEdgeHelper;

@Internal
public enum FaceEdge implements StringRepresentable {
	TOP_EDGE(SOUTH, NORTH, UP, UP, UP, UP),
	BOTTOM_EDGE(NORTH, SOUTH, DOWN, DOWN, DOWN, DOWN),
	LEFT_EDGE(WEST, WEST, EAST, WEST, NORTH, SOUTH),
	RIGHT_EDGE(EAST, EAST, WEST, EAST, SOUTH, NORTH);

	// for a given face, which face is at the position identified by this enum?
	private final Direction[] relativeLookup;

	@Internal
	public final int ordinalBit;

	public final String name;

	FaceEdge(Direction... relativeLookup) {
		name = name().toLowerCase(Locale.ROOT);
		this.relativeLookup = relativeLookup;
		ordinalBit = 1 << ordinal();
	}

	public FaceEdge clockwise() {
		switch (this) {
			case BOTTOM_EDGE:
				return LEFT_EDGE;
			case LEFT_EDGE:
				return TOP_EDGE;
			case RIGHT_EDGE:
				return BOTTOM_EDGE;
			case TOP_EDGE:
				return RIGHT_EDGE;
			default:
				return null;
		}
	}

	public FaceEdge counterClockwise() {
		switch (this) {
			case BOTTOM_EDGE:
				return RIGHT_EDGE;
			case LEFT_EDGE:
				return BOTTOM_EDGE;
			case RIGHT_EDGE:
				return TOP_EDGE;
			case TOP_EDGE:
				return LEFT_EDGE;
			default:
				return null;
		}
	}

	public FaceEdge opposite() {
		switch (this) {
			case BOTTOM_EDGE:
				return TOP_EDGE;
			case LEFT_EDGE:
				return RIGHT_EDGE;
			case RIGHT_EDGE:
				return LEFT_EDGE;
			case TOP_EDGE:
				return BOTTOM_EDGE;
			default:
				return null;
		}
	}

	/**
	 * Returns the block face next to this FaceSide on the given block face.
	 */
	public Direction toWorld(Direction face) {
		return relativeLookup[face.ordinal()];
	}

	/**
	 * Determines if the given sideFace is TOP, BOTTOM, DEFAULT_LEFT or
	 * DEFAULT_RIGHT of onFace. If none (sideFace on same orthogonalAxis as onFace),
	 */
	@Nullable
	public static FaceEdge fromWorld(Direction edgeFace, Direction onFace) {
		return FaceEdgeHelper.fromWorld(edgeFace, onFace);
	}

	public static final int COUNT = FaceEdgeHelper.COUNT;

	public static FaceEdge fromOrdinal(int ordinal) {
		return FaceEdgeHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<FaceEdge> consumer) {
		FaceEdgeHelper.forEach(consumer);
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public boolean isHorizontal() {
		return this == LEFT_EDGE || this == RIGHT_EDGE;
	}

	public boolean isVertical() {
		return this == TOP_EDGE || this == BOTTOM_EDGE;
	}
}
