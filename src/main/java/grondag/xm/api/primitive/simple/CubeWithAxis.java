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

package grondag.xm.api.primitive.simple;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NONE;

import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.painting.SurfaceTopology;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class CubeWithAxis extends AbstractSimplePrimitive {
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("ends", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .build();

    public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
    public static final XmSurface SURFACE_SIDES = SURFACES.get(1);

    /** never changes so may as well save it */
    private final XmMesh[] cachedQuads = new XmMesh[3];

    public static final CubeWithAxis INSTANCE = new CubeWithAxis(Xm.idString("cube_axis"));

    protected CubeWithAxis(String idString) {
        super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY, s -> SURFACES);
        invalidateCache();
    }

    @Override
    public void invalidateCache() {
        cachedQuads[Axis.X.ordinal()] = getCubeQuads(Axis.X);
        cachedQuads[Axis.Y.ordinal()] = getCubeQuads(Axis.Y);
        cachedQuads[Axis.Z.ordinal()] = getCubeQuads(Axis.Z);
    }

    @Override
    public OrientationType orientationType(SimpleModelState modelState) {
        return OrientationType.AXIS;
    }
    
    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        cachedQuads[modelState.orientationIndex()].forEach(target);
    }

    private XmMesh getCubeQuads(Axis orientation) {
        PolyTransform transform = PolyTransform.forAxis(orientation.ordinal());

        WritableMesh stream = XmMeshes.claimWritable();
        MutablePolygon writer = stream.writer();
        writer.colorAll(0, 0xFFFFFFFF);
        writer.lockUV(0, true);
        writer.rotation(0, Rotation.ROTATE_NONE);
        writer.sprite(0, "");
        stream.saveDefaults();

        writer.surface(SURFACE_ENDS);
        writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_ENDS);
        writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();

        XmMesh result = stream.releaseToReader();

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
