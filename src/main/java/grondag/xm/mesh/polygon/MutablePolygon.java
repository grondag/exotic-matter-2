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

package grondag.xm.mesh.polygon;

import java.util.List;
import java.util.function.Consumer;

import grondag.fermion.spatial.Rotation;
import grondag.xm.api.surface.XmSurface;
import grondag.xm.mesh.helper.FaceVertex;
import grondag.xm.mesh.helper.QuadHelper;
import grondag.xm.mesh.polygon.MutablePolygon.Helper.UVLocker;
import grondag.xm.mesh.vertex.MutableVertex;
import grondag.xm.mesh.vertex.Vertex3f;
import grondag.xm.mesh.vertex.UnpackedVertex3;
import grondag.xm.mesh.vertex.Vec3f;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface MutablePolygon extends Polygon {
    class Helper {
        @FunctionalInterface
        interface UVLocker {
            void apply(int vertexIndex, int layerIndex, MutablePolygon poly);
        }

        private static final UVLocker[] UVLOCKERS = new UVLocker[6];

        static {
            UVLOCKERS[Direction.EAST.ordinal()] = (v, l, p) -> p.sprite(v, l, 1 - p.z(v), 1 - p.y(v));

            UVLOCKERS[Direction.WEST.ordinal()] = (v, l, p) -> p.sprite(v, l, p.z(v), 1 - p.y(v));

            UVLOCKERS[Direction.NORTH.ordinal()] = (v, l, p) -> p.sprite(v, l, 1 - p.x(v), 1 - p.y(v));

            UVLOCKERS[Direction.SOUTH.ordinal()] = (v, l, p) -> p.sprite(v, l, p.x(v), 1 - p.y(v));

            UVLOCKERS[Direction.DOWN.ordinal()] = (v, l, p) -> p.sprite(v, l, p.x(v), 1 - p.z(v));

            // our default semantic for UP is different than MC
            // "top" is north instead of south
            UVLOCKERS[Direction.UP.ordinal()] = (v, l, p) -> p.sprite(v, l, p.x(v), p.z(v));
        }

        static class ListAdapter implements Consumer<MutablePolygon> {
            List<MutablePolygon> wrapped;

            ListAdapter prepare(List<MutablePolygon> list) {
                wrapped = list;
                return this;
            }

            @Override
            public void accept(MutablePolygon t) {
                wrapped.add(t);
            }
        }

        private static ThreadLocal<Helper> helper = new ThreadLocal<Helper>() {
            @Override
            protected Helper initialValue() {
                return new Helper();
            }
        };

        static Helper get() {
            return helper.get();
        }

        final ListAdapter listAdapter = new ListAdapter();
        final MutableVertex swapVertex = new UnpackedVertex3();
    }

    MutablePolygon spriteVertex(int layerIndex, int vertexIndex, float u, float v, int color, int glow);

    MutablePolygon maxU(int layerIndex, float maxU);

    MutablePolygon maxV(int layerIndex, float maxV);

    MutablePolygon minU(int layerIndex, float minU);

    MutablePolygon minV(int layerIndex, float minV);

    MutablePolygon uvWrapDistance(float uvWrapDistance);

    /**
     * Sets all vertex colors to given color
     */
    default MutablePolygon colorAll(int layerIndex, int color) {
        final int limit = vertexCount();
        for (int i = 0; i < limit; i++) {
            spriteColor(i, layerIndex, color);
        }
        return this;
    }

    MutablePolygon textureSalt(int salt);

    MutablePolygon lockUV(int layerIndex, boolean lockUV);

    MutablePolygon sprite(int layerIndex, String spriteName);

    MutablePolygon rotation(int layerIndex, Rotation rotation);

    MutablePolygon contractUV(int layerIndex, boolean contractUVs);

    MutablePolygon blendMode(int layerIndex, BlockRenderLayer layer);

    MutablePolygon spriteDepth(int layerCount);

    MutablePolygon emissive(int layerIndex, boolean emissive);

    MutablePolygon disableAo(int layerIndex, boolean disable);
    
    MutablePolygon disableDiffuse(int layerIndex, boolean disable);
    
    /**
     * Assumes layer 0
     */
    MutablePolygon vertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow);

    /**
     * Assumes layer 0, sets glow to 0
     */
    default MutablePolygon vertex(int vertexIndex, float x, float y, float z, float u, float v, int color) {
        return vertex(vertexIndex, x, y, z, u, v, color, 0);
    }

    /**
     * Assumes layer 0
     */
    default MutablePolygon vertex(int vertexIndex, float x, float y, float z, float u, float v, int color, float normX, float normY, float normZ) {
        return this.vertex(vertexIndex, normX, normY, normZ, u, v, color).normal(vertexIndex, normX, normY, normZ);
    }

    /**
     * Assumes layer 0, sets glow to 0. Prefer value-passing, floats.
     */
    @Deprecated
    default MutablePolygon vertex(int vertexIndex, Vec3d pos, double u, double v, int color) {
        return vertex(vertexIndex, (float) pos.x, (float) pos.y, (float) pos.z, (float) u, (float) v, color, 0);
    }

    /**
     * Assumes layer 0, sets glow to 0. Prefer value-passing, floats.
     */
    @Deprecated
    default MutablePolygon vertex(int vertexIndex, Vec3d pos, double u, double v, int color, Vec3d normal) {
        MutablePolygon result = vertex(vertexIndex, (float) pos.x, (float) pos.y, (float) pos.z, (float) u, (float) v, color, 0);
        return normal == null ? result : result.normal(vertexIndex, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    MutablePolygon pos(int vertexIndex, float x, float y, float z);

    MutablePolygon pos(int vertexIndex, Vec3f pos);

    MutablePolygon spriteColor(int vertexIndex, int layerIndex, int color);

    MutablePolygon sprite(int vertexIndex, int layerIndex, float u, float v);

    default MutablePolygon spriteU(int vertexIndex, int layerIndex, float u) {
        return this.sprite(vertexIndex, layerIndex, u, this.spriteV(vertexIndex, layerIndex));
    }

    default MutablePolygon spriteV(int vertexIndex, int layerIndex, float v) {
        return this.sprite(vertexIndex, layerIndex, this.spriteU(vertexIndex, layerIndex), v);
    }

    /**
     * glow is clamped to allowed values
     */
    MutablePolygon glow(int vertexIndex, int glow);

    MutablePolygon normal(int vertexIndex, Vec3f normal);

    default MutablePolygon normal(int vertexIndex, Vertex3f normal) {
        return normal(vertexIndex, normal.x(), normal.y(), normal.z());
    }

    MutablePolygon normal(int vertexIndex, float x, float y, float z);

    // TODO: use materials
//    default IMutablePolygon setPipeline( IRenderPipeline pipeline)
//    {
//        setPipelineIndex(pipeline == null ? 0 : pipeline.getIndex());
//        return this;
//    }
//    
//    IMutablePolygon setPipelineIndex(int pipelineIndex);

    MutablePolygon clearFaceNormal();

    /**
     * Same as
     * {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, Direction)}
     * except also sets nominal face to the given face in the start parameter.
     * Returns self for convenience.
     */
    default MutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, Direction topFace) {
        assert (vertexCount() == 4);
        nominalFace(side);
        return setupFaceQuad(tv0, tv1, tv2, tv3, topFace);
    }

    /**
     * Sets up a quad with human-friendly semantics. <br>
     * <br>
     * 
     * topFace establishes a reference for "up" in these semantics. If null, will
     * use default. Depth represents how far recessed into the surface of the face
     * the quad should be. <br>
     * <br>
     * 
     * Vertices should be given counter-clockwise. Ordering of vertices is
     * maintained for future references. (First vertex passed in will be vertex 0,
     * for example.) <br>
     * <br>
     * 
     * UV coordinates will be based on where rotated vertices project onto the
     * nominal face for this quad (effectively lockedUV) unless face vertexes have
     * UV coordinates.
     */
    default MutablePolygon setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2, FaceVertex vertexIn3, Direction topFace) {
        assert vertexCount() <= 4;
        Direction defaultTop = QuadHelper.defaultTopOf(this.nominalFace());
        if (topFace == null)
            topFace = defaultTop;

        FaceVertex rv0;
        FaceVertex rv1;
        FaceVertex rv2;
        FaceVertex rv3;

        if (topFace == defaultTop) {
            rv0 = vertexIn0;
            rv1 = vertexIn1;
            rv2 = vertexIn2;
            rv3 = vertexIn3;
        } else if (topFace == QuadHelper.rightOf(this.nominalFace(), defaultTop)) {
            rv0 = vertexIn0.withXY(vertexIn0.y, 1 - vertexIn0.x);
            rv1 = vertexIn1.withXY(vertexIn1.y, 1 - vertexIn1.x);
            rv2 = vertexIn2.withXY(vertexIn2.y, 1 - vertexIn2.x);
            rv3 = vertexIn3.withXY(vertexIn3.y, 1 - vertexIn3.x);
        } else if (topFace == QuadHelper.bottomOf(this.nominalFace(), defaultTop)) {
            rv0 = vertexIn0.withXY(1 - vertexIn0.x, 1 - vertexIn0.y);
            rv1 = vertexIn1.withXY(1 - vertexIn1.x, 1 - vertexIn1.y);
            rv2 = vertexIn2.withXY(1 - vertexIn2.x, 1 - vertexIn2.y);
            rv3 = vertexIn3.withXY(1 - vertexIn3.x, 1 - vertexIn3.y);
        } else // left of
        {
            rv0 = vertexIn0.withXY(1 - vertexIn0.y, vertexIn0.x);
            rv1 = vertexIn1.withXY(1 - vertexIn1.y, vertexIn1.x);
            rv2 = vertexIn2.withXY(1 - vertexIn2.y, vertexIn2.x);
            rv3 = vertexIn3.withXY(1 - vertexIn3.y, vertexIn3.x);
        }

        switch (this.nominalFace()) {
        case UP:
            vertex(0, rv0.x, 1 - rv0.depth, 1 - rv0.y, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            vertex(1, rv1.x, 1 - rv1.depth, 1 - rv1.y, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            vertex(2, rv2.x, 1 - rv2.depth, 1 - rv2.y, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                vertex(3, rv3.x, 1 - rv3.depth, 1 - rv3.y, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case DOWN:
            vertex(0, rv0.x, rv0.depth, rv0.y, 1 - rv0.u(), 1 - rv0.v(), rv0.color(), rv0.glow());
            vertex(1, rv1.x, rv1.depth, rv1.y, 1 - rv1.u(), 1 - rv1.v(), rv1.color(), rv1.glow());
            vertex(2, rv2.x, rv2.depth, rv2.y, 1 - rv2.u(), 1 - rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                vertex(3, rv3.x, rv3.depth, rv3.y, 1 - rv3.u(), 1 - rv3.v(), rv3.color(), rv3.glow());
            break;

        case EAST:
            vertex(0, 1 - rv0.depth, rv0.y, 1 - rv0.x, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            vertex(1, 1 - rv1.depth, rv1.y, 1 - rv1.x, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            vertex(2, 1 - rv2.depth, rv2.y, 1 - rv2.x, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                vertex(3, 1 - rv3.depth, rv3.y, 1 - rv3.x, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case WEST:
            vertex(0, rv0.depth, rv0.y, rv0.x, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            vertex(1, rv1.depth, rv1.y, rv1.x, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            vertex(2, rv2.depth, rv2.y, rv2.x, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                vertex(3, rv3.depth, rv3.y, rv3.x, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case NORTH:
            vertex(0, 1 - rv0.x, rv0.y, rv0.depth, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            vertex(1, 1 - rv1.x, rv1.y, rv1.depth, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            vertex(2, 1 - rv2.x, rv2.y, rv2.depth, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                vertex(3, 1 - rv3.x, rv3.y, rv3.depth, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case SOUTH:
            vertex(0, rv0.x, rv0.y, 1 - rv0.depth, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            vertex(1, rv1.x, rv1.y, 1 - rv1.depth, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            vertex(2, rv2.x, rv2.y, 1 - rv2.depth, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                vertex(3, rv3.x, rv3.y, 1 - rv3.depth, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;
        }

        //TODO: put back
        if(cullFace() == null) {
            cullFace(computeCullFace());
        }
        
        return this;
    }

    /**
     * Sets up a quad with standard semantics. x0,y0 are at lower left and x1, y1
     * are top right. topFace establishes a reference for "up" in these semantics.
     * Depth represents how far recessed into the surface of the face the quad
     * should be.<br>
     * <br>
     * 
     * Returns self for convenience.<br>
     * <br>
     * 
     * @see #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex,
     *      Direction)
     */
    default MutablePolygon setupFaceQuad(float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        // PERF: garbage factory
        return setupFaceQuad(new FaceVertex(x0, y0, depth), new FaceVertex(x1, y0, depth), new FaceVertex(x1, y1, depth), new FaceVertex(x0, y1, depth),
                topFace);
    }

    /**
     * Same as
     * {@link #setupFaceQuad(double, double, double, double, double, Direction)} but
     * also sets nominal face with given face in start parameter. Returns self as
     * convenience.
     */
    default MutablePolygon setupFaceQuad(Direction face, float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        nominalFace(face);
        return setupFaceQuad(x0, y0, x1, y1, depth, topFace);
    }

    // PERF use float version
    @Deprecated
    public default MutablePolygon setupFaceQuad(Direction face, double x0, double y0, double x1, double y1, double depth, Direction topFace) {
        return this.setupFaceQuad(face, (float) x0, (float) y0, (float) x1, (float) y1, (float) depth, topFace);
    }

    /**
     * Triangular version of
     * {@link #setupFaceQuad(Direction, FaceVertex, FaceVertex, FaceVertex, Direction)}
     */
    default MutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, Direction topFace) {
        assert (vertexCount() == 3);
        nominalFace(side);
        return setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    /**
     * Triangular version of
     * {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, Direction)}
     */
    default MutablePolygon setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, Direction topFace) {
        assert (vertexCount() == 3);
        return setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    public MutablePolygon nominalFace(Direction face);

    public MutablePolygon cullFace(Direction face);
    
    /**
     * Reverses winding order, clears face normal and flips vertex normals if
     * present. Used by CSG. TODO: Do this with streams
     */
    default MutablePolygon flip() {

//        final Helper help = Helper.get();
//        final IMutableVertex swapVertex = help.swapVertex;
//      //  final VertexAdapter.Mutable adapter = help.mutableAdapter;
//        
//        final int vCount = this.vertexCount();
//        final int midPoint = (vCount + 1) / 2;
//        for(int low = 0; low < midPoint; low++)
//        {
//            final int high = vCount - low - 1;
//
//            // flip low vertex normal, or mid-point on odd-numbered polys
//            if(hasVertexNormal(low))
//                setVertexNormal(low, -getVertexNormalX(low), -getVertexNormalY(low), -getVertexNormalZ(low));
//            
//            if(low != high)
//            {
//                // flip high vertex normal, or mid-point on odd-numbered polys
//                if(hasVertexNormal(high))
//                    setVertexNormal(high, -getVertexNormalX(high), -getVertexNormalY(high), -getVertexNormalZ(high));
//                
//                // swap low with high
//                adapter.prepare(this, low);
//                swapVertex.copyFrom(adapter);
//                copyVertexFrom(low, this, high);
//                copyVertexFrom(high, swapVertex);
//            }
//        }
//        
//        clearFaceNormal();

        return this;
    }

    MutablePolygon surface(XmSurface surface);

    /**
     * Adds given offsets to u,v values of each vertex.
     */
    default MutablePolygon offsetVertexUV(int layerIndex, float uShift, float vShift) {
        for (int i = 0; i < this.vertexCount(); i++) {
            final float u = this.spriteU(i, layerIndex) + uShift;
            final float v = this.spriteV(i, layerIndex) + vShift;

            assert u > -QuadHelper.EPSILON : "vertex uv offset out of bounds";
            assert u < 1 + QuadHelper.EPSILON : "vertex uv offset out of bounds";
            assert v > -QuadHelper.EPSILON : "vertex uv offset out of bounds";
            assert v < 1 + QuadHelper.EPSILON : "vertex uv offset out of bounds";

            this.sprite(i, layerIndex, u, v);
        }
        return this;
    }

    /**
     * Copies all attributes that are available in the source poly. DOES NOT retain
     * a reference to the input poly.
     */
    public MutablePolygon copyVertexFrom(int targetIndex, Polygon source, int sourceIndex);

    /**
     * Interpolates all attributes that are available in the source poly. Weight = 0
     * gives source 0, weight = 1 gives source 1, with values in between giving
     * blended results. DOES NOT retain a reference to either input poly.
     */
    public default MutablePolygon copyInterpolatedVertexFrom(int targetIndex, Polygon from, int fromIndex, Polygon to, int toIndex, float toWeight) {
        // TODO: convert to streams or remove

//        Helper help = Helper.get();
//        
//        help.fromAdapter.prepare(from, fromIndex).interpolate(
//                help.toAdapter.prepare(to, toIndex), 
//                toWeight, 
//                help.mutableAdapter.prepare(this, targetIndex));

        return this;
    }

    /**
     * Sets all attributes that are available in the source vertex. DOES NOT retain
     * a reference to the input vertex.
     */
    public default MutablePolygon copyVertexFrom(int vertexIndex, MutableVertex source) {
        // TODO: convert to streams or remove
//        Helper.get().mutableAdapter.prepare(this, vertexIndex).copyFrom(source);
        return this;
    }

    public default MutablePolygon scaleFromBlockCenter(float scale) {
        float c = 0.5f * (1 - scale);

        final int limit = this.vertexCount();
        for (int i = 0; i < limit; i++)
            pos(i, x(i) * scale + c, y(i) * scale + c, z(i) * scale + c);

        return this;
    }

    /**
     * if lockUV is on, derive UV coords by projection of vertex coordinates on the
     * plane of the quad's face
     */
    public default MutablePolygon assignLockedUVCoordinates(int layerIndex) {
        UVLocker locker = Helper.UVLOCKERS[nominalFace().ordinal()];

        final int vertexCount = vertexCount();
        for (int i = 0; i < vertexCount; i++)
            locker.apply(i, layerIndex, this);

        return this;
    }

    /**
     * Will copy all poly and poly layer attributes. Layer counts must match. Will
     * copy vertices if requested, but vertex counts must match or will throw
     * exception.
     * <p>
     * 
     * Does not copy links, marks, tags or deleted status.
     */
    void copyFrom(Polygon polyIn, boolean includeVertices);

    default MutablePolygon tag(int tag) {
        throw new UnsupportedOperationException();
    }
}