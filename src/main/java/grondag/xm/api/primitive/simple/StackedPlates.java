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

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_HAS_AXIS_ORIENTATION;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SPECIES;

import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.Xm;
import grondag.xm.api.mesh.PolyTransform;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.primitive.base.AbstractSimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.mesh.polygon.MutablePolygon;
import grondag.xm.mesh.polygon.Polygon;
import grondag.xm.mesh.stream.PolyStreams;
import grondag.xm.mesh.stream.WritablePolyStream;
import grondag.xm.model.state.SimpleModelStateImpl;
import grondag.xm.model.varia.BlockOrientationType;
import grondag.xm.painting.SurfaceTopology;
import net.minecraft.util.math.Direction;

public class StackedPlates extends AbstractSimplePrimitive {
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

    public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
    public static final XmSurface SURFACE_TOP = SURFACES.get(1);
    public static final XmSurface SURFACE_SIDES = SURFACES.get(2);

    private static final Direction[] HORIZONTAL_FACES = { Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH };

    public static final StackedPlates INSTANCE = new StackedPlates(Xm.idString("stacked_plates"));

    public StackedPlates(String idString) {
        super(idString, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ORIENTATION, SimpleModelStateImpl.FACTORY, s -> SURFACES);
    }

    @Override
    public void produceQuads(SimpleModelState modelState, Consumer<Polygon> target) {
        // FIX: Add height to block/model state once model state refactor is complete
        final int meta = 0; // modelState.getMetaData();
        final PolyTransform transform = PolyTransform.get(modelState);
        final float height = (meta + 1) / 16;

        // PERF: if have a consumer and doing this dynamically - should consumer simply
        // be a stream?
        // Why create a stream just to pipe it to the consumer? Or cache the result.
        final WritablePolyStream stream = PolyStreams.claimWritable();
        final MutablePolygon writer = stream.writer();

        writer.rotation(0, Rotation.ROTATE_NONE);
        writer.lockUV(0, true);
        stream.saveDefaults();

        writer.surface(SURFACE_TOP);
        writer.nominalFace(Direction.UP);
        writer.setupFaceQuad(0, 0, 1, 1, 1 - height, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        for (Direction face : HORIZONTAL_FACES) {
            writer.surface(SURFACE_SIDES);
            writer.nominalFace(face);
            writer.setupFaceQuad(0, 0, 1, height, 0, Direction.UP);
            transform.apply(writer);
            stream.append();
        }

        writer.surface(SURFACE_BOTTOM);
        writer.nominalFace(Direction.DOWN);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
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

    @Override
    public boolean isAdditive() {
        return true;
    }

    @Override
    public BlockOrientationType orientationType(SimpleModelState modelState) {
        return BlockOrientationType.FACE;
    }

    @Override
    public SimpleModelState.Mutable geometricState(SimpleModelState fromState) {
        SimpleModelState.Mutable result = this.newState();
        if(fromState.primitive() instanceof StackedPlates) {
            result.orientationIndex(fromState.orientationIndex());
            result.primitiveBits(fromState.primitiveBits());
        }
        return result;
    }

    @Override
    public boolean doesShapeMatch(SimpleModelState from, SimpleModelState to) {
        return from.primitive() == to.primitive() 
                && from.orientationIndex() == to.orientationIndex()
                && from.primitiveBits() == to.primitiveBits();
    }
}
