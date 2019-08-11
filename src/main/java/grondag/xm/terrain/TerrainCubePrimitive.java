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

package grondag.xm.terrain;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.api.terrain.TerrainModelState;
import grondag.xm.mesh.helper.CubeInputs;
import grondag.xm.mesh.polygon.IPolygon;
import grondag.xm.mesh.stream.IPolyStream;
import grondag.xm.mesh.stream.IWritablePolyStream;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.painting.SurfaceTopology;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public class TerrainCubePrimitive extends AbstractTerrainPrimitive {
    public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder().add("all", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS).build();

    public static final XmSurfaceImpl SURFACE_ALL = SURFACES.get(0);

    /** never changes so may as well save it */
    private final IPolyStream cachedQuads;

    public TerrainCubePrimitive(String idString) {
        super(idString, STATE_FLAG_NONE, TerrainModelStateImpl.FACTORY);
        this.cachedQuads = getCubeQuads();
    }

    @Override
    public XmSurfaceListImpl surfaces(TerrainModelState modelState) {
        return SURFACES;
    }

    @Override
    public void produceQuads(TerrainModelState modelState, Consumer<IPolygon> target) {
        cachedQuads.forEach(target);
    }

    private IPolyStream getCubeQuads() {
        CubeInputs cube = new CubeInputs();
        cube.color = 0xFFFFFFFF;
        cube.textureRotation = Rotation.ROTATE_NONE;
        cube.isFullBrightness = false;
        cube.u0 = 0;
        cube.v0 = 0;
        cube.u1 = 1;
        cube.v1 = 1;
        cube.isOverlay = false;
        cube.surface = SURFACE_ALL;

        IWritablePolyStream stream = PolyStreams.claimWritable();
        cube.appendFace(stream, Direction.DOWN);
        cube.appendFace(stream, Direction.UP);
        cube.appendFace(stream, Direction.EAST);
        cube.appendFace(stream, Direction.WEST);
        cube.appendFace(stream, Direction.NORTH);
        cube.appendFace(stream, Direction.SOUTH);

        IPolyStream result = stream.releaseAndConvertToReader();

        result.origin();
        assert result.reader().vertexCount() == 4;

        return result;
    }

    @Override
    public TerrainModelState.Mutable geometricState(TerrainModelState fromState) {
        return defaultState().mutableCopy();
    }

    @Override
    public boolean doesShapeMatch(TerrainModelState from, TerrainModelState to) {
        return from.primitive() == to.primitive();
    }
}
