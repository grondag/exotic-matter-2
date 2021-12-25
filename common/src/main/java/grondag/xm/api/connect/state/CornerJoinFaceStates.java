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

import java.util.function.Consumer;

import grondag.xm.connect.CornerJoinFaceStateImpl;

/**
 * Identifies all of the possible face state within a corner join. All of these
 * states must be textures/transformed to correctly render the face.
 *
 * <p>The actual texturing/transformation is not part of this library because it
 * can vary greatly depending on use case, texture layout, etc.
 */
public abstract class CornerJoinFaceStates {
	private CornerJoinFaceStates() { }

	public static final CornerJoinFaceState NO_FACE = CornerJoinFaceStateImpl.NO_FACE;
	public static final CornerJoinFaceState NONE = CornerJoinFaceStateImpl.NONE;

	public static final CornerJoinFaceState TOP = CornerJoinFaceStateImpl.TOP;
	public static final CornerJoinFaceState BOTTOM = CornerJoinFaceStateImpl.BOTTOM;
	public static final CornerJoinFaceState LEFT = CornerJoinFaceStateImpl.LEFT;
	public static final CornerJoinFaceState RIGHT = CornerJoinFaceStateImpl.RIGHT;

	public static final CornerJoinFaceState TOP_BOTTOM = CornerJoinFaceStateImpl.TOP_BOTTOM;
	public static final CornerJoinFaceState LEFT_RIGHT = CornerJoinFaceStateImpl.LEFT_RIGHT;

	public static final CornerJoinFaceState TOP_BOTTOM_RIGHT_NO_CORNERS = CornerJoinFaceStateImpl.TOP_BOTTOM_RIGHT_NO_CORNERS;
	public static final CornerJoinFaceState TOP_BOTTOM_RIGHT_TR = CornerJoinFaceStateImpl.TOP_BOTTOM_RIGHT_TR;
	public static final CornerJoinFaceState TOP_BOTTOM_RIGHT_BR = CornerJoinFaceStateImpl.TOP_BOTTOM_RIGHT_BR;
	public static final CornerJoinFaceState TOP_BOTTOM_RIGHT_TR_BR = CornerJoinFaceStateImpl.TOP_BOTTOM_RIGHT_TR_BR;

	public static final CornerJoinFaceState TOP_BOTTOM_LEFT_NO_CORNERS = CornerJoinFaceStateImpl.TOP_BOTTOM_LEFT_NO_CORNERS;
	public static final CornerJoinFaceState TOP_BOTTOM_LEFT_TL = CornerJoinFaceStateImpl.TOP_BOTTOM_LEFT_TL;
	public static final CornerJoinFaceState TOP_BOTTOM_LEFT_BL = CornerJoinFaceStateImpl.TOP_BOTTOM_LEFT_BL;
	public static final CornerJoinFaceState TOP_BOTTOM_LEFT_TL_BL = CornerJoinFaceStateImpl.TOP_BOTTOM_LEFT_TL_BL;

	public static final CornerJoinFaceState TOP_LEFT_RIGHT_NO_CORNERS = CornerJoinFaceStateImpl.TOP_LEFT_RIGHT_NO_CORNERS;
	public static final CornerJoinFaceState TOP_LEFT_RIGHT_TL = CornerJoinFaceStateImpl.TOP_LEFT_RIGHT_TL;
	public static final CornerJoinFaceState TOP_LEFT_RIGHT_TR = CornerJoinFaceStateImpl.TOP_LEFT_RIGHT_TR;
	public static final CornerJoinFaceState TOP_LEFT_RIGHT_TL_TR = CornerJoinFaceStateImpl.TOP_LEFT_RIGHT_TL_TR;

	public static final CornerJoinFaceState BOTTOM_LEFT_RIGHT_NO_CORNERS = CornerJoinFaceStateImpl.BOTTOM_LEFT_RIGHT_NO_CORNERS;
	public static final CornerJoinFaceState BOTTOM_LEFT_RIGHT_BL = CornerJoinFaceStateImpl.BOTTOM_LEFT_RIGHT_BL;
	public static final CornerJoinFaceState BOTTOM_LEFT_RIGHT_BR = CornerJoinFaceStateImpl.BOTTOM_LEFT_RIGHT_BR;
	public static final CornerJoinFaceState BOTTOM_LEFT_RIGHT_BL_BR = CornerJoinFaceStateImpl.BOTTOM_LEFT_RIGHT_BL_BR;

