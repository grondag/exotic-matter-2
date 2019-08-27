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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.fermion.spatial.Rotation;
import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.MutableSimpleModelState;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

@API(status = EXPERIMENTAL)
public class StackedPlates {
    private StackedPlates() {}
    
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("bottom", SurfaceTopology.CUBIC, XmSurface.FLAG_ALLOW_BORDERS)
            .add("top", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("sides", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE).build();

    public static final XmSurface SURFACE_BOTTOM = SURFACES.get(0);
    public static final XmSurface SURFACE_TOP = SURFACES.get(1);
    public static final XmSurface SURFACE_SIDES = SURFACES.get(2);

    static final Function<SimpleModelState, XmMesh> POLY_FACTORY = modelState -> {
        final PolyTransform transform = PolyTransform.get(modelState);
        
        WritableMesh mesh = XmMeshes.claimWritable();
        MutablePolygon writer = mesh.writer();
        writer.colorAll(0, 0xFFFFFFFF);
        writer.lockUV(0, true);
        writer.rotation(0, Rotation.ROTATE_NONE);
        writer.sprite(0, "");
        mesh.saveDefaults();

        final float height = getHeight(modelState) / 16;
        
        writer.surface(SURFACE_BOTTOM);
        writer.setupFaceQuad(Direction.DOWN, 0, 0, 1, 1, 0, Direction.NORTH);
        transform.accept(writer);
        mesh.append();
        
        writer.surface(SURFACE_TOP);
        writer.setupFaceQuad(Direction.UP, 0, 0, 1, 1, 1 - height, Direction.NORTH);
        transform.accept(writer);
        mesh.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.EAST, 0, 0, 1, height, 0, Direction.UP);
        transform.accept(writer);
        mesh.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.WEST, 0, 0, 1, height, 0, Direction.UP);
        transform.accept(writer);
        mesh.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.NORTH, 0, 0, 1, height, 0, Direction.UP);
        transform.accept(writer);
        mesh.append();
        
        writer.surface(SURFACE_SIDES);
        writer.setupFaceQuad(Direction.SOUTH, 0, 0, 1, height, 0, Direction.UP);
        transform.accept(writer);
        mesh.append();

        return mesh.releaseToReader();
    };

    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.FACE)
            .primitiveBitCount(4)
            .build(Xm.idString("stacked_plates"));

   /**
    * 
    * @param height  1-16
    * @param modelState
    */
    public static void setHeight(int height, MutableSimpleModelState modelState) {
        modelState.primitiveBits(MathHelper.clamp(height, 1, 16) - 1);
    }

    public static int getHeight(SimpleModelState modelState) {
        return modelState.primitiveBits() + 1;
    }
}
