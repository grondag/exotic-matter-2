package grondag.xm2.primitives.polygon;

import java.util.List;
import java.util.function.Consumer;

import grondag.fermion.world.Rotation;
import grondag.xm2.painting.Surface;
import grondag.xm2.primitives.FaceVertex;
import grondag.xm2.primitives.QuadHelper;
import grondag.xm2.primitives.polygon.IMutablePolygon.Helper.UVLocker;
import grondag.xm2.primitives.vertex.IMutableVertex;
import grondag.xm2.primitives.vertex.IVec3f;
import grondag.xm2.primitives.vertex.UnpackedVertex3;
import grondag.xm2.primitives.vertex.Vec3f;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface IMutablePolygon extends IPolygon {
    class Helper {
        @FunctionalInterface
        interface UVLocker {
            void apply(int vertexIndex, int layerIndex, IMutablePolygon poly);
        }

        private static final UVLocker[] UVLOCKERS = new UVLocker[6];

        static {
            UVLOCKERS[Direction.EAST.ordinal()] = (v, l, p) -> p.sprite(v, l, 1 - p.z(v),
                    1 - p.y(v));

            UVLOCKERS[Direction.WEST.ordinal()] = (v, l, p) -> p.sprite(v, l, p.z(v),
                    1 - p.y(v));

            UVLOCKERS[Direction.NORTH.ordinal()] = (v, l, p) -> p.sprite(v, l, 1 - p.x(v),
                    1 - p.y(v));

            UVLOCKERS[Direction.SOUTH.ordinal()] = (v, l, p) -> p.sprite(v, l, p.x(v),
                    1 - p.y(v));

            UVLOCKERS[Direction.DOWN.ordinal()] = (v, l, p) -> p.sprite(v, l, p.x(v),
                    1 - p.z(v));

            // our default semantic for UP is different than MC
            // "top" is north instead of south
            UVLOCKERS[Direction.UP.ordinal()] = (v, l, p) -> p.sprite(v, l, p.x(v), p.z(v));
        }

        static class ListAdapter implements Consumer<IMutablePolygon> {
            List<IMutablePolygon> wrapped;

            ListAdapter prepare(List<IMutablePolygon> list) {
                wrapped = list;
                return this;
            }

            @Override
            public void accept(IMutablePolygon t) {
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
        final IMutableVertex swapVertex = new UnpackedVertex3();
    }

    IMutablePolygon setVertexLayer(int layerIndex, int vertexIndex, float u, float v, int color, int glow);

    IMutablePolygon setMaxU(int layerIndex, float maxU);

    IMutablePolygon setMaxV(int layerIndex, float maxV);

    IMutablePolygon setMinU(int layerIndex, float minU);

    IMutablePolygon setMinV(int layerIndex, float minV);

    /**
     * Sets all vertex colors to given color
     */
    default IMutablePolygon spriteColorAll(int layerIndex, int color) {
        final int limit = vertexCount();
        for (int i = 0; i < limit; i++) {
            spriteColor(i, layerIndex, color);
        }
        return this;
    }

    IMutablePolygon setTextureSalt(int salt);

    IMutablePolygon setLockUV(int layerIndex, boolean lockUV);

    IMutablePolygon setTextureName(int layerIndex, String textureName);

    IMutablePolygon setRotation(int layerIndex, Rotation rotation);

    IMutablePolygon setShouldContractUVs(int layerIndex, boolean contractUVs);

    IMutablePolygon setRenderLayer(int layerIndex, BlockRenderLayer layer);

    IMutablePolygon setLayerCount(int layerCount);

    IMutablePolygon setEmissive(int layerIndex, boolean emissive);

    /**
     * Assumes layer 0
     */
    IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow);

    /**
     * Assumes layer 0, sets glow to 0
     */
    default IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color) {
        return setVertex(vertexIndex, x, y, z, u, v, color, 0);
    }

    /**
     * Assumes layer 0
     */
    default IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color,
            float normX, float normY, float normZ) {
        return this.setVertex(vertexIndex, normX, normY, normZ, u, v, color).normal(vertexIndex, normX, normY,
                normZ);
    }

    /**
     * Assumes layer 0, sets glow to 0. Prefer value-passing, floats.
     */
    @Deprecated
    default IMutablePolygon setVertex(int vertexIndex, Vec3d pos, double u, double v, int color) {
        return setVertex(vertexIndex, (float) pos.x, (float) pos.y, (float) pos.z, (float) u, (float) v, color, 0);
    }

    /**
     * Assumes layer 0, sets glow to 0. Prefer value-passing, floats.
     */
    @Deprecated
    default IMutablePolygon setVertex(int vertexIndex, Vec3d pos, double u, double v, int color, Vec3d normal) {
        IMutablePolygon result = setVertex(vertexIndex, (float) pos.x, (float) pos.y, (float) pos.z, (float) u,
                (float) v, color, 0);
        return normal == null ? result
                : result.normal(vertexIndex, (float) normal.x, (float) normal.y, (float) normal.z);
    }

    IMutablePolygon pos(int vertexIndex, float x, float y, float z);

    IMutablePolygon pos(int vertexIndex, Vec3f pos);

    IMutablePolygon spriteColor(int vertexIndex, int layerIndex, int color);

    IMutablePolygon sprite(int vertexIndex, int layerIndex, float u, float v);

    default IMutablePolygon spriteU(int vertexIndex, int layerIndex, float u) {
        return this.sprite(vertexIndex, layerIndex, u, this.spriteV(vertexIndex, layerIndex));
    }

    default IMutablePolygon spriteV(int vertexIndex, int layerIndex, float v) {
        return this.sprite(vertexIndex, layerIndex, this.spriteU(vertexIndex, layerIndex), v);
    }

    /**
     * glow is clamped to allowed values
     */
    IMutablePolygon setVertexGlow(int vertexIndex, int glow);

    IMutablePolygon normal(int vertexIndex, Vec3f normal);

    default IMutablePolygon normal(int vertexIndex, IVec3f normal) {
        return normal(vertexIndex, normal.x(), normal.y(), normal.z());
    }

    IMutablePolygon normal(int vertexIndex, float x, float y, float z);

    // TODO: use materials
