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

package grondag.xm2.connect.api.state;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

import grondag.xm2.connect.api.model.FaceCorner;
import grondag.xm2.connect.api.model.FaceEdge;
import net.minecraft.util.math.Direction;

/**
 * Describes the connected-texture state of a block face for 
 * a face within a {@link CornerJoinState}. Result interpretation
 * may vary depending on the test used to derive the state.<p>
 * 
 * Can also be applied to shapes if the shape has some sort of
 * meaningful face with an appearance that varies based on connections.<p>
 * 
 * All information is <em>relative</em> to the block face for which this
 * state was returned.  You must obtain and retain that information
 * separately - it is not part of this object.
 */
@API(status = STABLE)
public interface CornerJoinFaceState {

    int ordinal();
    
    boolean isJoined(FaceEdge side);

    boolean isJoined(Direction toFace, Direction onFace);

    /**
     * True if connected-texture/shape blocks need to render corner due to
     * missing/covered block in adjacent corner.
     */
    boolean needsCorner(FaceCorner corner);

    /**
     * True if connected-texture/shape blocks need to render corner due to
     * missing/covered block in adjacent corner.
     * 
     * Note that to use this version you must know which block face
     * this state is on. 
     */
    boolean needsCorner(Direction face1, Direction face2, Direction onFace);
}
