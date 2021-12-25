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

package grondag.xm.connect;

import static grondag.xm.orientation.api.FaceCorner.BOTTOM_LEFT;
import static grondag.xm.orientation.api.FaceCorner.BOTTOM_RIGHT;
import static grondag.xm.orientation.api.FaceCorner.TOP_LEFT;
import static grondag.xm.orientation.api.FaceCorner.TOP_RIGHT;
import static grondag.xm.orientation.api.FaceEdge.BOTTOM_EDGE;
import static grondag.xm.orientation.api.FaceEdge.LEFT_EDGE;
import static grondag.xm.orientation.api.FaceEdge.RIGHT_EDGE;
import static grondag.xm.orientation.api.FaceEdge.TOP_EDGE;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.Direction;

import grondag.xm.api.connect.state.CornerJoinFaceState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.orientation.api.CubeEdge;
import grondag.xm.orientation.api.FaceCorner;
import grondag.xm.orientation.api.FaceEdge;

/**
 * Corner bits indicate that a corner is needed, not that the corner is present.
 * (These are normally inverse.)
 */
@Internal
public enum CornerJoinFaceStateImpl implements CornerJoinFaceState {
	NO_FACE(0, 0), NONE(0, 0), // must be after NO_FACE, overwrites NO_FACE in lookup table, should never be
	// checked by lookup
	TOP(TOP_EDGE.ordinalBit, 0), BOTTOM(BOTTOM_EDGE.ordinalBit, 0), LEFT(LEFT_EDGE.ordinalBit, 0), RIGHT(RIGHT_EDGE.ordinalBit, 0),

	TOP_BOTTOM(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit, 0), LEFT_RIGHT(LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0),

