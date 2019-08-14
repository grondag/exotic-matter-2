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

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.helper.CubeInputs;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.PolyStream;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.painting.SurfaceTopology;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public class CubePrimitive extends AbstractBasePrimitive {
    public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
            .add("down", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("up", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("north", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("south", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("west", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("east", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .build();

    public static final XmSurfaceImpl SURFACE_DOWN = SURFACES.get(0);
    public static final XmSurfaceImpl SURFACE_UP = SURFACES.get(1);
    public static final XmSurfaceImpl SURFACE_NORTH = SURFACES.get(2);
    public static final XmSurfaceImpl SURFACE_SOUTH = SURFACES.get(3);
    public static final XmSurfaceImpl SURFACE_WEST = SURFACES.get(4);
    public static final XmSurfaceImpl SURFACE_EAST = SURFACES.get(5);
    
    public static final XmSurfaceImpl SURFACE_BOTTOM = SURFACES.get(0);
    public static final XmSurfaceImpl SURFACE_TOP = SURFACES.get(1);
    public static final XmSurfaceImpl SURFACE_BACK = SURFACES.get(2);
    public static final XmSurfaceImpl SURFACE_FRONT = SURFACES.get(3);
    public static final XmSurfaceImpl SURFACE_LEFT = SURFACES.get(4);
    public static final XmSurfaceImpl SURFACE_RIGHT = SURFACES.get(5);

    /** never changes so may as well save it */
    private final PolyStream cachedQuads;

    public CubePrimitive(String idString) {
        super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY);
        this.cachedQuads = getCubeQuads();
    }

    @Override
    public XmSurfaceListImpl surfaces(SimpleModelState modelState) {
        return SURFACES;
    }

    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        cachedQuads.forEach(target);
    }

    private PolyStream getCubeQuads() {
        CubeInputs cube = new CubeInputs();
        cube.color = 0xFFFFFFFF;
        cube.textureRotation = Rotation.ROTATE_NONE;
        cube.isFullBrightness = false;
        cube.u0 = 0;
        cube.v0 = 0;
        cube.u1 = 1;
        cube.v1 = 1;
        cube.isOverlay = false;

        WritablePolyStream stream = PolyStreams.claimWritable();
        cube.surface = SURFACE_DOWN;
        cube.appendFace(stream, Direction.DOWN);
        
        cube.surface = SURFACE_UP;
        cube.appendFace(stream, Direction.UP);
        
        cube.surface = SURFACE_EAST;
        cube.appendFace(stream, Direction.EAST);
        
        cube.surface = SURFACE_WEST;
        cube.appendFace(stream, Direction.WEST);
        
        cube.surface = SURFACE_NORTH;
        cube.appendFace(stream, Direction.NORTH);
        
        cube.surface = SURFACE_SOUTH;
        cube.appendFace(stream, Direction.SOUTH);

        PolyStream result = stream.releaseAndConvertToReader();

        result.origin();
        assert result.reader().vertexCount() == 4;

        return result;
    }

    @Override
    public SimpleModelState.Mutable geometricState(SimpleModelState fromState) {
        return defaultState().mutableCopy();
    }

    @Override
    public boolean doesShapeMatch(SimpleModelState from, SimpleModelState to) {
        return from.primitive() == to.primitive();
    }
}
