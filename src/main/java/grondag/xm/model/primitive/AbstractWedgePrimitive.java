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

package grondag.xm.model.primitive;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SPECIES;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.model.varia.BlockOrientationType;
import grondag.xm.painting.SurfaceTopology;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;

public abstract class AbstractWedgePrimitive extends AbstractModelPrimitive {
    private static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder().add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS).add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

    public static final XmSurfaceImpl SURFACE_BACK = SURFACES.get(0);
    public static final XmSurfaceImpl SURFACE_BOTTOM = SURFACES.get(1);
    public static final XmSurfaceImpl SURFACE_TOP = SURFACES.get(2);
    public static final XmSurfaceImpl SURFACE_SIDES = SURFACES.get(3);

    public AbstractWedgePrimitive(String idString) {
        super(idString, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION | STATE_FLAG_HAS_AXIS_ORIENTATION);
    }

    @Override
    public XmSurfaceListImpl surfaces(ModelState modelState) {
        return SURFACES;
    }

    @Override
    public BlockOrientationType orientationType(ModelState modelState) {
        return isCorner(modelState) ? BlockOrientationType.CORNER : BlockOrientationType.EDGE;
    }

    @Override
    public boolean isAxisOrthogonalToPlacementFace() {
        return true;
    }

    // PERF: should be an owned model state
    @Override
    public ModelState geometricState(ModelState fromState) {
        MutableModelState result = this.newState();
        result.setAxis(fromState.getAxis());
        result.setAxisInverted(fromState.isAxisInverted());
        result.setAxisRotation(fromState.getAxisRotation());
        result.primitiveBits(fromState.primitiveBits());
        return result;
    }

    @Override
    public boolean doesShapeMatch(ModelState from, ModelState to) {
        return from.primitive() == to.primitive() && from.getAxis() == to.getAxis() && from.isAxisInverted() == to.isAxisInverted()
                && from.getAxisRotation() == to.getAxisRotation() && from.primitiveBits() == to.primitiveBits();
    }

    public static boolean isCorner(ModelState modelState) {
        return modelState.primitiveBits() == 1;
    }

    /**
     * If true, cuts in shape are on the block boundary. Saves value in static shape
     * bits in model state
     */
    public static void setCorner(boolean isCorner, MutableModelState modelState) {
        modelState.primitiveBits(isCorner ? 1 : 0);
    }
}
