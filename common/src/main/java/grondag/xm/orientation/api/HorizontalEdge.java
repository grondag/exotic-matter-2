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
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;

import grondag.xm.orientation.impl.HorizontalEdgeHelper;

/**
 * A subset of {@link CubeEdge}, includes only the edges in the horizontal
 * plane.
 */
@Experimental
public enum HorizontalEdge {
	NORTH_EAST(HorizontalFace.NORTH, HorizontalFace.EAST),
	NORTH_WEST(HorizontalFace.WEST, HorizontalFace.NORTH),
	SOUTH_EAST(HorizontalFace.EAST, HorizontalFace.SOUTH),
	SOUTH_WEST(HorizontalFace.SOUTH, HorizontalFace.WEST);

	public final HorizontalFace left;
	public final HorizontalFace right;

	public final Vec3i vector;

	HorizontalEdge(HorizontalFace left, HorizontalFace right) {
		this.left = left;
		this.right = right;
		vector = new Vec3i(left.face.getNormal().getX() + right.face.getNormal().getX(), 0,
			left.face.getNormal().getZ() + right.face.getNormal().getZ());
	}

	public HorizontalEdge rotate(Rotation rotation) {
		final Direction face1 = rotation.rotate(left.face);
		final Direction face2 = rotation.rotate(right.face);
		return ObjectUtils.defaultIfNull(find(face1, face2), this);
	}

	public static final int COUNT = HorizontalEdgeHelper.COUNT;

	/**
	 * Will return null if inputs do not specify a horizontal block edge.
	 */
	@Nullable
	public static HorizontalEdge find(HorizontalFace face1, HorizontalFace face2) {
		return HorizontalEdgeHelper.find(face1, face2);
	}

	@Nullable
	public static HorizontalEdge find(Direction face1, Direction face2) {
		return find(HorizontalFace.find(face1), HorizontalFace.find(face2));
	}

	public static HorizontalEdge fromRotation(double yawDegrees) {
		return HorizontalEdgeHelper.fromRotation(yawDegrees);
	}

	public static HorizontalEdge fromOrdinal(int ordinal) {
		return HorizontalEdgeHelper.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<HorizontalEdge> consumer) {
		HorizontalEdgeHelper.forEach(consumer);
	}
}
