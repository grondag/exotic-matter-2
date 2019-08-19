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
package grondag.xm.api.mesh;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.polygon.MutablePolygon;
import grondag.xm.api.paint.SurfaceTopology;
import grondag.xm.api.primitive.surface.XmSurface;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@API(status = EXPERIMENTAL)
public class MeshHelper {

    // TODO: fix or remove
    // NOTE: this is a prototype implementation
    // It's fine for smaller objects, but would likely generate excess polys for big
    // shapes after CSG operations.
    // Also needs better/different texture handling for top and bottom when face
    // diameter is > 1.
    // Will probably need separate version for creating orthogonalAxis-aligned
    // cylinders and cones.
    // Also needs a parameter for minimum slices to reduce poly count on small model
    // parts when appropriate.
    // Right now minimum is fixed at 12.
//    public static List<IPolygon> makeCylinder(Vec3d start, Vec3d end, double startRadius, double endRadius,
//            IMutablePolygon template) {
//        double circumference = Math.PI * Math.max(startRadius, endRadius) * 2;
//        int textureSlices = (int) Math.max(1, Math.round(circumference));
//        int polysPerTextureSlice = 1;
//        while (textureSlices * polysPerTextureSlice < 12)
//            polysPerTextureSlice++;
//        int polySlices = textureSlices * polysPerTextureSlice;
//
//        double length = start.distanceTo(end);
//        int raySlices = (int) Math.ceil(length);
//
//        final Vec3d axisZ = end.subtract(start).normalize();
//        boolean isY = (Math.abs(axisZ.y) > 0.5);
//        final Vec3d axisX = new Vec3d(isY ? 1 : 0, !isY ? 1 : 0, 0).crossProduct(axisZ).normalize();
//        final Vec3d axisY = axisX.crossProduct(axisZ).normalize();
//        IMutablePolygon top = template.claimCopy(polySlices);
//        IMutablePolygon bottom = template.claimCopy(polySlices);
//        IMutablePolygon side = template.claimCopy(4);
//
//        List<IPolygon> results = new ArrayList<>(48);
//
//        for (int i = 0; i < polySlices; i++) {
//            double t0 = i / (double) polySlices, t1 = (i + 1) / (double) polySlices;
//
//            for (int j = 0; j < raySlices; j++) {
//                double rayLength = Math.min(1, length - j);
//                Vec3d centerStart = start.add(axisZ.scale(j));
//                Vec3d centerEnd = start.add(axisZ.scale(j + rayLength));
//
//                double quadStartRadius = Useful.linearInterpolate(startRadius, endRadius, (double) j / raySlices);
//                double quadEndRadius = Useful.linearInterpolate(startRadius, endRadius,
//                        Math.min(1, (double) (j + 1) / raySlices));
//
//                double uStart = ((double) (i % polysPerTextureSlice) / polysPerTextureSlice);
//                double u0 = uStart;
//                double u1 = uStart + 1.0 / polysPerTextureSlice;
//                double v0 = 0;
//                double v1 = rayLength;
//
//                Vec3d n0 = cylNormal(axisX, axisY, t1);
//                Vec3d n1 = cylNormal(axisX, axisY, t0);
//
//                side.setVertex(0, centerStart.add(n0.scale(quadStartRadius)), u0, v0, 0xFFFFFFFF, n0);
//                side.setVertex(1, centerStart.add(n1.scale(quadStartRadius)), u1, v0, 0xFFFFFFFF, n1);
//                side.setVertex(2, centerEnd.add(n1.scale(quadEndRadius)), u1, v1, 0xFFFFFFFF, n1);
//                side.setVertex(3, centerEnd.add(n0.scale(quadEndRadius)), u0, v1, 0xFFFFFFFF, n0);
//                results.add(side.toPainted());
//
//                if (j == 0 || j == raySlices - 1) {
//                    double angle = t0 * Math.PI * 2;
//                    double u = 8.0 + Math.cos(angle) * 8.0;
//                    double v = 8.0 + Math.sin(angle) * 8.0;
//
//                    if (j == 0) {
//                        bottom.setVertex(i, centerStart.add(n0.scale(quadStartRadius)), u, v, 0xFFFFFFFF, null);
//                    }
//                    if (j == raySlices - 1) {
//                        top.setVertex(polySlices - i - 1, centerEnd.add(n0.scale(quadEndRadius)), u, v, 0xFFFFFFFF,
//                                null);
//                    }
//                }
//            }
//
//        }
//
//        results.add(top.toPainted());
//        results.add(bottom.toPainted());
//
//        top.release();
//        bottom.release();
//        side.release();
//        return results;
//    }
//
//    private static Vec3d cylNormal(Vec3d axisX, Vec3d axisY, double slice) {
//        double angle = slice * Math.PI * 2;
//        return axisX.scale(Math.cos(angle)).add(axisY.scale(Math.sin(angle)));
//    }