	public static final CornerJoinFaceState TOP_LEFT_NO_CORNER = CornerJoinFaceStateImpl.TOP_LEFT_NO_CORNER;
	public static final CornerJoinFaceState TOP_LEFT_TL = CornerJoinFaceStateImpl.TOP_LEFT_TL;
	public static final CornerJoinFaceState TOP_RIGHT_NO_CORNER = CornerJoinFaceStateImpl.TOP_RIGHT_NO_CORNER;
	public static final CornerJoinFaceState TOP_RIGHT_TR = CornerJoinFaceStateImpl.TOP_RIGHT_TR;

	public static final CornerJoinFaceState BOTTOM_LEFT_NO_CORNER = CornerJoinFaceStateImpl.BOTTOM_LEFT_NO_CORNER;
	public static final CornerJoinFaceState BOTTOM_LEFT_BL = CornerJoinFaceStateImpl.BOTTOM_LEFT_BL;
	public static final CornerJoinFaceState BOTTOM_RIGHT_NO_CORNER = CornerJoinFaceStateImpl.BOTTOM_RIGHT_NO_CORNER;
	public static final CornerJoinFaceState BOTTOM_RIGHT_BR = CornerJoinFaceStateImpl.BOTTOM_RIGHT_BR;

	public static final CornerJoinFaceState ALL_NO_CORNERS = CornerJoinFaceStateImpl.ALL_NO_CORNERS;
	public static final CornerJoinFaceState ALL_TL = CornerJoinFaceStateImpl.ALL_TL;
	public static final CornerJoinFaceState ALL_TR = CornerJoinFaceStateImpl.ALL_TR;
	public static final CornerJoinFaceState ALL_TL_TR = CornerJoinFaceStateImpl.ALL_TL_TR;
	public static final CornerJoinFaceState ALL_BL = CornerJoinFaceStateImpl.ALL_BL;
	public static final CornerJoinFaceState ALL_TL_BL = CornerJoinFaceStateImpl.ALL_TL_BL;
	public static final CornerJoinFaceState ALL_TR_BL = CornerJoinFaceStateImpl.ALL_TR_BL;
	public static final CornerJoinFaceState ALL_TL_TR_BL = CornerJoinFaceStateImpl.ALL_TL_TR_BL;
	public static final CornerJoinFaceState ALL_BR = CornerJoinFaceStateImpl.ALL_BR;
	public static final CornerJoinFaceState ALL_TL_BR = CornerJoinFaceStateImpl.ALL_TL_BR;
	public static final CornerJoinFaceState ALL_TR_BR = CornerJoinFaceStateImpl.ALL_TR_BR;
	public static final CornerJoinFaceState ALL_TL_TR_BR = CornerJoinFaceStateImpl.ALL_TL_TR_BR;
	public static final CornerJoinFaceState ALL_BL_BR = CornerJoinFaceStateImpl.ALL_BL_BR;
	public static final CornerJoinFaceState ALL_TL_BL_BR = CornerJoinFaceStateImpl.ALL_TL_BL_BR;
	public static final CornerJoinFaceState ALL_TR_BL_BR = CornerJoinFaceStateImpl.ALL_TR_BL_BR;
	public static final CornerJoinFaceState ALL_TL_TR_BL_BR = CornerJoinFaceStateImpl.ALL_TL_TR_BL_BR;

	public static final int COUNT = CornerJoinFaceStateImpl.COUNT;

	public static final CornerJoinFaceState fromOrdinal(int ordinal) {
		return CornerJoinFaceStateImpl.fromOrdinal(ordinal);
	}

	public static void forEach(Consumer<CornerJoinFaceState> consumer) {
		CornerJoinFaceStateImpl.forEach(consumer);
	}
}
