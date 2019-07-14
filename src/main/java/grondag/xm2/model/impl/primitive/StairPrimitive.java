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

import grondag.fermion.world.Rotation;
import grondag.xm2.mesh.helper.PolyTransform;
import grondag.xm2.mesh.polygon.IMutablePolygon;
import grondag.xm2.mesh.polygon.IPolygon;
import grondag.xm2.mesh.stream.IWritablePolyStream;
import grondag.xm2.mesh.stream.PolyStreams;
import grondag.xm2.model.impl.state.ModelState;
import net.minecraft.util.math.Direction;

public class StairPrimitive extends AbstractWedgePrimitive {
    @Override
    public void produceQuads(ModelState modelState, Consumer<IPolygon> target) {
        // Axis for this shape is along the face of the sloping surface
        // Four rotations x 3 axes gives 12 orientations - one for each edge of a cube.
        // Default geometry is Y orthogonalAxis with full sides against north/east
        // faces.

        // PERF: if have a consumer and doing this dynamically - should consumer simply be a stream?
        // Why create a stream just to pipe it to the consumer?  Or cache the result.
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon quad = stream.writer();
        
        PolyTransform transform = PolyTransform.get(modelState);

        quad.setRotation(0, Rotation.ROTATE_NONE);
        quad.setLockUV(0, true);
        stream.saveDefaults();

        quad.surface(SURFACE_BACK);
        quad.setNominalFace(Direction.NORTH);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_BOTTOM);
        quad.setNominalFace(Direction.EAST);
        quad.setupFaceQuad(0, 0, 1, 1, 0, Direction.UP);
        transform.apply(quad);
        stream.append();

        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.UP, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.UP, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.UP, 0.5, 0.0, 1.0, 0.5, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        // Splitting sides into three quadrants vs one long strip plus one long quadrant
        // is necessary to avoid AO lighting artifacts. AO is done by vertex, and having
        // a T-junction tends to mess about with the results.

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.DOWN, 0.0, 0.5, 0.5, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.DOWN, 0.5, 0.5, 1.0, 1.0, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.DOWN, 0.0, 0.0, 0.5, 0.5, 0.0, Direction.NORTH);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_SIDES);
        quad.setupFaceQuad(Direction.SOUTH, 0.5, 0.0, 1.0, 1.0, 0.0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_TOP);
        // salt is so cuts appear different from top/front face
        // wedges can't connect textures with adjacent flat blocks consistently anyway,
        // so doesn't hurt them
        quad.setTextureSalt(1);
        quad.setupFaceQuad(Direction.SOUTH, 0.0, 0.0, 0.5, 1.0, 0.5, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_TOP);
        quad.setupFaceQuad(Direction.WEST, 0.0, 0.0, 0.5, 1.0, 0.0, Direction.UP);
        transform.apply(quad);
        stream.append();

        quad.surface(SURFACE_TOP);
        quad.setTextureSalt(1);
        quad.setupFaceQuad(Direction.WEST, 0.5, 0.0, 1.0, 1.0, 0.5, Direction.UP);
        transform.apply(quad);
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
