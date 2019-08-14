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

import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.helper.FaceVertex;
import grondag.xm.mesh.helper.PolyTransform;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.painting.SurfaceTopology;
import grondag.xm.surface.XmSurfaceImpl;
import grondag.xm.surface.XmSurfaceImpl.XmSurfaceListImpl;
import grondag.xm.mesh.stream.PolyStreams;
import net.minecraft.util.math.Direction;

public class WedgePrimitive extends AbstractWedgePrimitive {
    private static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
            .add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

    public static final XmSurfaceImpl SURFACE_BACK = SURFACES.get(0);
    public static final XmSurfaceImpl SURFACE_BOTTOM = SURFACES.get(1);
    public static final XmSurfaceImpl SURFACE_TOP = SURFACES.get(2);
    public static final XmSurfaceImpl SURFACE_SIDES = SURFACES.get(3);
    
    public WedgePrimitive(String idString) {
        super(idString);
    }
    
    @Override
    public XmSurfaceListImpl surfaces(SimpleModelState modelState) {
        return SURFACES;
    }

    @Override
    public void invalidateCache() { 
        //TODO: caching
    }
    
    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        // Axis for this shape is through the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y axis with full sides against north/down faces.

        // PERF: caching
        final WritablePolyStream stream = PolyStreams.claimWritable();
        final MutablePolygon writer = stream.writer();

        PolyTransform transform = PolyTransform.get(modelState);

        writer.rotation(0, Rotation.ROTATE_NONE);
        writer.lockUV(0, true);
        stream.saveDefaults();

        writer.surface(SURFACE_BOTTOM);
        writer.nominalFace(Direction.NORTH);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();

        writer.surface(SURFACE_BOTTOM);
        writer.nominalFace(Direction.DOWN);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(3);
        writer.surface(SURFACE_SIDES);
        writer.nominalFace(Direction.EAST);
        writer.setupFaceQuad(Direction.EAST, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0), new FaceVertex(1, 1, 0), Direction.UP);
        writer.assignLockedUVCoordinates(0);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(3);
        writer.surface(SURFACE_SIDES);
        writer.nominalFace(Direction.WEST);
        writer.setupFaceQuad(Direction.WEST, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0), new FaceVertex(0, 1, 0), Direction.UP);
        writer.assignLockedUVCoordinates(0);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(4);
        writer.surface(SURFACE_TOP);
        writer.nominalFace(Direction.UP);
        writer.setupFaceQuad(Direction.UP, new FaceVertex(0, 0, 1), new FaceVertex(1, 0, 1), new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0), Direction.NORTH);
        transform.apply(writer);
        stream.append();

        if (stream.origin()) {
            Polygon reader = stream.reader();

            do
                target.accept(reader);
            while (stream.next());
        }
        stream.release();
    }
}
