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
package grondag.xm.api.connect.state;

import net.minecraft.util.math.Direction;

import grondag.fermion.orientation.api.CubeEdge;
import grondag.fermion.orientation.api.FaceEdge;
import grondag.xm.api.connect.world.BlockNeighbors;

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
