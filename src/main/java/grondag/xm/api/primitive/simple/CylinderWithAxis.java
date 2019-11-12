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

import java.util.function.Consumer;
import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.Xm;
import grondag.xm.api.mesh.MeshHelper;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.api.texture.TextureOrientation;

@API(status = EXPERIMENTAL)
public class CylinderWithAxis  {
    private CylinderWithAxis() {}

    public static final XmSurfaceList SURFACES = XmSurfaceList.builder()
            .add("ends", SurfaceTopology.CUBIC, XmSurface.FLAG_NONE)
            .add("sides", SurfaceTopology.TILED, XmSurface.FLAG_NONE)
            .build();

    public static final XmSurface SURFACE_ENDS = SURFACES.get(0);
    public static final XmSurface SURFACE_SIDES = SURFACES.get(1);

    static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
        final PolyTransform pt = PolyTransform.get(modelState);

        final WritableMesh mesh = XmMeshes.claimWritable();
        final MutablePolygon writer = mesh.writer();
        writer.colorAll(0, 0xFFFFFFFF);
        writer.lockUV(0, false);
        writer.rotation(0, TextureOrientation.IDENTITY);
        writer.sprite(0, "");
        writer.saveDefaults();

        final Consumer<MutablePolygon> transform = p -> {
            p.nominalFace(p.lightFace()).apply(pt);
        };

        MeshHelper.unitCylinder(mesh.writer(), 16, transform, SURFACE_SIDES, SURFACE_ENDS, SURFACE_ENDS, 3);

        return mesh.releaseToReader();
    };

    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.AXIS)
            .build(Xm.idString("cylinder_axis"));
}
