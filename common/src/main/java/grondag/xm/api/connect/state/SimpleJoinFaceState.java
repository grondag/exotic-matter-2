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

package grondag.xm.api.connect.state;

import net.minecraft.core.Direction;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.orientation.api.CubeEdge;
import grondag.xm.orientation.api.FaceEdge;

public enum SimpleJoinFaceState {
	NO_FACE(0),
	TOP(FaceEdge.TOP_EDGE.ordinalBit),
	BOTTOM(FaceEdge.BOTTOM_EDGE.ordinalBit),
	LEFT(FaceEdge.LEFT_EDGE.ordinalBit),
	RIGHT(FaceEdge.RIGHT_EDGE.ordinalBit),
	TOP_BOTTOM(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit),
	LEFT_RIGHT(FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
	TOP_BOTTOM_RIGHT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
	TOP_BOTTOM_LEFT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit),
	TOP_LEFT_RIGHT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
	BOTTOM_LEFT_RIGHT(FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
	TOP_LEFT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit),
	TOP_RIGHT(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
	BOTTOM_LEFT(FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit),
	BOTTOM_RIGHT(FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit),
	ALL(FaceEdge.TOP_EDGE.ordinalBit | FaceEdge.BOTTOM_EDGE.ordinalBit | FaceEdge.LEFT_EDGE.ordinalBit | FaceEdge.RIGHT_EDGE.ordinalBit);

	public static final int COUNT = 16;

	private static final SimpleJoinFaceState[] LOOKUP = new SimpleJoinFaceState[COUNT];

	private final int bitFlags;

	static {
		for (final SimpleJoinFaceState state : SimpleJoinFaceState.values()) {
			LOOKUP[state.bitFlags] = state;
		}
	}

	SimpleJoinFaceState(int faceBits) {
		bitFlags = faceBits;
	}

	private static SimpleJoinFaceState find(int faceBits) {
		return LOOKUP[(faceBits & 15)];
	}

	private static final FaceEdge[] EDGES = FaceEdge.values();

	// PERF: need a lookup similar to corner joins here?
	public static SimpleJoinFaceState find(Direction face, SimpleJoinState join) {
		int faceFlags = 0;

		SimpleJoinFaceState fjs;

		if (join.isJoined(face)) {
			fjs = SimpleJoinFaceState.NO_FACE;
		} else {
			for (final FaceEdge fside : EDGES) {
				if (join.isJoined(fside.toWorld(face))) {
					faceFlags |= fside.ordinalBit;
				}
			}

			fjs = SimpleJoinFaceState.find(faceFlags);
		}

		return fjs;
	}

	public static SimpleJoinFaceState find(Direction face, BlockNeighbors tests) {
		int faceFlags = 0;

		SimpleJoinFaceState fjs;

		if (tests.result(face)) {
			fjs = SimpleJoinFaceState.NO_FACE;
		} else {
			for (final FaceEdge fside : EDGES) {
				final Direction joinFace = fside.toWorld(face);

				if (tests.result(joinFace) && !tests.result(CubeEdge.find(face, joinFace))) {
					faceFlags |= fside.ordinalBit;
				}
			}

			fjs = SimpleJoinFaceState.find(faceFlags);
		}

		return fjs;
	}

	public boolean isJoined(FaceEdge side) {
		return (bitFlags & side.ordinalBit) == side.ordinalBit;
	}

	public boolean isJoined(Direction toFace, Direction onFace) {
		final FaceEdge side = FaceEdge.fromWorld(toFace, onFace);
		return side == null ? false : this.isJoined(side);
	}
}