    /**
     * Makes a regular icosahedron, which is a very close approximation to a sphere
     * for most purposes. Loosely based on
     * http://blog.andreaskahler.com/2009/06/creating-icosphere-mesh-in-code.html
     * 
     * PERF: use primitives instead of Vec3d
     */
    public static void icosahedron(Vec3d center, double radius, WritableMesh stream, boolean smoothNormals) {
        /** vertex scale */
        final double s = radius / (2 * Math.sin(2 * Math.PI / 5));

        Vec3d[] vertexes = new Vec3d[12];

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

        stream.setVertexCount(3);
        MutablePolygon writer = stream.writer();

        XmSurface surface = writer.surface();
        if (surface.topology() == SurfaceTopology.TILED) {
            final float uvMax = (float) (2 * s);
            writer.maxU(0, uvMax);
            writer.maxV(0, uvMax);
            writer.uvWrapDistance(uvMax);
        }

        // enable texture randomization
        int salt = 0;
        writer.textureSalt(salt++);
        stream.saveDefaults();

        icosahedronFace(true, 0, 11, 5, vertexes, normals, stream);
        icosahedronFace(false, 4, 5, 11, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 0, 5, 1, vertexes, normals, stream);
        icosahedronFace(false, 9, 1, 5, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 0, 1, 7, vertexes, normals, stream);
        icosahedronFace(false, 8, 7, 1, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 0, 7, 10, vertexes, normals, stream);
        icosahedronFace(false, 6, 10, 7, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 0, 10, 11, vertexes, normals, stream);
        icosahedronFace(false, 2, 11, 10, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 5, 4, 9, vertexes, normals, stream);
        icosahedronFace(false, 3, 9, 4, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 11, 2, 4, vertexes, normals, stream);
        icosahedronFace(false, 3, 4, 2, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 10, 6, 2, vertexes, normals, stream);
        icosahedronFace(false, 3, 2, 6, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 7, 8, 6, vertexes, normals, stream);
        icosahedronFace(false, 3, 6, 8, vertexes, normals, stream);

        writer.textureSalt(salt++);
        stream.saveDefaults();
        icosahedronFace(true, 1, 9, 8, vertexes, normals, stream);
        icosahedronFace(false, 3, 8, 9, vertexes, normals, stream);

    }

    private static void icosahedronFace(boolean topHalf, int p1, int p2, int p3, Vec3d[] points, Vec3d[] normals, WritableMesh stream) {
        MutablePolygon writer = stream.writer();
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
        stream.append();
    }

