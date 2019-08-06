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
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.mesh.helper.FaceVertex;
import grondag.xm.mesh.helper.PolyTransform;
import grondag.xm.mesh.polygon.IMutablePolygon;
import grondag.xm.mesh.polygon.IPolygon;
import grondag.xm.mesh.stream.IWritablePolyStream;
import grondag.xm.mesh.stream.PolyStreams;
import net.minecraft.util.math.Direction;

public class WedgePrimitive extends AbstractWedgePrimitive {
    public WedgePrimitive(String idString) {
        super(idString);
    }

    @Override
    public void produceQuads(ModelState modelState, Consumer<IPolygon> target) {
        // Axis for this shape is through the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y axis with full sides against north/down faces.

        // PERF: caching
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon writer = stream.writer();

        PolyTransform transform = PolyTransform.get(modelState);

        writer.setRotation(0, Rotation.ROTATE_NONE);
        writer.setLockUV(0, true);
        stream.saveDefaults();

        writer.surface(SURFACE_BOTTOM);
        writer.setNominalFace(Direction.NORTH);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        transform.apply(writer);
        stream.append();

        writer.surface(SURFACE_BOTTOM);
        writer.setNominalFace(Direction.DOWN);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(3);
        writer.surface(SURFACE_SIDES);
        writer.setNominalFace(Direction.EAST);
        writer.setupFaceQuad(Direction.EAST, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0), new FaceVertex(1, 1, 0), Direction.UP);
        writer.assignLockedUVCoordinates(0);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(3);
        writer.surface(SURFACE_SIDES);
        writer.setNominalFace(Direction.WEST);
        writer.setupFaceQuad(Direction.WEST, new FaceVertex(0, 0, 0), new FaceVertex(1, 0, 0), new FaceVertex(0, 1, 0), Direction.UP);
        writer.assignLockedUVCoordinates(0);
        transform.apply(writer);
        stream.append();

        stream.setVertexCount(4);
        writer.surface(SURFACE_TOP);
        writer.setNominalFace(Direction.UP);
        writer.setupFaceQuad(Direction.UP, new FaceVertex(0, 0, 1), new FaceVertex(1, 0, 1), new FaceVertex(1, 1, 0), new FaceVertex(0, 1, 0), Direction.NORTH);
        transform.apply(writer);
        stream.append();

        if (stream.origin()) {
            IPolygon reader = stream.reader();

            do
                target.accept(reader);
            while (stream.next());
        }
        stream.release();
    }
}
