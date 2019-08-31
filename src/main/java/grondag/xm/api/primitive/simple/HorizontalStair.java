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
import grondag.xm.api.mesh.generator.StairMesh;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;

@API(status = EXPERIMENTAL)
public class HorizontalStair {
    
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("front", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("back", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("side", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .build();

    public static final XmSurface SURFACE_FRONT = SURFACES.get(0);
    public static final XmSurface SURFACE_BACK = SURFACES.get(1);
    public static final XmSurface SURFACE_SIDE = SURFACES.get(2);
    

    static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
        
        final WritableMesh mesh = XmMeshes.claimWritable();
        mesh.writer()
            .colorAll(0, 0xFFFFFFFF)
            .lockUV(0, false)
            .rotation(0, Rotation.ROTATE_NONE)
            .sprite(0, "")
            .saveDefaults();
        
        StairMesh.build(mesh, PolyTransform.get(modelState), false, false, SURFACE_BACK, SURFACE_FRONT, SURFACE_FRONT, SURFACE_BACK, SURFACE_SIDE, SURFACE_SIDE);
        return mesh.releaseToReader();
    };

    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.HORIZONTAL_EDGE)
            .build(Xm.idString("horiz_stair"));
}