    /**
     * Adds box to stream using current stream defaults.
     */
    public static void box(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, WritableMesh stream) {
        MutablePolygon quad = stream.writer();
        stream.setVertexCount(4);
        quad.setupFaceQuad(Direction.UP, 1 - maxX, minZ, 1 - minX, maxZ, 1 - maxY, Direction.SOUTH);
        stream.append();

        stream.setVertexCount(4);
        quad.setupFaceQuad(Direction.DOWN, minX, minZ, maxX, maxZ, minY, Direction.SOUTH);
        stream.append();

        // -X
        stream.setVertexCount(4);
        quad.setupFaceQuad(Direction.WEST, minZ, minY, maxZ, maxY, minX, Direction.UP);
        stream.append();

        // +X
        stream.setVertexCount(4);
        quad.setupFaceQuad(Direction.EAST, 1 - maxZ, minY, 1 - minZ, maxY, 1 - maxX, Direction.UP);
        stream.append();

        // -Z
        stream.setVertexCount(4);
        quad.setupFaceQuad(Direction.NORTH, 1 - maxX, minY, 1 - minX, maxY, minZ, Direction.UP);
        stream.append();

        // +Z
        stream.setVertexCount(4);
        quad.setupFaceQuad(Direction.SOUTH, minX, minY, maxX, maxY, 1 - maxZ, Direction.UP);
        stream.append();
    }

//    /**
//     * This method is intended for boxes that span multiple world blocks. Typically
//     * used with unlocked UV coordinates and tiled surface painter. Will emit quads
//     * with uv min/max outside the 0-1 range. Textures will render 1:1, no wrapping.
//     * 
//     * TODO: incomplete / use polystream
//     */
//    public static List<IPolygon> makeBigBox(IVec3f origin, final float xSize, final float ySize, final float zSize,
//            IMutablePolygon template) {
//        ImmutableList.Builder<IPolygon> builder = ImmutableList.builder();
//
//        final float xEnd = origin.x() + xSize;
//        final float yEnd = origin.y() + ySize;
//        final float zEnd = origin.z() + zSize;
//
//        IMutablePolygon quad = template.claimCopy(4);
//        quad.setLockUV(0, false);
//        quad.setMinU(0, 0);
//        quad.setMaxU(0, xSize);
//        quad.setMinV(0, 0);
//        quad.setMaxV(0, zSize);
//        quad.setNominalFace(Direction.UP);
//        quad.setVertex(0, xEnd, yEnd, origin.z(), 0, 0, 0xFFFFFFFF, 0, 1, 0);
//        quad.setVertex(1, origin.x(), yEnd, origin.z(), 0, 0, 0xFFFFFFFF, 0, 1, 0);
//        quad.setVertex(2, origin.x(), yEnd, zEnd, 0, 0, 0xFFFFFFFF, 0, 1, 0);
//        quad.setVertex(3, xEnd, yEnd, zEnd, 0, 0, 0xFFFFFFFF, 0, 1, 0);
////        quad.setupFaceQuad(Direction.UP, 1 - box.maxX, box.minZ, 1 - box.minX, box.maxZ, 1 - box.maxY, Direction.SOUTH);
//        builder.add(quad);
//
////        quad = Poly.mutable(template);
////        quad.setupFaceQuad(Direction.DOWN, box.minX, box.minZ, box.maxX, box.maxZ, box.minY, Direction.SOUTH);
////        builder.add(quad);
////    
////        //-X
////        quad = Poly.mutable(template);
////        quad.setupFaceQuad(Direction.WEST, box.minZ, box.minY, box.maxZ, box.maxY, box.minX, Direction.UP);
////        builder.add(quad);
////        
////        //+X
////        quad = Poly.mutable(template);
////        quad.setupFaceQuad(Direction.EAST, 1 - box.maxZ, box.minY, 1 - box.minZ, box.maxY, 1 - box.maxX, Direction.UP);
////        builder.add(quad);
////        
////        //-Z
////        quad = Poly.mutable(template);
////        quad.setupFaceQuad(Direction.NORTH, 1 - box.maxX, box.minY, 1 - box.minX, box.maxY, box.minZ, Direction.UP);
////        builder.add(quad);
////        
////        //+Z
////        quad = Poly.mutable(template);
////        quad.setupFaceQuad(Direction.SOUTH, box.minX, box.minY, box.maxX, box.maxY, 1 - box.maxZ, Direction.UP);
////        builder.add(quad);
//
//        return builder.build();
//    }

