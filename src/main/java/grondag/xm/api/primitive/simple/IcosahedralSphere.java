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

import grondag.xm.Xm;
import grondag.xm.api.mesh.WritableMesh;
import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.mesh.XmMeshes;
import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.mesh.polygon.PolyTransform;
import grondag.xm.api.mesh.polygon.Polygon;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import net.minecraft.util.math.Vec3d;

@API(status = EXPERIMENTAL)
public class IcosahedralSphere {
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

    public static final XmSurface SURFACE_ALL = SURFACES.get(0);

    static final Function<PolyTransform, XmMesh> POLY_FACTORY = transform -> {
        final WritableMesh icoMesh = XmMeshes.claimWritable();
        icoMesh.writer().lockUV(0, false);
        icoMesh.writer().surface(SURFACE_ALL);
        icoMesh.saveDefaults();
        Icosahedron.icosahedron(Vec3d.ZERO, 0.5, icoMesh, true);
        
        icoMesh.origin();
        final Polygon reader = icoMesh.reader();
        final WritableMesh result = XmMeshes.claimWritable();
        
        do {
            subdivideAndEmit(reader, result);
        } while (icoMesh.next());
        
        icoMesh.release();
        return result.releaseToReader();
    };
    
    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.NONE)
            .build(Xm.idString("ico_sphere"));
    
    static void subdivideAndEmit(Polygon poly, WritableMesh output) {
        final MutablePolygon writer = output.writer();
        
        final float xCenter = (poly.x(0) + poly.x(1) + poly.x(2)) / 3;
        final float yCenter = (poly.y(0) + poly.y(1) + poly.y(2)) / 3;
        final float zCenter = (poly.z(0) + poly.z(1) + poly.z(2)) / 3;
        final float uCenter = (poly.u(0, 0) + poly.u(1, 0) + poly.u(2, 0)) / 3;
        final float vCenter = (poly.v(0, 0) + poly.v(1, 0) + poly.v(2, 0)) / 3;
        
        output.setVertexCount(3);
        writer.copyFrom(poly, false);
        writer.copyVertexFrom(0, poly, 0);
        writer.copyVertexFrom(1, poly, 1);
        writer.pos(2, xCenter, yCenter, zCenter);
        writer.uv(2, 0, uCenter, vCenter);
        writer.color(2, 0, 0xFFFFFFFF);
        scalePoly(writer);
        writer.normal(2, writer.x(2) * 2, writer.y(2) * 2, writer.z(2) * 2);
        writer.translate(0.5f);
        output.append();
        
        output.setVertexCount(3);
        writer.copyFrom(poly, false);
        writer.copyVertexFrom(0, poly, 1);
        writer.copyVertexFrom(1, poly, 2);
        writer.pos(2, xCenter, yCenter, zCenter);
        writer.uv(2, 0, uCenter, vCenter);
        writer.color(2, 0, 0xFFFFFFFF);
        scalePoly(writer);
        writer.normal(2, writer.x(2) * 2, writer.y(2) * 2, writer.z(2) * 2);
        writer.translate(0.5f);
        output.append();
        
        output.setVertexCount(3);
        writer.copyFrom(poly, false);
        writer.copyVertexFrom(0, poly, 2);
        writer.copyVertexFrom(1, poly, 0);
        writer.pos(2, xCenter, yCenter, zCenter);
        writer.uv(2, 0, uCenter, vCenter);
        writer.color(2, 0, 0xFFFFFFFF);
        scalePoly(writer);
        writer.normal(2, writer.x(2) * 2, writer.y(2) * 2, writer.z(2) * 2);
        writer.translate(0.5f);
        output.append();
    }
    
    static void scalePoly(MutablePolygon poly) {
        // is at orgin, so position is essentially a vector
        final int limit = poly.vertexCount();
        for(int i = 0; i < limit; i++) {
            scaleVertex(poly, i);
        }
    }
    
    static void scaleVertex(MutablePolygon poly, int i) {
        final float x = poly.x(i);
        final float y = poly.y(i);
        final float z = poly.z(i);
        final float scale = (float) (0.5 / Math.sqrt(x * x + y * y + z * z));
        poly.pos(i, x * scale, y * scale, z * scale);
    }
}