//    default IMutablePolygon setPipeline( IRenderPipeline pipeline)
//    {
//        setPipelineIndex(pipeline == null ? 0 : pipeline.getIndex());
//        return this;
//    }
//    
//    IMutablePolygon setPipelineIndex(int pipelineIndex);

    IMutablePolygon clearFaceNormal();

    /**
     * Same as
     * {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, Direction)}
     * except also sets nominal face to the given face in the start parameter.
     * Returns self for convenience.
     */
    default IMutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2,
            FaceVertex tv3, Direction topFace) {
        assert (vertexCount() == 4);
        setNominalFace(side);
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
    default IMutablePolygon setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2,
            FaceVertex vertexIn3, Direction topFace) {
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
            setVertex(0, rv0.x, 1 - rv0.depth, 1 - rv0.y, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.x, 1 - rv1.depth, 1 - rv1.y, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.x, 1 - rv2.depth, 1 - rv2.y, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                setVertex(3, rv3.x, 1 - rv3.depth, 1 - rv3.y, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case DOWN:
            setVertex(0, rv0.x, rv0.depth, rv0.y, 1 - rv0.u(), 1 - rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.x, rv1.depth, rv1.y, 1 - rv1.u(), 1 - rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.x, rv2.depth, rv2.y, 1 - rv2.u(), 1 - rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                setVertex(3, rv3.x, rv3.depth, rv3.y, 1 - rv3.u(), 1 - rv3.v(), rv3.color(), rv3.glow());
            break;

        case EAST:
            setVertex(0, 1 - rv0.depth, rv0.y, 1 - rv0.x, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, 1 - rv1.depth, rv1.y, 1 - rv1.x, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, 1 - rv2.depth, rv2.y, 1 - rv2.x, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                setVertex(3, 1 - rv3.depth, rv3.y, 1 - rv3.x, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case WEST:
            setVertex(0, rv0.depth, rv0.y, rv0.x, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.depth, rv1.y, rv1.x, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.depth, rv2.y, rv2.x, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                setVertex(3, rv3.depth, rv3.y, rv3.x, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case NORTH:
            setVertex(0, 1 - rv0.x, rv0.y, rv0.depth, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, 1 - rv1.x, rv1.y, rv1.depth, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, 1 - rv2.x, rv2.y, rv2.depth, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                setVertex(3, 1 - rv3.x, rv3.y, rv3.depth, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case SOUTH:
            setVertex(0, rv0.x, rv0.y, 1 - rv0.depth, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.x, rv1.y, 1 - rv1.depth, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.x, rv2.y, 1 - rv2.depth, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if (vertexCount() == 4)
                setVertex(3, rv3.x, rv3.y, 1 - rv3.depth, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;
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
    default IMutablePolygon setupFaceQuad(float x0, float y0, float x1, float y1, float depth, Direction topFace) {
        // PERF: garbage factory
        return setupFaceQuad(new FaceVertex(x0, y0, depth), new FaceVertex(x1, y0, depth),
                new FaceVertex(x1, y1, depth), new FaceVertex(x0, y1, depth), topFace);
    }

    /**
     * Same as
     * {@link #setupFaceQuad(double, double, double, double, double, Direction)} but
     * also sets nominal face with given face in start parameter. Returns self as
     * convenience.
     */
    default IMutablePolygon setupFaceQuad(Direction face, float x0, float y0, float x1, float y1, float depth,
            Direction topFace) {
        setNominalFace(face);
        return setupFaceQuad(x0, y0, x1, y1, depth, topFace);
    }

    // PERF use float version
    @Deprecated
    public default IMutablePolygon setupFaceQuad(Direction face, double x0, double y0, double x1, double y1,
            double depth, Direction topFace) {
        return this.setupFaceQuad(face, (float) x0, (float) y0, (float) x1, (float) y1, (float) depth, topFace);
    }

    /**
     * Triangular version of
     * {@link #setupFaceQuad(Direction, FaceVertex, FaceVertex, FaceVertex, Direction)}
     */
    default IMutablePolygon setupFaceQuad(Direction side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2,
            Direction topFace) {
        assert (vertexCount() == 3);
        setNominalFace(side);
        return setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    /**
     * Triangular version of
     * {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, Direction)}
     */
    default IMutablePolygon setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, Direction topFace) {
        assert (vertexCount() == 3);
        return setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    public IMutablePolygon setNominalFace(Direction face);

    /**
     * Reverses winding order, clears face normal and flips vertex normals if
     * present. Used by CSG. TODO: Do this with streams
     */
    default IMutablePolygon flip() {

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

    IMutablePolygon setSurface(Surface surface);

    /**
     * Adds given offsets to u,v values of each vertex.
     */
    default IMutablePolygon offsetVertexUV(int layerIndex, float uShift, float vShift) {
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
    public IMutablePolygon copyVertexFrom(int targetIndex, IPolygon source, int sourceIndex);

    /**
     * Interpolates all attributes that are available in the source poly. Weight = 0
     * gives source 0, weight = 1 gives source 1, with values in between giving
     * blended results. DOES NOT retain a reference to either input poly.
     */
    public default IMutablePolygon copyInterpolatedVertexFrom(int targetIndex, IPolygon from, int fromIndex,
            IPolygon to, int toIndex, float toWeight) {
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
    public default IMutablePolygon copyVertexFrom(int vertexIndex, IMutableVertex source) {
        // TODO: convert to streams or remove
//        Helper.get().mutableAdapter.prepare(this, vertexIndex).copyFrom(source);
        return this;
    }

    public default IMutablePolygon scaleFromBlockCenter(float scale) {
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
    public default IMutablePolygon assignLockedUVCoordinates(int layerIndex) {
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
    void copyFrom(IPolygon polyIn, boolean includeVertices);
    
    default IMutablePolygon tag(int tag) {
        throw new UnsupportedOperationException();
    }
}
