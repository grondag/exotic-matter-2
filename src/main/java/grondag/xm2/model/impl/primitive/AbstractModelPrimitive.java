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

package grondag.xm2.model.impl.primitive;

import java.util.function.Consumer;

import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.model.api.primitive.ModelPrimitive;
import grondag.xm2.model.impl.state.ModelState;
import grondag.xm2.model.impl.state.StateFormat;
import grondag.xm2.surface.impl.XmSurfaceImpl.XmSurfaceListImpl;

public abstract class AbstractModelPrimitive implements ModelPrimitive {
	public final XmSurfaceListImpl surfaces;
	
    /**
     * used by ModelState to know why type of state representation is needed by this
     * shape
     */
    public final StateFormat stateFormat;

    /**
     * bits flags used by ModelState to know which optional state elements are
     * needed by this shape
     */
    private final int stateFlags;

    /**
     * When shape is changed on ModelState, the per-shape bits will be set to this
     * value. Only need to change if shape needs some preset state.
     */
    public final long defaultShapeStateBits;

    protected AbstractModelPrimitive(XmSurfaceListImpl surfaces, StateFormat stateFormat, int stateFlags) {
        this(surfaces, stateFormat, stateFlags, 0L);
    }

    protected AbstractModelPrimitive(XmSurfaceListImpl surfaces, StateFormat stateFormat, int stateFlags, long defaultShapeStateBits) {
    	this.surfaces = surfaces;
        this.stateFormat = stateFormat;
        this.stateFlags = stateFlags;
        this.defaultShapeStateBits = defaultShapeStateBits;
    }

    /**
     * How much of the sky is occluded by the shape of this block? Based on geometry
     * alone, not transparency. Returns 0 if no occlusion (unlikely result). 1-15 if
     * some occlusion. 255 if fully occludes sky.
     */
    public abstract int geometricSkyOcclusion(ModelState modelState);

    /**
     * Generator will output polygons and they will be quads or tris.
     * <p>
     * 
     * Consumer MUST NOT hold references to any of the polys received.
     */
    public abstract void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target);

    /** Returns true if geometry is a full 1x1x1 cube. */
    public abstract boolean isCube(ModelState modelState);

    /**
     * If true, shape can be placed on itself to become bigger.
     */
    public boolean isAdditive() {
        return false;
    }

    /**
     * Override to true for blocks like stairs and wedges. CubicPlacementHandler
     * will know they need to be placed in a corner instead of a face.
     */
    public boolean isAxisOrthogonalToPlacementFace() {
        return false;
    }

    public int getStateFlags(ModelState modelState) {
        return stateFlags;
    }
}
