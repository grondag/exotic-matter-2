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
import grondag.xm.api.connect.model.BlockEdgeSided;
import grondag.xm.api.mesh.PolyTransform;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.PolyStream;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.model.varia.BlockOrientationType;
import grondag.xm.painting.SurfaceTopology;
import net.minecraft.util.math.Direction;

public class CubeWithAxisAndFace extends AbstractSimplePrimitive {
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("down", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("up", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("north", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("south", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("west", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("east", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .build();

    public static final XmSurface SURFACE_DOWN = SURFACES.get(0);
    public static final XmSurface SURFACE_UP = SURFACES.get(1);
    public static final XmSurface SURFACE_NORTH = SURFACES.get(2);
    public static final XmSurface SURFACE_SOUTH = SURFACES.get(3);
    public static final XmSurface SURFACE_WEST = SURFACES.get(4);
    public static final XmSurface SURFACE_EAST = SURFACES.get(5);
    
    public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
    public static final XmSurface SURFACE_TOP = SURFACES.get(1);
    public static final XmSurface SURFACE_BACK = SURFACES.get(2);
    public static final XmSurface SURFACE_FRONT = SURFACES.get(3);
    public static final XmSurface SURFACE_LEFT = SURFACES.get(4);
    public static final XmSurface SURFACE_RIGHT = SURFACES.get(5);

    /** never changes so may as well save it */
    private final PolyStream[] cachedQuads = new PolyStream[BlockEdgeSided.COUNT];

    public static final CubeWithAxisAndFace INSTANCE = new CubeWithAxisAndFace(Xm.idString("cube_axis_face"));

    protected CubeWithAxisAndFace(String idString) {
        super(idString, STATE_FLAG_NONE, SimpleModelStateImpl.FACTORY, s -> SURFACES);
        invalidateCache();
    }

    @Override
    public void invalidateCache() {
        BlockEdgeSided.forEach( o -> cachedQuads[o.ordinal()] = getCubeQuads(o));
    }


    @Override
    public BlockOrientationType orientationType(SimpleModelState modelState) {
        return BlockOrientationType.EDGE_SIDED;
    }
    
    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        cachedQuads[modelState.orientationIndex()].forEach(target);
    }

    private PolyStream getCubeQuads(BlockEdgeSided orientation) {
        PolyTransform transform = PolyTransform.forEdge(orientation.ordinal());

        WritablePolyStream stream = PolyStreams.claimWritable();
        MutablePolygon writer = stream.writer();
        writer.colorAll(0, 0xFFFFFFFF);
        writer.lockUV(0, true);
        writer.rotation(0, Rotation.ROTATE_NONE);
        writer.sprite(0, "");
        stream.saveDefaults();

        writer.surface(SURFACE_DOWN);
        writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_UP);
        writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_EAST);
        writer.setupFaceQuad(Direction.EAST, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_WEST);
        writer.setupFaceQuad(Direction.WEST, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_NORTH);
        writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();
        
        writer.surface(SURFACE_SOUTH);
        writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();

        PolyStream result = stream.releaseToReader();

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
