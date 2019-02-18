package grondag.brocade.primitives.polygon;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import com.google.common.collect.ImmutableList;

import grondag.acuity.api.IRenderPipeline;
import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.primitives.FaceVertex;
import grondag.exotic_matter.model.primitives.QuadHelper;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon.Helper.UVLocker;
import grondag.exotic_matter.model.primitives.vertex.IMutableVertex;
import grondag.exotic_matter.model.primitives.vertex.IVec3f;
import grondag.exotic_matter.model.primitives.vertex.UnpackedVertex3;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public interface IMutablePolygon extends IPolygon
{
    class Helper
    {
        @FunctionalInterface
        interface UVLocker
        {
            void apply(int vertexIndex, int layerIndex, IMutablePolygon poly);
        }
        
        private static final UVLocker [] UVLOCKERS = new UVLocker[6];
        
        static
        {
            UVLOCKERS[EnumFacing.EAST.ordinal()] = (v, l, p) -> p.setVertexUV(l, v, 1 - p.getVertexZ(v), 1 - p.getVertexY(v));
            
            UVLOCKERS[EnumFacing.WEST.ordinal()] = (v, l, p) -> p.setVertexUV(l, v, p.getVertexZ(v), 1 - p.getVertexY(v));
            
            UVLOCKERS[EnumFacing.NORTH.ordinal()] = (v, l, p) -> p.setVertexUV(l, v, 1 - p.getVertexX(v), 1 - p.getVertexY(v));
            
            UVLOCKERS[EnumFacing.SOUTH.ordinal()] = (v, l, p) -> p.setVertexUV(l, v, p.getVertexX(v), 1 - p.getVertexY(v));
            
            UVLOCKERS[EnumFacing.DOWN.ordinal()] = (v, l, p) -> p.setVertexUV(l, v, p.getVertexX(v), 1 - p.getVertexZ(v));
            
            // our default semantic for UP is different than MC
            // "top" is north instead of south
            UVLOCKERS[EnumFacing.UP.ordinal()] = (v, l, p) -> p.setVertexUV(l, v, p.getVertexX(v), p.getVertexZ(v));
        }
        
        static class ListAdapter implements Consumer<IMutablePolygon>
        {
            List<IMutablePolygon> wrapped;
            
            ListAdapter prepare(List<IMutablePolygon> list)
            {
                wrapped = list;
                return this;
            }
            
            @Override
            public void accept(@SuppressWarnings("null") IMutablePolygon t)
            {
                wrapped.add(t);
            }
        }
        
        private static ThreadLocal<Helper> helper = new ThreadLocal<Helper>()
        {
            @Override
            protected Helper initialValue()
            {
                return new Helper();
            }
        };
        
        static Helper get()
        {
            return helper.get();
        }
        
        final ListAdapter listAdapter = new ListAdapter();
        final VertexAdapter.Mutable mutableAdapter = new VertexAdapter.Mutable();
        final VertexAdapter.Fixed fromAdapter = new VertexAdapter.Fixed();
        final VertexAdapter.Fixed toAdapter = new VertexAdapter.Fixed();
        final Vector4f transform = new Vector4f();
        final IMutableVertex swapVertex = new UnpackedVertex3();
    }

    IMutablePolygon setVertexLayer(int layerIndex, int vertexIndex,float u, float v, int color, int glow);

    IMutablePolygon setMaxU(int layerIndex, float maxU);
    
    IMutablePolygon setMaxV(int layerIndex, float maxV);
    
    IMutablePolygon setMinU(int layerIndex, float minU);

    IMutablePolygon setMinV(int layerIndex, float minV);
    
    /**
     * Sets all vertex colors to given color
     */
    default IMutablePolygon setColor(int layerIndex, int color)
    {
        final int limit = vertexCount();
        for(int i = 0; i < limit; i++)
        {
            setVertexColor(layerIndex, i, color);
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
    default IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color)
    {
        return setVertex(vertexIndex, x, y, z, u, v, color, 0);
    }
    
    /**
     * Assumes layer 0
     */
    default IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color, float normX, float normY, float normZ)
    {
        return this.setVertex(vertexIndex, normX, normY, normZ, u, v, color).setVertexNormal(vertexIndex, normX, normY, normZ);
    }
    
    /**
     * Assumes layer 0, sets glow to 0. 
     * Prefer value-passing, floats.
     */
    @Deprecated
    default IMutablePolygon setVertex(int vertexIndex, Vec3d pos, double u, double v, int color)
    {
        return setVertex(vertexIndex, (float)pos.x, (float)pos.y, (float)pos.z, (float)u, (float)v, color, 0);
    }
    
    /**
     * Assumes layer 0, sets glow to 0. 
     * Prefer value-passing, floats.
     */
    @Deprecated
    default IMutablePolygon setVertex(int vertexIndex, Vec3d pos, double u, double v, int color, @Nullable Vec3d normal)
    {
        IMutablePolygon result = setVertex(vertexIndex, (float)pos.x, (float)pos.y, (float)pos.z, (float)u, (float)v, color, 0);
        return normal == null ? result : result.setVertexNormal(vertexIndex, (float)normal.x, (float)normal.y, (float)normal.z);
    }
    
    IMutablePolygon setVertexPos(int vertexIndex, float x, float y, float z);
    
    IMutablePolygon setVertexPos(int vertexIndex, Vec3f pos);
    
    IMutablePolygon setVertexColor(int layerIndex, int vertexIndex, int color);
    
    IMutablePolygon setVertexUV(int layerIndex, int vertexIndex, float u, float v);
    
    default IMutablePolygon setVertexU(int layerIndex, int vertexIndex, float u)
    {
        return this.setVertexUV(layerIndex, vertexIndex, u, this.getVertexV(layerIndex, vertexIndex));
    }
    
    default IMutablePolygon setVertexV(int layerIndex, int vertexIndex, float v)
    {
        return this.setVertexUV(layerIndex, vertexIndex, this.getVertexU(layerIndex, vertexIndex), v);
    }
    
    /**
     * glow is clamped to allowed values
     */
    IMutablePolygon setVertexGlow(int vertexIndex, int glow);
    
    IMutablePolygon setVertexNormal(int vertexIndex,  @Nullable Vec3f normal);
    
    default IMutablePolygon setVertexNormal(int vertexIndex, IVec3f normal)
    {
        return setVertexNormal(vertexIndex, normal.x(), normal.y(), normal.z());
    }
    
    IMutablePolygon setVertexNormal(int vertexIndex, float x, float y, float z);
    
    default IMutablePolygon setPipeline(@Nullable IRenderPipeline pipeline)
    {
        setPipelineIndex(pipeline == null ? 0 : pipeline.getIndex());
        return this;
    }
    
    IMutablePolygon setPipelineIndex(int pipelineIndex);
    
    IMutablePolygon clearFaceNormal();

    /**
     * Same as {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, EnumFacing)}
     * except also sets nominal face to the given face in the start parameter. 
     * Returns self for convenience.
     */
    default IMutablePolygon setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, FaceVertex tv3, @Nullable EnumFacing topFace)
    {
        assert(vertexCount() == 4);
        setNominalFace(side);
        return setupFaceQuad(tv0, tv1, tv2, tv3, topFace);
    }

    /** 
     * Sets up a quad with human-friendly semantics. <br><br>
     * 
     * topFace establishes a reference for "up" in these semantics. If null, will use default.
     * Depth represents how far recessed into the surface of the face the quad should be. <br><br>
     * 
     * Vertices should be given counter-clockwise.
     * Ordering of vertices is maintained for future references.
     * (First vertex passed in will be vertex 0, for example.) <br><br>
     * 
     * UV coordinates will be based on where rotated vertices project onto the nominal 
     * face for this quad (effectively lockedUV) unless face vertexes have UV coordinates.
     */
    default IMutablePolygon setupFaceQuad(FaceVertex vertexIn0, FaceVertex vertexIn1, FaceVertex vertexIn2, FaceVertex vertexIn3, @Nullable EnumFacing topFace)
    {
        assert vertexCount() <= 4;
        EnumFacing defaultTop = QuadHelper.defaultTopOf(this.getNominalFace());
        if(topFace == null) topFace = defaultTop;
        
        FaceVertex rv0;
        FaceVertex rv1;
        FaceVertex rv2;
        FaceVertex rv3;

        if(topFace == defaultTop)
        {
            rv0 = vertexIn0;
            rv1 = vertexIn1;
            rv2 = vertexIn2;
            rv3 = vertexIn3;
        }
        else if(topFace == QuadHelper.rightOf(this.getNominalFace(), defaultTop))
        {
            rv0 = vertexIn0.withXY(vertexIn0.y, 1 - vertexIn0.x);
            rv1 = vertexIn1.withXY(vertexIn1.y, 1 - vertexIn1.x);
            rv2 = vertexIn2.withXY(vertexIn2.y, 1 - vertexIn2.x);
            rv3 = vertexIn3.withXY(vertexIn3.y, 1 - vertexIn3.x);
        }
        else if(topFace == QuadHelper.bottomOf(this.getNominalFace(), defaultTop))
        {
            rv0 = vertexIn0.withXY(1 - vertexIn0.x, 1 - vertexIn0.y);
            rv1 = vertexIn1.withXY(1 - vertexIn1.x, 1 - vertexIn1.y);
            rv2 = vertexIn2.withXY(1 - vertexIn2.x, 1 - vertexIn2.y);
            rv3 = vertexIn3.withXY(1 - vertexIn3.x, 1 - vertexIn3.y);
        }
        else // left of
        {
            rv0 = vertexIn0.withXY(1 - vertexIn0.y, vertexIn0.x);
            rv1 = vertexIn1.withXY(1 - vertexIn1.y, vertexIn1.x);
            rv2 = vertexIn2.withXY(1 - vertexIn2.y, vertexIn2.x);
            rv3 = vertexIn3.withXY(1 - vertexIn3.y, vertexIn3.x);
        }

        
        switch(this.getNominalFace())
        {
        case UP:
            setVertex(0, rv0.x, 1-rv0.depth, 1-rv0.y, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.x, 1-rv1.depth, 1-rv1.y, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.x, 1-rv2.depth, 1-rv2.y, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if(vertexCount() == 4) setVertex(3, rv3.x, 1-rv3.depth, 1-rv3.y, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case DOWN:     
            setVertex(0, rv0.x, rv0.depth, rv0.y, 1-rv0.u(), 1-rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.x, rv1.depth, rv1.y, 1-rv1.u(), 1-rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.x, rv2.depth, rv2.y, 1-rv2.u(), 1-rv2.v(), rv2.color(), rv2.glow());
            if(vertexCount() == 4) setVertex(3, rv3.x, rv3.depth, rv3.y, 1-rv3.u(), 1-rv3.v(), rv3.color(), rv3.glow());
            break;

        case EAST:
            setVertex(0, 1-rv0.depth, rv0.y, 1-rv0.x, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, 1-rv1.depth, rv1.y, 1-rv1.x, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, 1-rv2.depth, rv2.y, 1-rv2.x, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if(vertexCount() == 4) setVertex(3, 1-rv3.depth, rv3.y, 1-rv3.x, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case WEST:
            setVertex(0, rv0.depth, rv0.y, rv0.x, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.depth, rv1.y, rv1.x, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.depth, rv2.y, rv2.x, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if(vertexCount() == 4) setVertex(3, rv3.depth, rv3.y, rv3.x, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case NORTH:
            setVertex(0, 1-rv0.x, rv0.y, rv0.depth, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, 1-rv1.x, rv1.y, rv1.depth, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, 1-rv2.x, rv2.y, rv2.depth, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if(vertexCount() == 4) setVertex(3, 1-rv3.x, rv3.y, rv3.depth, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;

        case SOUTH:
            setVertex(0, rv0.x, rv0.y, 1-rv0.depth, rv0.u(), rv0.v(), rv0.color(), rv0.glow());
            setVertex(1, rv1.x, rv1.y, 1-rv1.depth, rv1.u(), rv1.v(), rv1.color(), rv1.glow());
            setVertex(2, rv2.x, rv2.y, 1-rv2.depth, rv2.u(), rv2.v(), rv2.color(), rv2.glow());
            if(vertexCount() == 4) setVertex(3, rv3.x, rv3.y, 1-rv3.depth, rv3.u(), rv3.v(), rv3.color(), rv3.glow());
            break;
        }
        
        return this;
    }
    
    /** 
     * Sets up a quad with standard semantics.  
     * x0,y0 are at lower left and x1, y1 are top right.
     * topFace establishes a reference for "up" in these semantics.
     * Depth represents how far recessed into the surface of the face the quad should be.<br><br>
     * 
     * Returns self for convenience.<br><br>
     * 
     * @see #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, EnumFacing)
     */
    default IMutablePolygon setupFaceQuad(float x0, float y0, float x1, float y1, float depth, @Nullable EnumFacing topFace)
    {
        // PERF: garbage factory
        return setupFaceQuad(
                new FaceVertex(x0, y0, depth),
                new FaceVertex(x1, y0, depth),
                new FaceVertex(x1, y1, depth),
                new FaceVertex(x0, y1, depth), 
                topFace);
    }

    /**
     * Same as {@link #setupFaceQuad(double, double, double, double, double, EnumFacing)}
     * but also sets nominal face with given face in start parameter.  
     * Returns self as convenience.
     */
    default IMutablePolygon setupFaceQuad(EnumFacing face, float x0, float y0, float x1, float y1, float depth, @Nullable EnumFacing topFace)
    {
        setNominalFace(face);
        return setupFaceQuad(x0, y0, x1, y1, depth, topFace);
    }
    
    //PERF use float version
    @Deprecated
    public default IMutablePolygon setupFaceQuad(EnumFacing face, double x0, double y0, double x1, double y1, double depth, @Nullable EnumFacing topFace)
    {
        return this.setupFaceQuad(face, (float)x0, (float)y0, (float)x1, (float)y1, (float)depth, topFace);
    }

    /**
     * Triangular version of {@link #setupFaceQuad(EnumFacing, FaceVertex, FaceVertex, FaceVertex, EnumFacing)}
     */
    default IMutablePolygon setupFaceQuad(EnumFacing side, FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, @Nullable EnumFacing topFace)
    {
        assert(vertexCount() == 3);
        setNominalFace(side);
        return setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }
    
    /**
     * Triangular version of {@link #setupFaceQuad(FaceVertex, FaceVertex, FaceVertex, FaceVertex, EnumFacing)}
     */
    default IMutablePolygon setupFaceQuad(FaceVertex tv0, FaceVertex tv1, FaceVertex tv2, @Nullable EnumFacing topFace)
    {
        assert(vertexCount() == 3);
        return setupFaceQuad(tv0, tv1, tv2, tv2, topFace);
    }

    public IMutablePolygon setNominalFace(EnumFacing face);
    
    /**
     * Reverses winding order, clears face normal and flips vertex normals if present.
     * Used by CSG.
     * TODO: eliminate use of IMutableVertex and then remove them
     */
    default IMutablePolygon flip()
    {

        final Helper help = Helper.get();
        final IMutableVertex swapVertex = help.swapVertex;
        final VertexAdapter.Mutable adapter = help.mutableAdapter;
        
        final int vCount = this.vertexCount();
        final int midPoint = (vCount + 1) / 2;
        for(int low = 0; low < midPoint; low++)
        {
            final int high = vCount - low - 1;

            // flip low vertex normal, or mid-point on odd-numbered polys
            if(hasVertexNormal(low))
                setVertexNormal(low, -getVertexNormalX(low), -getVertexNormalY(low), -getVertexNormalZ(low));
            
            if(low != high)
            {
                // flip high vertex normal, or mid-point on odd-numbered polys
                if(hasVertexNormal(high))
                    setVertexNormal(high, -getVertexNormalX(high), -getVertexNormalY(high), -getVertexNormalZ(high));
                
                // swap low with high
                adapter.prepare(this, low);
                swapVertex.copyFrom(adapter);
                copyVertexFrom(low, this, high);
                copyVertexFrom(high, swapVertex);
            }
        }
        
        clearFaceNormal();
        
        return this;
    }
    
    IMutablePolygon setSurface(Surface surface);

    /**
     * WARNING: releases all polys in the input collection. <br>
     * DO NOT RETAIN REFERENCES TO ANY INPUTS. <br>
     * Returns a new Does NOT split non-quads to quads.
     */
    static ImmutableList<IPolygon> paintAndRelease(Collection<IMutablePolygon> from)
    {
        ImmutableList.Builder<IPolygon> builder = ImmutableList.builder();

        for(IMutablePolygon p : from)
        {
            builder.add(p.toPainted());
            p.release();
        }
        return builder.build();
    }
    
    static void releaseAll(Collection<IMutablePolygon> targets)
    {
        for(IMutablePolygon p : targets)
        {
            p.release();
        }
    }
    
    /**
     * Adds given offsets to u,v values of each vertex.
     */
    default IMutablePolygon offsetVertexUV(int layerIndex, float uShift, float vShift)
    {
        for(int i = 0; i < this.vertexCount(); i++)
        {
            final float u = this.getVertexU(layerIndex, i) + uShift;
            final float v = this.getVertexV(layerIndex, i) + vShift;
            
            assert u > -QuadHelper.EPSILON : "vertex uv offset out of bounds"; 
            assert u < 1 + QuadHelper.EPSILON : "vertex uv offset out of bounds"; 
            assert v > -QuadHelper.EPSILON : "vertex uv offset out of bounds"; 
            assert v < 1 + QuadHelper.EPSILON : "vertex uv offset out of bounds";

            this.setVertexUV(layerIndex, i, u, v);
        }      
        return this;
    }
    
    /**
     * Copies all attributes that are available in the source poly.
     * DOES NOT retain a reference to the input poly.
     */
    public IMutablePolygon copyVertexFrom(int targetIndex, IPolygon source, int sourceIndex);

    /**
     * Interpolates all attributes that are available in the source poly.
     * Weight = 0 gives source 0, weight = 1 gives source 1, with values 
     * in between giving blended results.
     * DOES NOT retain a reference to either input poly.
     */
    public default IMutablePolygon copyInterpolatedVertexFrom(int targetIndex, IPolygon from, int fromIndex, IPolygon to, int toIndex, float toWeight)
    {
        Helper help = Helper.get();
        
        help.fromAdapter.prepare(from, fromIndex).interpolate(
                help.toAdapter.prepare(to, toIndex), 
                toWeight, 
                help.mutableAdapter.prepare(this, targetIndex));
        
        return this;
    }

    /**
     * Sets all attributes that are available in the source vertex.
     * DOES NOT retain a reference to the input vertex.
     */
    public default IMutablePolygon copyVertexFrom(int vertexIndex, IMutableVertex source)
    {
        Helper.get().mutableAdapter.prepare(this, vertexIndex).copyFrom(source);
        return this;
    }
    
    public default IMutablePolygon scaleFromBlockCenter(float scale)
    {
        float c = 0.5f * (1 - scale);
        
        final int limit = this.vertexCount();
        for(int i = 0; i < limit; i++)
            setVertexPos(i, 
                    getVertexX(i) * scale + c,
                    getVertexY(i) * scale + c,
                    getVertexZ(i) * scale + c);
        
        return this;
    }

    //TODO: remove in favor of stream operations
    @Deprecated
    public default IPolygon toPainted()
    {
        return factory().toPainted(this);
    }

    //TODO: remove in favor of stream operations
    /**
     * Wraps list as a consumer and calls {@link #toPaintableQuads(Consumer)}
     */
    @Deprecated
    public default boolean toPaintableQuads(List<IMutablePolygon> list)
    {
        return toPaintableQuads(Helper.get().listAdapter.prepare(list));
    }
    
    //TODO: remove in favor of stream operations
    /**
     * If this poly is a tri or a convex quad, simply passes to consumer and returns false.<p>
     * 
     * If it a concave quad or higher-order polygon, generates new paintables that split this poly
     * into convex quads or tris. If a split occurs, returns true. This instance will be unmodified.<p>
     * 
     * Return value of true signals to release this poly if it is no longer needed for processing.
     */
    @Deprecated
    public default boolean toPaintableQuads(Consumer<IMutablePolygon> consumer)
    {
        if(vertexCount() <= 4 && this.isConvex())
        {
            consumer.accept(this);
            return false;
        }

        int head = vertexCount() - 1;
        int tail = 2;
        IMutablePolygon work = claimCopy(4);
        work.copyVertexFrom(0, this, head);
        work.copyVertexFrom(1, this, 0);
        work.copyVertexFrom(2, this, 1);
        work.copyVertexFrom(3, this, tail);
        consumer.accept(work);

        while(head - tail > 1)
        {
            work = claimCopy(head - tail == 2 ? 3 : 4);
            work.copyVertexFrom(0, this, head);
            work.copyVertexFrom(1, this, tail);
            work.copyVertexFrom(2, this, ++tail);
            if(head - tail > 1)
            {
                work.copyVertexFrom(3, this,--head);
            }
            if(!work.isConvex())
            {
                if(work.toPaintableTris(consumer))
                    work.release();
                else
                    assert false : "Tri split should have returned true to signal quad release.";
            }
            else 
                consumer.accept(work);
        }
        return true;
    }

    /**
     * If this poly is a tri, simply passes to consumer and returns false.<p>
     * 
     * If it a higher-order polygon, generates new paintables that split this poly
     * into tris. If a split occurs, returns true. This instance will be unmodified.<p>
     * 
     * Return value of true signals to release this poly if it is no longer needed for processing.
     */
    public default boolean toPaintableTris(Consumer<IMutablePolygon> consumer)
    {
        if(this.vertexCount() == 3)
        {
            consumer.accept(this);
            return false;
        }
        
        int head = this.vertexCount() - 1;
        int tail = 1;

        IMutablePolygon work = claimCopy(3);
        work.copyVertexFrom(0, this, head);
        work.copyVertexFrom(1, this, 0);
        work.copyVertexFrom(2, this, tail);
        consumer.accept(work);

        while(head - tail > 1)
        {
            work = claimCopy(3);
            work.copyVertexFrom(0, this, head);
            work.copyVertexFrom(1, this, tail);
            work.copyVertexFrom(2, this, ++tail);
            consumer.accept(work);

            if(head - tail > 1)
            {
                work = claimCopy(3);
                work.copyVertexFrom(0, this, head);
                work.copyVertexFrom(1, this, tail);
                work.copyVertexFrom(2, this, --head);
                consumer.accept(work);
            }
        }
        return true;
    }

    /**
     *  if lockUV is on, derive UV coords by projection
     *  of vertex coordinates on the plane of the quad's face
     */
    public default IMutablePolygon assignLockedUVCoordinates(int layerIndex)
    {
        UVLocker locker = Helper.UVLOCKERS[getNominalFace().ordinal()];

        final int vertexCount = vertexCount();
        for(int i = 0; i < vertexCount; i++)
            locker.apply(i, layerIndex, this);
        
        return this;
    }

    public default IMutablePolygon transform(Matrix4f matrix)
    {
        final int vertexCount = this.vertexCount();
        final Vector4f vec4 = Helper.get().transform ;
        // transform vertices
        for(int i = 0; i < vertexCount; i++)
        {
            vec4.set(getVertexX(i), getVertexZ(i), getVertexZ(i), 1);
            matrix.transform(vec4);
            this.setVertexPos(i, vec4.x, vec4.y, vec4.z);
            
            if(this.hasVertexNormal(i))
            {
                vec4.set(getVertexNormalX(i), getVertexNormalZ(i), getVertexNormalZ(i), 1);
                matrix.transform(vec4);
                float normScale = (float) (1f / Math.sqrt(vec4.x * vec4.x + vec4.y * vec4.y + vec4.z * vec4.z));
                this.setVertexNormal(i, vec4.x * normScale, vec4.y * normScale, vec4.z * normScale);
            }
        }
        
        // transform nominal face
        // our matrix transform has block center as its origin,
        // so need to translate face vectors to/from block center 
        // origin before/applying matrix.
        final EnumFacing nomFace = this.getNominalFace();
        final Vec3i curNorm = nomFace.getDirectionVec();
        vec4.set(curNorm.getX() + 0.5f, curNorm.getY() + 0.5f, curNorm.getZ() + 0.5f, 1);
        matrix.transform(vec4);
        vec4.x -= 0.5;
        vec4.y -= 0.5;
        vec4.z -= 0.5;
        this.setNominalFace(QuadHelper.computeFaceForNormal(vec4));
        return this;
    }

    /**
     * Will copy all poly and poly layer attributes. Layer counts must match.
     * Will copy vertices if requested, but vertex counts must match or will throw exception.<p>
     * 
     * Does not copy links, marks, tags or deleted status.  
     */
    void copyFrom(IPolygon polyIn, boolean includeVertices);
}
