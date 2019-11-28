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

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;
import org.apiguardian.api.API;

import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

import grondag.xm.connect.helper.HorizontalEdgeHelper;

/**
 * A subset of {@link CubeEdge}, includes only the edges in the horizontal
 * plane.
 */
@API(status = EXPERIMENTAL)
public enum HorizontalEdge implements StringIdentifiable {
	NORTH_EAST(HorizontalFace.NORTH, HorizontalFace.EAST),
	NORTH_WEST(HorizontalFace.WEST, HorizontalFace.NORTH),
	SOUTH_EAST(HorizontalFace.EAST, HorizontalFace.SOUTH),
	SOUTH_WEST(HorizontalFace.SOUTH, HorizontalFace.WEST);

	public final HorizontalFace left;
	public final HorizontalFace right;

	public final Vec3i vector;

	public final String name;

	private HorizontalEdge(HorizontalFace left, HorizontalFace right) {
		name = name().toLowerCase();
		this.left = left;
		this.right = right;
		vector = new Vec3i(left.face.getVector().getX() + right.face.getVector().getX(), 0,
				left.face.getVector().getZ() + right.face.getVector().getZ());
	}

	public HorizontalEdge rotate(BlockRotation rotation) {
		final Direction face1 = rotation.rotate(left.face);
		final Direction face2 = rotation.rotate(right.face);
		return ObjectUtils.defaultIfNull(find(face1, face2), this);
	}

	@Override
	public String asString() {
		return name;
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
