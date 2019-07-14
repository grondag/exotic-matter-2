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

package grondag.xm2.api.connect.state;

import static org.apiguardian.api.API.Status.STABLE;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm2.connect.CornerJoinFaceStateImpl;

/**
 * Identifies all of the possible face state within a corner join. All of these
 * states must be textures/transformed to correctly render the face.
 * <p>
 * 
 * The actual texturing/transformation is not part of this library because it
 * can vary greatly depending on use case, texture layout, etc.
 */
@API(status = STABLE)
public abstract class CornerJoinFaceStates {
    private CornerJoinFaceStates() {
    };

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