    // TODO: fix or remove
//    /**
//     * Generates a quad that isn't uv-locked - originally for putting symbols on
//     * MatterPackaging Cubes. Bit of a mess, but thought might get some reuse out of
//     * it, so putting here.
//     * 
//     * @param createMutable  if true will add Paintable (mutable) quads. Painted
//     *                       (immutable) otherwise.
//     * @param rawTextureName should not have mod/blocks prefix
//     * @param top            using semantic coordinates here; 0,0 is lower right of
//     *                       face
//     * @param left
//     * @param size           assuming square box
//     * @param scaleFactor    quads will be scaled out from center by this- use value
//     *                       > 1 to bump out overlays
//     * @param color          color of textures
//     * @param uvFraction     how much of texture to include, starting from u,v 0,0.
//     *                       Pass 1 to include whole texture. Mainly of use when
//     *                       trying to apply big textures to item models and don't
//     *                       want whole thing.
//     * @param contractUVs    should be true for everything except fonts maybe
//     * @param list           your mutable list of quads
//     */
//    @SuppressWarnings("unchecked")
//    public static <T extends IPolygon> void addTextureToAllFaces(boolean createMutable, String rawTextureName,
//            float left, float top, float size, float scaleFactor, int color, boolean contractUVs, float uvFraction,
//            Rotation texturRotation, List<T> list) {
//        IMutablePolygon template = PolyFactory.COMMON_POOL.newPaintable(4)
//                .setTextureName(0, "hard_science:blocks/" + rawTextureName).setLockUV(0, false)
//                .setShouldContractUVs(0, contractUVs);
//
//        float bottom = top - size;
//        float right = left + size;
//
//        FaceVertex[] fv = new FaceVertex[4];
//
//        switch (texturRotation) {
//        case ROTATE_180:
//            fv[0] = new FaceVertex.UV(left, bottom, 0, uvFraction, 0);
//            fv[1] = new FaceVertex.UV(right, bottom, 0, 0, 0);
//            fv[2] = new FaceVertex.UV(right, top, 0, 0, uvFraction);
//            fv[3] = new FaceVertex.UV(left, top, 0, uvFraction, uvFraction);
//            break;
//
//        case ROTATE_270:
//            fv[0] = new FaceVertex.UV(left, bottom, 0, 0, 0);
//            fv[1] = new FaceVertex.UV(right, bottom, 0, 0, uvFraction);
//            fv[2] = new FaceVertex.UV(right, top, 0, uvFraction, uvFraction);
//            fv[3] = new FaceVertex.UV(left, top, 0, uvFraction, 0);
//            break;
//
//        case ROTATE_90:
//            fv[0] = new FaceVertex.UV(left, bottom, 0, uvFraction, uvFraction);
//            fv[1] = new FaceVertex.UV(right, bottom, 0, uvFraction, 0);
//            fv[2] = new FaceVertex.UV(right, top, 0, 0, 0);
//            fv[3] = new FaceVertex.UV(left, top, 0, 0, uvFraction);
//            break;
//
//        case ROTATE_NONE:
//        default:
//            fv[0] = new FaceVertex.UV(left, bottom, 0, 0, uvFraction);
//            fv[1] = new FaceVertex.UV(right, bottom, 0, uvFraction, uvFraction);
//            fv[2] = new FaceVertex.UV(right, top, 0, uvFraction, 0);
//            fv[3] = new FaceVertex.UV(left, top, 0, 0, 0);
//            break;
//
//        }
//
//        for (Direction face : Direction.VALUES) {
//            template.setupFaceQuad(face, fv[0], fv[1], fv[2], fv[3], null);
//            template.scaleFromBlockCenter(scaleFactor);
//            list.add((T) (createMutable ? template.claimCopy() : template.toPainted()));
//        }
//
//        template.release();
//    }
}
