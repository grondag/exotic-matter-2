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

package grondag.xm2.mesh;

import static grondag.xm2.state.ModelStateData.STATE_FLAG_HAS_AXIS;
import static grondag.xm2.state.ModelStateData.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm2.state.ModelStateData.STATE_FLAG_HAS_AXIS_ROTATION;
import static grondag.xm2.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;

import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.StateFormat;
import grondag.xm2.surface.api.XmSurface;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import grondag.xm2.surface.impl.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public abstract class AbstractWedgeMeshFactory extends MeshFactory {
	public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
			.add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.build();
	
	
	public static final XmSurfaceImpl SURFACE_BACK = SURFACES.get(0);
	public static final XmSurfaceImpl SURFACE_BOTTOM = SURFACES.get(1);
	public static final XmSurfaceImpl SURFACE_TOP = SURFACES.get(2);
	public static final XmSurfaceImpl SURFACE_SIDES = SURFACES.get(3);
	
    public AbstractWedgeMeshFactory() {
        super(SURFACES, StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ROTATION | STATE_FLAG_HAS_AXIS_ORIENTATION);
    }

    @Override
    public boolean isCube(ModelState modelState) {
        return false;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState) {
        return modelState.getAxis() == Direction.Axis.Y ? 7 : 255;
    }

    @Override
    public BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.EDGE;
    }

    @Override
    public boolean isAxisOrthogonalToPlacementFace() {
        return true;
    }
}
