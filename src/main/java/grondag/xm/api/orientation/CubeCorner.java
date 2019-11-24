/*******************************************************************************
 * Copyright 2019 grondag
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package grondag.xm.api.orientation;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.apiguardian.api.API;

import grondag.xm.connect.helper.CubeCornerHelper;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

/**
 * Defines the eight corners of a block and the relative positions of the
 * neighboring blocks diagonally adjacent to those corners.
 */
@API(status = EXPERIMENTAL)
public enum CubeCorner implements StringIdentifiable {
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
	public final String name;

	/**
	 * Ordinal sequence that includes all faces, corner and far corners. Use to
	 * index them in a mixed array.
	 */
	@API(status = INTERNAL)
	public final int superOrdinal;
	@API(status = INTERNAL)
	public final int superOrdinalBit;

	private CubeCorner(Direction face1, Direction face2, Direction face3) {
		name = name().toLowerCase();
		this.face1 = face1;
		this.face2 = face2;
		this.face3 = face3;

		// 6 is number of possible faces
		superOrdinal = 6 + ordinal() + CubeEdge.values().length;
		superOrdinalBit = 1 << superOrdinal;

		final Vec3i v1 = face1.getVector();
		final Vec3i v2 = face2.getVector();
		final Vec3i v3 = face3.getVector();
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

	public static final CubeCorner fromOrdinal(int ordinal) {
		return CubeCornerHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<CubeCorner> consumer) {
		CubeCornerHelper.forEach(consumer);
	}

	@Override
	public String asString() {
		return name;
	}

	public CubeCorner rotate(BlockRotation rotation) {
		final Direction face1 = rotation.rotate(this.face1);
		final Direction face2 = rotation.rotate(this.face2);
		final Direction face3 = rotation.rotate(this.face3);
		return ObjectUtils.defaultIfNull(find(face1, face2, face3), this);
	}
}
