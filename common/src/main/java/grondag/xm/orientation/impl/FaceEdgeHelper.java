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

package grondag.xm.orientation.impl;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import grondag.xm.orientation.api.FaceEdge;

@Internal
public abstract class FaceEdgeHelper {
	private FaceEdgeHelper() {
	}

	private static final FaceEdge[] VALUES = FaceEdge.values();

	public static final int COUNT = VALUES.length;

	public static final FaceEdge fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	// find the side for a given face orthogonal to a face
	private static final FaceEdge[][] FACE_LOOKUP = new FaceEdge[6][6];

	static {
		for (final Direction onFace : Direction.values()) {
			for (final Direction edgeFace : Direction.values()) {
				FaceEdge match = null;

				for (final FaceEdge side : FaceEdge.values()) {
					if (side.toWorld(onFace) == edgeFace) {
						match = side;
					}
				}

				FACE_LOOKUP[onFace.ordinal()][edgeFace.ordinal()] = match;
			}
		}
	}

	/**
	 * Determines if the given sideFace is TOP, BOTTOM, DEFAULT_LEFT or
	 * DEFAULT_RIGHT of onFace. If none (sideFace on same orthogonalAxis as onFace),
	 * return null;
	 */
	@Nullable
	public static FaceEdge fromWorld(Direction edgeFace, Direction onFace) {
		return FACE_LOOKUP[onFace.ordinal()][edgeFace.ordinal()];
	}

	public static void forEach(Consumer<FaceEdge> consumer) {
		for (final FaceEdge val : VALUES) {
			consumer.accept(val);
		}
	}
}
