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

package grondag.xm.api.primitive.base;

import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TL_TR_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.ALL_TR_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BL_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_LEFT_RIGHT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_RIGHT_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.BOTTOM_RIGHT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.COUNT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.LEFT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.LEFT_RIGHT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.NONE;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.RIGHT;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_LEFT_TL_BL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_BOTTOM_RIGHT_TR_BR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_NO_CORNERS;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_TL_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_RIGHT_TR;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_LEFT_TL;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_RIGHT_NO_CORNER;
import static grondag.xm.api.connect.state.CornerJoinFaceStates.TOP_RIGHT_TR;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.connect.state.CornerJoinFaceState;

/**
 * Holds helper methods for generating connected shapes - not sure yet if part of API.
 */
@API(status = EXPERIMENTAL)
public class ConnectedShapeHelper {
    private static void spec(float[][] target, CornerJoinFaceState state, float... values) {
        target[state.ordinal()] = values;
    }
    
    /** generates CSG cuts needed for a simple even-margin panel */
    public static float[][] panelspec(final float margin) {
        final float far = 1 - margin;
        final float[][] result = new float[COUNT][];
        
        spec(result, ALL_NO_CORNERS, 0, 0, 1, 1);
        spec(result, NONE, margin, margin, far, far);
        spec(result, TOP, margin, margin, far, 1);
        spec(result, BOTTOM, margin, 0, far, far);
        spec(result, LEFT, 0, margin, far, far);
        spec(result, RIGHT, margin, margin, 1, far);
        spec(result, TOP_BOTTOM, margin, 0, far, 1);
        spec(result, LEFT_RIGHT, 0, margin, 1, far);
        spec(result, ALL_TL_TR_BL_BR, margin, 0, far, 1, 0, margin, 1, far);
        spec(result, BOTTOM_LEFT_NO_CORNER, 0, 0, far, far);
        spec(result, TOP_LEFT_NO_CORNER, 0, margin, far, 1);
        spec(result, TOP_RIGHT_NO_CORNER, margin, margin, 1, 1);
        spec(result, BOTTOM_RIGHT_NO_CORNER, margin, 0, 1, far);
        spec(result, BOTTOM_LEFT_RIGHT_NO_CORNERS, 0, 0, 1, far);
        spec(result, TOP_BOTTOM_LEFT_NO_CORNERS, 0, 0, far, 1);
        spec(result, TOP_LEFT_RIGHT_NO_CORNERS, 0, margin, 1, 1);
        spec(result, TOP_BOTTOM_RIGHT_NO_CORNERS, margin, 0, 1, 1);
        spec(result, ALL_TR, 0, 0, far, 1, 0, 0, 1, far);
        spec(result, ALL_BR, 0, 0, far, 1, 0, margin, 1, 1);
        spec(result, ALL_BL, margin, 0, 1, 1, 0, margin, 1, 1);
        spec(result, ALL_TL, margin, 0, 1, 1, 0, 0, 1, far);
        spec(result, ALL_TL_TR, margin, 0, far, 1, 0, 0, 1, far);
        spec(result, ALL_TR_BR, 0, 0, far, 1, 0, margin, 1, far);
        spec(result, ALL_TL_BL, margin, 0, 1, 1, 0, margin, 1, far);
        spec(result, ALL_TR_BL, margin, 0, 1, far, 0, margin, far, 1);
        spec(result, ALL_TL_BR, 0, 0, far, far, margin, margin, 1, 1);
        spec(result, ALL_BL_BR, 0, margin, 1, 1, margin, 0, far, 1);
        spec(result, ALL_TR_BL_BR, margin, 0, far, 1, 0, margin, 1, far, 0, far, margin, 1);
        spec(result, ALL_TL_BL_BR, margin, 0, far, 1, 0, margin, 1, far, far, far, 1, 1);
        spec(result, ALL_TL_TR_BL, margin, 0, far, 1, 0, margin, 1, far, far, 0, 1, margin);
        spec(result, ALL_TL_TR_BR, margin, 0, far, 1, 0, margin, 1, far, 0, 0, margin, margin);
        spec(result, BOTTOM_LEFT_RIGHT_BR, 0, margin, 1, far, 0, 0, far, margin);
        spec(result, BOTTOM_LEFT_RIGHT_BL, 0, margin, 1, far, margin, 0, 1, margin);
        spec(result, TOP_LEFT_RIGHT_TL, 0, margin, 1, far, margin, far, 1, 1);
        spec(result, TOP_LEFT_RIGHT_TR, 0, margin, 1, far, 0, far, far, 1);
        spec(result, TOP_BOTTOM_LEFT_BL, margin, 0, far, 1, 0, margin, margin, 1);
        spec(result, TOP_BOTTOM_LEFT_TL, margin, 0, far, 1, 0, 0, margin, far);
        spec(result, TOP_BOTTOM_RIGHT_TR, margin, 0, far, 1, far, 0, 1, far);
        spec(result, TOP_BOTTOM_RIGHT_BR, margin, 0, far, 1, far, margin, 1, 1);
        spec(result, BOTTOM_LEFT_BL, margin, 0, far, far, 0, margin, margin, far);
        spec(result, TOP_LEFT_TL, margin, margin, far, 1, 0, margin, margin, far);
        spec(result, TOP_RIGHT_TR, margin, margin, far, 1, far, margin, 1, far);
        spec(result, BOTTOM_RIGHT_BR, margin, 0, far, far, far, margin, 1, far);
        spec(result, BOTTOM_LEFT_RIGHT_BL_BR, margin, 0, far, margin, 0, margin, 1, far);
        spec(result, TOP_BOTTOM_LEFT_TL_BL, margin, 0, far, 1, 0, margin, margin, far);
        spec(result, TOP_LEFT_RIGHT_TL_TR, 0, margin, 1, far, margin, far, far, 1);
        spec(result, TOP_BOTTOM_RIGHT_TR_BR, margin, 0, far, 1, far, margin, 1, far);
        return result;
    }
}