	TOP_BOTTOM_RIGHT_NO_CORNERS(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, TOP_RIGHT, BOTTOM_RIGHT),
	TOP_BOTTOM_RIGHT_TR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit),
	TOP_BOTTOM_RIGHT_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit),
	TOP_BOTTOM_RIGHT_TR_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit | BOTTOM_RIGHT.ordinalBit),

	TOP_BOTTOM_LEFT_NO_CORNERS(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, 0, TOP_LEFT, BOTTOM_LEFT),
	TOP_BOTTOM_LEFT_TL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, TOP_LEFT.ordinalBit),
	TOP_BOTTOM_LEFT_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit),
	TOP_BOTTOM_LEFT_TL_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, TOP_LEFT.ordinalBit | BOTTOM_LEFT.ordinalBit),

	TOP_LEFT_RIGHT_NO_CORNERS(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, TOP_LEFT, TOP_RIGHT),
	TOP_LEFT_RIGHT_TL(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_LEFT.ordinalBit),
	TOP_LEFT_RIGHT_TR(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit),
	TOP_LEFT_RIGHT_TL_TR(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_LEFT.ordinalBit | TOP_RIGHT.ordinalBit),

	BOTTOM_LEFT_RIGHT_NO_CORNERS(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, BOTTOM_LEFT, BOTTOM_RIGHT),
	BOTTOM_LEFT_RIGHT_BL(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit),
	BOTTOM_LEFT_RIGHT_BR(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit),
	BOTTOM_LEFT_RIGHT_BL_BR(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit | BOTTOM_RIGHT.ordinalBit),

	TOP_LEFT_NO_CORNER(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, 0, TOP_LEFT), TOP_LEFT_TL(TOP_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, TOP_LEFT.ordinalBit),

	TOP_RIGHT_NO_CORNER(TOP_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, TOP_RIGHT),
	TOP_RIGHT_TR(TOP_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit),

	BOTTOM_LEFT_NO_CORNER(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, 0, BOTTOM_LEFT),
	BOTTOM_LEFT_BL(BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit),

	BOTTOM_RIGHT_NO_CORNER(BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, BOTTOM_RIGHT),
	BOTTOM_RIGHT_BR(BOTTOM_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit),

	ALL_NO_CORNERS(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, 0, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT,
			BOTTOM_RIGHT),
	ALL_TL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_LEFT.ordinalBit),
	ALL_TR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit),
	ALL_TL_TR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
	ALL_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit),
	ALL_TL_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit | TOP_LEFT.ordinalBit),
	ALL_TR_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit),
	ALL_TL_TR_BL(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
			BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
	ALL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit),
	ALL_TL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
	ALL_TR_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit | TOP_RIGHT.ordinalBit),
	ALL_TL_TR_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
			BOTTOM_RIGHT.ordinalBit | TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit),
	ALL_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit, BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit),
	ALL_TL_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
			BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit | TOP_LEFT.ordinalBit),
	ALL_TR_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
			BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit),
	ALL_TL_TR_BL_BR(TOP_EDGE.ordinalBit | BOTTOM_EDGE.ordinalBit | LEFT_EDGE.ordinalBit | RIGHT_EDGE.ordinalBit,
			BOTTOM_RIGHT.ordinalBit | BOTTOM_LEFT.ordinalBit | TOP_RIGHT.ordinalBit | TOP_LEFT.ordinalBit);

	private final int bitFlags;
	private final FaceCorner[] cornerTests;
	private CornerJoinFaceStateImpl[] subStates;

	CornerJoinFaceStateImpl(int faceBits, int cornerBits, FaceCorner... cornerTests) {
		bitFlags = faceBits | (cornerBits << 4);
		this.cornerTests = cornerTests;
	}

	private boolean hasCornerTests() {
		return (cornerTests != null && cornerTests.length > 0);
	}

	private FaceCorner[] cornerTests() {
		return cornerTests;
	}

	CornerJoinFaceStateImpl[] subStates() {
		return subStates;
	}

	@Override
	public boolean isJoined(FaceEdge side) {
		return (bitFlags & side.ordinalBit) == side.ordinalBit;
	}

	@Override
	public boolean isJoined(Direction toFace, Direction onFace) {
		final FaceEdge side = FaceEdge.fromWorld(toFace, onFace);
		return side == null ? false : this.isJoined(side);
	}

	/**
	 * True if connected-texture/shape blocks need to render corner due to
	 * missing/covered block in adjacent corner.
	 */
	@Override
	public boolean needsCorner(FaceCorner corner) {
		return ((bitFlags >> 4) & corner.ordinalBit) == corner.ordinalBit;
	}

	@Override
	public boolean needsCorner(Direction face1, Direction face2, Direction onFace) {
		final FaceEdge side1 = FaceEdge.fromWorld(face1, onFace);
		final FaceEdge side2 = FaceEdge.fromWorld(face2, onFace);
		return side1 == null || side2 == null ? false : this.needsCorner(FaceCorner.find(side1, side2));
	}

	private static final CornerJoinFaceStateImpl[] VALUES = CornerJoinFaceStateImpl.values();
	public static final int COUNT = VALUES.length;

	/**
	 * Sparsely populated - only meaningful states are non-null. For example, cannot
	 * also have corners on side with a border.
	 */
	private static final CornerJoinFaceStateImpl[] LOOKUP = new CornerJoinFaceStateImpl[256];

	static {
		for (final CornerJoinFaceStateImpl state : CornerJoinFaceStateImpl.values()) {
			LOOKUP[state.bitFlags] = state;

			final ArrayList<CornerJoinFaceStateImpl> subStateList = new ArrayList<>();

			if (state == NO_FACE) {
				subStateList.add(NO_FACE);
			} else {
				for (final CornerJoinFaceStateImpl subState : CornerJoinFaceStateImpl.values()) {
					if (subState != NO_FACE && (subState.bitFlags & state.bitFlags & 15) == (subState.bitFlags & 15)) {
						subStateList.add(subState);
					}
				}
			}

			state.subStates = subStateList.toArray(new CornerJoinFaceStateImpl[subStateList.size()]);
		}
	}

	private static CornerJoinFaceStateImpl find(int faceBits, int cornerBits) {
		return LOOKUP[(faceBits & 15) | ((cornerBits & 15) << 4)];
	}

	public static CornerJoinFaceStateImpl find(Direction face, SimpleJoinStateImpl join) {
		int faceFlags = 0;

		CornerJoinFaceStateImpl fjs;

		if (join.isJoined(face)) {
			fjs = CornerJoinFaceStateImpl.NO_FACE;
		} else {
			for (int i = 0; i < FaceEdge.COUNT; i++) {
				final FaceEdge fside = FaceEdge.fromOrdinal(i);

				if (join.isJoined(fside.toWorld(face))) {
					faceFlags |= fside.ordinalBit;
				}
			}

			fjs = CornerJoinFaceStateImpl.find(faceFlags, 0);
		}

		return fjs;
	}

	public static CornerJoinFaceStateImpl find(Direction face, BlockNeighbors tests) {
		int faceFlags = 0;
		int cornerFlags = 0;

		CornerJoinFaceStateImpl fjs;

		if (tests.result(face)) {
			fjs = CornerJoinFaceStateImpl.NO_FACE;
		} else {
			for (int i = 0; i < FaceEdge.COUNT; i++) {
				final FaceEdge fside = FaceEdge.fromOrdinal(i);
				final Direction joinFace = fside.toWorld(face);

				if (tests.result(joinFace) && !tests.result(CubeEdge.find(face, joinFace))) {
					faceFlags |= fside.ordinalBit;
				}
			}

			fjs = CornerJoinFaceStateImpl.find(faceFlags, cornerFlags);

			if (fjs.hasCornerTests()) {
				for (final FaceCorner corner : fjs.cornerTests()) {
					if (!tests.result(corner.leftSide.toWorld(face), corner.rightSide.toWorld(face))
							|| tests.result(corner.leftSide.toWorld(face), corner.rightSide.toWorld(face), face)) {
						cornerFlags |= corner.ordinalBit;
					}
				}

				fjs = CornerJoinFaceStateImpl.find(faceFlags, cornerFlags);
			}
		}

		return fjs;
	}

	public static CornerJoinFaceStateImpl fromOrdinal(int ordinal) {
		return VALUES[ordinal];
	}

	public static void forEach(Consumer<CornerJoinFaceState> consumer) {
		for (final CornerJoinFaceStateImpl val : VALUES) {
			consumer.accept(val);
		}
	}
}
