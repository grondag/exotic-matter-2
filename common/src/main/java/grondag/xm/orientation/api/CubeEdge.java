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

import static grondag.xm.orientation.api.ClockwiseRotation.ROTATE_180;
import static grondag.xm.orientation.api.ClockwiseRotation.ROTATE_270;
import static grondag.xm.orientation.api.ClockwiseRotation.ROTATE_90;
import static grondag.xm.orientation.api.ClockwiseRotation.ROTATE_NONE;

import java.util.Locale;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.util.StringRepresentable;

import grondag.xm.orientation.impl.CubeEdgeHelper;

/**
 * Defines the twelve edges of a block and the relative position of neighboring
 * blocks diagonally adjacent to those edges. Use when shape is symmetrical with
 * respect to that edge.
 */
@Experimental
public enum CubeEdge implements StringRepresentable {
	DOWN_SOUTH(Direction.DOWN, Direction.SOUTH, ROTATE_180),
	DOWN_WEST(Direction.DOWN, Direction.WEST, ROTATE_270),
	DOWN_NORTH(Direction.DOWN, Direction.NORTH, ROTATE_NONE),
	DOWN_EAST(Direction.DOWN, Direction.EAST, ROTATE_90),
	UP_NORTH(Direction.UP, Direction.NORTH, ROTATE_180),
	UP_EAST(Direction.UP, Direction.EAST, ROTATE_90),
	UP_SOUTH(Direction.UP, Direction.SOUTH, ROTATE_NONE),
	UP_WEST(Direction.UP, Direction.WEST, ROTATE_270),
	NORTH_EAST(Direction.NORTH, Direction.EAST, ROTATE_90),
	NORTH_WEST(Direction.NORTH, Direction.WEST, ROTATE_270),
	SOUTH_EAST(Direction.SOUTH, Direction.EAST, ROTATE_270),
	SOUTH_WEST(Direction.SOUTH, Direction.WEST, ROTATE_90);

	public final Direction face1;
	public final Direction face2;

	/**
	 * Used to position models like stairs/wedges. Representation rotation around
	 * the parallel axis such that face1 and face2 are most occluded. Based on
	 * "default" model occluding north and down faces. Use the axis implied by
	 * face1.
	 */

	public final ClockwiseRotation rotation;
	private final String serializedName;
	public final Vec3i vector;

	/**
	 * Ordinal sequence that includes all faces, corner and far corners. Used to
	 * index them in a mixed array.
	 */
	@Internal
	public final int superOrdinal;

	@Internal
	public final int superOrdinalBit;

	/**
	 * Will be null if not a horizontal edge.
	 */
	@Nullable
	public final HorizontalEdge horizontalEdge;

	CubeEdge(Direction face1, Direction face2, ClockwiseRotation rotation) {
		serializedName = name().toLowerCase(Locale.ROOT);
		this.face1 = face1;
		this.face2 = face2;
		this.rotation = rotation;
		superOrdinal = 6 + ordinal();
		superOrdinalBit = 1 << superOrdinal;

		final Vec3i v1 = face1.getNormal();
		final Vec3i v2 = face2.getNormal();
		vector = new Vec3i(v1.getX() + v2.getX(), v1.getY() + v2.getY(), v1.getZ() + v2.getZ());

		if (face1.getAxis() == Axis.Y || face2.getAxis() == Axis.Y) {
			horizontalEdge = null;
		} else {
			horizontalEdge = HorizontalEdge.find(HorizontalFace.find(face1), HorizontalFace.find(face2));
		}
	}

	public static final int COUNT = CubeEdgeHelper.COUNT;

	/**
	 * Will be null if the inputs do not specify an edge.
	 */
	@Nullable
	public static CubeEdge find(Direction face1, Direction face2) {
		return CubeEdgeHelper.find(face1, face2);
	}

	public static CubeEdge fromOrdinal(int ordinal) {
		return CubeEdgeHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<CubeEdge> consumer) {
		CubeEdgeHelper.forEach(consumer);
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}
}
