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
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.api.primitive.surface.XmSurface;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import net.minecraft.util.math.Vec3d;

@API(status = EXPERIMENTAL)
public class Icosahedron {
    public static final XmSurfaceList SURFACES = XmSurfaceList.builder().add("back", SurfaceTopology.TILED, XmSurface.FLAG_NONE).build();

    public static final XmSurface SURFACE_ALL = SURFACES.get(0);

    static final Function<PrimitiveState, XmMesh> POLY_FACTORY = modelState -> {
        final WritableMesh mesh = XmMeshes.claimWritable();
        mesh.writer()
        .lockUV(0, false)
        .surface(SURFACE_ALL)
        .saveDefaults();

        icosahedron(new Vec3d(.5, .5, .5), 0.6, mesh, false);
        return mesh.releaseToReader();
    };

    public static final SimplePrimitive INSTANCE = SimplePrimitive.builder()
            .surfaceList(SURFACES)
            .polyFactory(POLY_FACTORY)
            .orientationType(OrientationType.NONE)
            .build(Xm.idString("icosahedron"));

    /**
     * Makes a regular icosahedron, which is a very close approximation to a sphere
     * for most purposes. Loosely based on
     * http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
     *
     * PERF: use primitives instead of Vec3d
     */
    public static void icosahedron(Vec3d center, double radius, WritableMesh mesh, boolean smoothNormals) {
        /** vertex scale */
        final double s = radius / (2 * Math.sin(2 * Math.PI / 5));

        final Vec3d[] vertexes = new Vec3d[12];

        // create 12 vertices of a icosahedron
        final double t = s * (1.0 + Math.sqrt(5.0)) / 2.0;
        int vi = 0;

        vertexes[vi++] = new Vec3d(-s, t, 0).add(center);
        vertexes[vi++] = new Vec3d(s, t, 0).add(center);
        vertexes[vi++] = new Vec3d(-s, -t, 0).add(center);
        vertexes[vi++] = new Vec3d(s, -t, 0).add(center);

        vertexes[vi++] = new Vec3d(0, -s, t).add(center);
        vertexes[vi++] = new Vec3d(0, s, t).add(center);
        vertexes[vi++] = new Vec3d(0, -s, -t).add(center);
        vertexes[vi++] = new Vec3d(0, s, -t).add(center);

        vertexes[vi++] = new Vec3d(t, 0, -s).add(center);
        vertexes[vi++] = new Vec3d(t, 0, s).add(center);
        vertexes[vi++] = new Vec3d(-t, 0, -s).add(center);
        vertexes[vi++] = new Vec3d(-t, 0, s).add(center);

        Vec3d[] normals = null;
        if (smoothNormals) {
            normals = new Vec3d[12];
            for (int i = 0; i < 12; i++) {
                normals[i] = vertexes[i].subtract(center).normalize();
            }
        }

        // create 20 triangles of the icosahedron

        final MutablePolygon writer = mesh.writer();
        writer.vertexCount(3);

        final XmSurface surface = writer.surface();
        if (surface.topology() == SurfaceTopology.TILED) {
            final float uvMax = (float) (2 * s);
            writer.maxU(0, uvMax);
            writer.maxV(0, uvMax);
            writer.uvWrapDistance(uvMax);
        }

        // enable texture randomization
        int salt = 0;
        writer.textureSalt(salt++);
        writer.saveDefaults();

        icosahedronFace(true, 0, 11, 5, vertexes, normals, mesh);
        icosahedronFace(false, 4, 5, 11, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 0, 5, 1, vertexes, normals, mesh);
        icosahedronFace(false, 9, 1, 5, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 0, 1, 7, vertexes, normals, mesh);
        icosahedronFace(false, 8, 7, 1, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 0, 7, 10, vertexes, normals, mesh);
        icosahedronFace(false, 6, 10, 7, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 0, 10, 11, vertexes, normals, mesh);
        icosahedronFace(false, 2, 11, 10, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 5, 4, 9, vertexes, normals, mesh);
        icosahedronFace(false, 3, 9, 4, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 11, 2, 4, vertexes, normals, mesh);
        icosahedronFace(false, 3, 4, 2, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 10, 6, 2, vertexes, normals, mesh);
        icosahedronFace(false, 3, 2, 6, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 7, 8, 6, vertexes, normals, mesh);
        icosahedronFace(false, 3, 6, 8, vertexes, normals, mesh);

        writer.textureSalt(salt++);
        writer.saveDefaults();
        icosahedronFace(true, 1, 9, 8, vertexes, normals, mesh);
        icosahedronFace(false, 3, 8, 9, vertexes, normals, mesh);

    }

    private static void icosahedronFace(boolean topHalf, int p1, int p2, int p3, Vec3d[] points, Vec3d[] normals, WritableMesh mesh) {
        final MutablePolygon writer = mesh.writer();
        if (normals == null) {
            if (topHalf) {
                writer.vertex(0, points[p1], 1, 1, 0xFFFFFFFF, null);
                writer.vertex(1, points[p2], 0, 1, 0xFFFFFFFF, null);
                writer.vertex(2, points[p3], 1, 0, 0xFFFFFFFF, null);
            } else {
                writer.vertex(0, points[p1], 0, 0, 0xFFFFFFFF, null);
                writer.vertex(1, points[p2], 1, 0, 0xFFFFFFFF, null);
                writer.vertex(2, points[p3], 0, 1, 0xFFFFFFFF, null);
            }
        } else {
            if (topHalf) {
                writer.vertex(0, points[p1], 1, 1, 0xFFFFFFFF, normals[p1]);
                writer.vertex(1, points[p2], 0, 1, 0xFFFFFFFF, normals[p2]);
                writer.vertex(2, points[p3], 1, 0, 0xFFFFFFFF, normals[p3]);
            } else {
                writer.vertex(0, points[p1], 0, 0, 0xFFFFFFFF, normals[p1]);
                writer.vertex(1, points[p2], 1, 0, 0xFFFFFFFF, normals[p2]);
                writer.vertex(2, points[p3], 0, 1, 0xFFFFFFFF, normals[p3]);
            }
        }
        // clear face normal if has been set somehow
        writer.clearFaceNormal();
        writer.append();
    }
}
