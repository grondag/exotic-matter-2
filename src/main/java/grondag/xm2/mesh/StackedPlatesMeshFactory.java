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
import static grondag.xm2.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;

import java.util.function.Consumer;

import grondag.fermion.world.Rotation;
import grondag.xm2.painting.SurfaceTopology;
import grondag.xm2.primitives.PolyTransform;
import grondag.xm2.primitives.polygon.IMutablePolygon;
import grondag.xm2.primitives.polygon.IPolygon;
import grondag.xm2.primitives.stream.IWritablePolyStream;
import grondag.xm2.primitives.stream.PolyStreams;
import grondag.xm2.state.ModelState;
import grondag.xm2.state.StateFormat;
import grondag.xm2.surface.api.XmSurface;
import grondag.xm2.surface.impl.XmSurfaceImpl;
import grondag.xm2.surface.impl.XmSurfaceImpl.XmSurfaceListImpl;
import net.minecraft.util.math.Direction;

public class StackedPlatesMeshFactory extends MeshFactory {
	public static final XmSurfaceListImpl SURFACES = XmSurfaceImpl.builder()
			.add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
			.add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
			.build();
	
	public static final XmSurfaceImpl SURFACE_BOTTOM = SURFACES.get(0);
	public static final XmSurfaceImpl SURFACE_TOP = SURFACES.get(1);
	public static final XmSurfaceImpl SURFACE_SIDES = SURFACES.get(2);
	
    public StackedPlatesMeshFactory() {
        super(SURFACES, StateFormat.BLOCK, STATE_FLAG_NEEDS_SPECIES | STATE_FLAG_HAS_AXIS | STATE_FLAG_HAS_AXIS_ORIENTATION);
    }

    private static final Direction[] HORIZONTAL_FACES = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};
    
    @Override
    public void produceShapeQuads(ModelState modelState, Consumer<IPolygon> target) {
        final int meta = modelState.getMetaData();
        final PolyTransform transform = PolyTransform.get(modelState);
        final float height = (meta + 1) / 16;
        
        // PERF: if have a consumer and doing this dynamically - should consumer simply be a stream?
        // Why create a stream just to pipe it to the consumer?  Or cache the result.
        final IWritablePolyStream stream = PolyStreams.claimWritable();
        final IMutablePolygon writer = stream.writer();

        writer.setRotation(0, Rotation.ROTATE_NONE);
        writer.setLockUV(0, true);
        stream.saveDefaults();
        
        writer.surface(SURFACE_TOP);
        writer.setNominalFace(Direction.UP);
        writer.setupFaceQuad(0, 0, 1, 1, 1 - height, Direction.NORTH);
        transform.apply(writer);
        stream.append();

        for (Direction face : HORIZONTAL_FACES) {
            writer.surface(SURFACE_SIDES);
            writer.setNominalFace(face);
            writer.setupFaceQuad(0, 0, 1, height, 0, Direction.UP);
            transform.apply(writer);
            stream.append();
        }

        writer.surface(SURFACE_BOTTOM);
        writer.setNominalFace(Direction.DOWN);
        writer.setupFaceQuad(0, 0, 1, 1, 0, Direction.NORTH);
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

    @Override
    public boolean isAdditive() {
        return true;
    }

    @Override
    public boolean isCube(ModelState modelState) {
        return modelState.getMetaData() == 15;
    }

    @Override
    public int geometricSkyOcclusion(ModelState modelState) {
        return modelState.getAxis() == Direction.Axis.Y ? 255 : modelState.getMetaData();
    }

    @Override
    public BlockOrientationType orientationType(ModelState modelState) {
        return BlockOrientationType.FACE;
    }

    @Override
    public int getMetaData(ModelState modelState) {
        return (int) (modelState.getStaticShapeBits() & 0xF);
    }

    @Override
    public void setMetaData(ModelState modelState, int meta) {
        modelState.setStaticShapeBits(meta);
    }

    @Override
    public boolean hasLampSurface(ModelState modelState) {
        return false;
    }

}
