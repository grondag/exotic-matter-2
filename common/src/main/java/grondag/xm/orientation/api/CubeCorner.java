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

import java.util.function.Consumer;

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;

import grondag.xm.orientation.impl.CubeCornerHelper;

/**
 * Defines the eight corners of a block and the relative positions of the
 * neighboring blocks diagonally adjacent to those corners.
 */
@Experimental
public enum CubeCorner {
	UP_NORTH_EAST(Direction.UP, Direction.EAST, Direction.NORTH),
	UP_NORTH_WEST(Direction.UP, Direction.WEST, Direction.NORTH),
	UP_SOUTH_EAST(Direction.UP, Direction.EAST, Direction.SOUTH),
	UP_SOUTH_WEST(Direction.UP, Direction.WEST, Direction.SOUTH),
	DOWN_NORTH_EAST(Direction.DOWN, Direction.EAST, Direction.NORTH),
	DOWN_NORTH_WEST(Direction.DOWN, Direction.WEST, Direction.NORTH),
	DOWN_SOUTH_EAST(Direction.DOWN, Direction.EAST, Direction.SOUTH),
	DOWN_SOUTH_WEST(Direction.DOWN, Direction.WEST, Direction.SOUTH);

	public final Direction face1;
	public final Direction face2;
	public final Direction face3;
	public final Vec3i vector;

	/**
	 * Ordinal sequence that includes all faces, corner and far corners. Use to
	 * index them in a mixed array.
	 */
	@Internal
	public final int superOrdinal;
	@Internal
	public final int superOrdinalBit;

	CubeCorner(Direction face1, Direction face2, Direction face3) {
		this.face1 = face1;
		this.face2 = face2;
		this.face3 = face3;

		// 6 is number of possible faces
		superOrdinal = 6 + ordinal() + CubeEdge.values().length;
		superOrdinalBit = 1 << superOrdinal;

		final Vec3i v1 = face1.getNormal();
		final Vec3i v2 = face2.getNormal();
		final Vec3i v3 = face3.getNormal();
		vector = new Vec3i(v1.getX() + v2.getX() + v3.getX(), v1.getY() + v2.getY() + v3.getY(), v1.getZ() + v2.getZ() + v3.getZ());
	}

	public static final int COUNT = CubeCornerHelper.COUNT;

	/**
	 * Will return null if the given inputs do not specify a corner.
	 */
	@Nullable
	public static CubeCorner find(Direction face1, Direction face2, Direction face3) {
		return CubeCornerHelper.find(face1, face2, face3);
	}

	public static CubeCorner fromOrdinal(int ordinal) {
		return CubeCornerHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<CubeCorner> consumer) {
		CubeCornerHelper.forEach(consumer);
	}

	public CubeCorner rotate(Rotation rotation) {
		final Direction face1 = rotation.rotate(this.face1);
		final Direction face2 = rotation.rotate(this.face2);
		final Direction face3 = rotation.rotate(this.face3);
		return ObjectUtils.defaultIfNull(find(face1, face2, face3), this);
	}
}
