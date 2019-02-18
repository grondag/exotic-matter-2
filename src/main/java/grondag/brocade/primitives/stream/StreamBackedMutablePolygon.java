package grondag.brocade.primitives.stream;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.painting.Surface;
import grondag.exotic_matter.model.primitives.polygon.IMutablePolygon;
import grondag.exotic_matter.model.primitives.polygon.IPolygon;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;
import grondag.exotic_matter.world.Rotation;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

public class StreamBackedMutablePolygon extends StreamBackedPolygon implements IMutablePolygon
{
    @Override
    public final IMutablePolygon setVertexLayer(int layerIndex, int vertexIndex, float u, float v, int color, int glow)
    {
        vertexIndex = vertexIndexer.apply(vertexIndex);
        setVertexColor(layerIndex, vertexIndex, color);
        setVertexUV(layerIndex, vertexIndex, u, v);
        setVertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public final IMutablePolygon setMaxU(int layerIndex, float maxU)
    {
        polyEncoder.setMaxU(stream, baseAddress, layerIndex, maxU);
        return this;
    }

    @Override
    public final IMutablePolygon setMaxV(int layerIndex, float maxV)
    {
        polyEncoder.setMaxV(stream, baseAddress, layerIndex, maxV);
        return this;
    }

    @Override
    public final IMutablePolygon setMinU(int layerIndex, float minU)
    {
        polyEncoder.setMinU(stream, baseAddress, layerIndex, minU);
        return this;
    }

    @Override
    public final IMutablePolygon setMinV(int layerIndex, float minV)
    {
        polyEncoder.setMinV(stream, baseAddress, layerIndex, minV);
        return this;
    }

    @Override
    public final IMutablePolygon setTextureSalt(int salt)
    {
        StaticEncoder.setTextureSalt(stream, baseAddress, salt);
        return this;
    }

    @Override
    public final IMutablePolygon setLockUV(int layerIndex, boolean lockUV)
    {
        StaticEncoder.setLockUV(stream, baseAddress, layerIndex, lockUV);
        return this;
    }

    @Override
    public final IMutablePolygon setTextureName(int layerIndex, String textureName)
    {
        polyEncoder.setTextureName(stream, baseAddress, layerIndex, textureName);
        return this;
    }

    @Override
    public final IMutablePolygon setRotation(int layerIndex, Rotation rotation)
    {
        StaticEncoder.setRotation(stream, baseAddress, layerIndex, rotation);
        return this;
    }

    @Override
    public final IMutablePolygon setShouldContractUVs(int layerIndex, boolean contractUVs)
    {
        StaticEncoder.setContractUVs(stream, baseAddress, layerIndex, contractUVs);
        return this;
    }

    @Override
    public final IMutablePolygon setRenderLayer(int layerIndex, BlockRenderLayer layer)
    {
        StaticEncoder.setRenderLayer(stream, baseAddress, layerIndex, layer);
        return this;
    }

    /**
     * Throws exception if not a mutable format.
     */
    @Override
    public final IMutablePolygon setLayerCount(int layerCount)
    {
        final int format = format();
        if(!PolyStreamFormat.isMutable(format))
            throw new UnsupportedOperationException("Cannot change layer count on immutable polygon");
        setFormat(PolyStreamFormat.setLayerCount(format, layerCount));
        return this;
    }

    @Override
    public final IMutablePolygon setEmissive(int layerIndex, boolean emissive)
    {
        StaticEncoder.setEmissive(stream, baseAddress, layerIndex, emissive);
        return this;
    }

    @Override
    public final IMutablePolygon setVertex(int vertexIndex, float x, float y, float z, float u, float v, int color, int glow)
    {
        vertexIndex = vertexIndexer.apply(vertexIndex);
        setVertexPos(vertexIndex, x, y, z);
        setVertexUV(0, vertexIndex, u, v);
        setVertexColor(0, vertexIndex, color);
        setVertexGlow(vertexIndex, glow);
        return this;
    }

    @Override
    public final IMutablePolygon setVertexPos(int vertexIndex, float x, float y, float z)
    {
        vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.apply(vertexIndex), x, y, z);
        return this;
    }

    @Override
    public final IMutablePolygon setVertexPos(int vertexIndex, Vec3f pos)
    {
        vertexEncoder.setVertexPos(stream, vertexAddress, vertexIndexer.apply(vertexIndex), pos.x(), pos.y(), pos.z());
        return this;
    }

    @Override
    public final IMutablePolygon setVertexColor(int layerIndex, int vertexIndex, int color)
    {
        if(vertexEncoder.hasColor())
            vertexEncoder.setVertexColor(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex), color);
        else
            polyEncoder.setVertexColor(stream, baseAddress, layerIndex, color);
        return this;
    }
    
    @Override
    public final IMutablePolygon setVertexU(int layerIndex, int vertexIndex, float u)
    {
        vertexEncoder.setVertexU(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex), u);
        return this;
    }
    
    @Override
    public final IMutablePolygon setVertexV(int layerIndex, int vertexIndex, float v)
    {
        vertexEncoder.setVertexV(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex), v);
        return this;
    }
    
    @Override
    public final IMutablePolygon setVertexUV(int layerIndex, int vertexIndex, float u, float v)
    {
        vertexEncoder.setVertexUV(stream, vertexAddress, layerIndex, vertexIndexer.apply(vertexIndex), u, v);
        return this;
    }

    @Override
    public final IMutablePolygon setVertexGlow(int vertexIndex, int glow)
    {
        glowEncoder.setGlow(stream, glowAddress, vertexIndexer.apply(vertexIndex), glow);
        return this;
    }

    @Override
    public final IMutablePolygon setVertexNormal(int vertexIndex, @Nullable Vec3f normal)
    {
        if(vertexEncoder.hasNormals())
        {
            if(normal == null)
                vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex), Float.NaN, Float.NaN, Float.NaN);
            else
                vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex), normal.x(), normal.y(), normal.z());
        }
        return this;
    }

    @Override
    public final IMutablePolygon setVertexNormal(int vertexIndex, float x, float y, float z)
    {
        if(vertexEncoder.hasNormals())
            vertexEncoder.setVertexNormal(stream, vertexAddress, vertexIndexer.apply(vertexIndex), x, y, z);
        return this;
    }

    @Override
    public final IMutablePolygon setPipelineIndex(int pipelineIndex)
    {
        StaticEncoder.setPipelineIndex(stream, baseAddress, pipelineIndex);
        return this;
    }

    @Override
    public final IMutablePolygon clearFaceNormal()
    {
        int normalFormat = PolyStreamFormat.getFaceNormalFormat(format());
        
        assert normalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED
                : "Face normal clear should only happen for full-precision normals";
        
        polyEncoder.clearFaceNormal(stream, baseAddress);
        return this;
    }

    @Override
    public final IMutablePolygon setNominalFace(EnumFacing face)
    {
        setFormat(PolyStreamFormat.setNominalFace(format(), face));
        return this;
    }

    @Override
    public final IMutablePolygon setSurface(Surface surface)
    {
        StaticEncoder.setSurface(stream, baseAddress, surface);
        return this;
    }

    @Override
    public final IMutablePolygon copyVertexFrom(int targetIndex, IPolygon source, int sourceIndex)
    {
        if(source.hasVertexNormal(sourceIndex))
        {
            assert vertexEncoder.hasNormals();
            setVertexNormal(targetIndex, source.getVertexNormalX(sourceIndex), source.getVertexNormalY(sourceIndex), source.getVertexNormalZ(sourceIndex));
        }
        else if(vertexEncoder.hasNormals())
            setVertexNormal(targetIndex, null);

        setVertexPos(targetIndex, source.getVertexX(sourceIndex), source.getVertexY(sourceIndex), source.getVertexZ(sourceIndex));

        if(glowEncoder.glowFormat() == PolyStreamFormat.VERTEX_GLOW_PER_VERTEX)
            setVertexGlow(targetIndex, source.getVertexGlow(sourceIndex));
        else if(targetIndex == 0 && glowEncoder.glowFormat() == PolyStreamFormat.VERTEX_GLOW_SAME)
            setVertexGlow(0, source.getVertexGlow(sourceIndex));
        
        final int layerCount = source.layerCount();
        assert layerCount <= layerCount();
        
        // do for all vertices even if all the same - slightly wasteful but fewer logic paths
        setVertexColor(0, targetIndex, source.getVertexColor(0, sourceIndex));
        if(layerCount > 1)
        {
            setVertexColor(1, targetIndex, source.getVertexColor(1, sourceIndex));
            
            if(layerCount == 3)
                setVertexColor(2, targetIndex, source.getVertexColor(2, sourceIndex));
        }
        
        setVertexUV(0, targetIndex, source.getVertexU(0, sourceIndex), source.getVertexV(0, sourceIndex));
        if(vertexEncoder.multiUV() && layerCount > 1)
        {
            setVertexUV(1, targetIndex, source.getVertexU(1, sourceIndex), source.getVertexV(1, sourceIndex));
            
            if(layerCount == 3)
                setVertexUV(2, targetIndex, source.getVertexU(2, sourceIndex), source.getVertexV(2, sourceIndex));
        }
        
        return this;
    }

    @Override
    public final void copyFrom(IPolygon polyIn, boolean includeVertices)
    {
        // PERF: make this faster for other stream-based polys
        setNominalFace(polyIn.getNominalFace());
        setPipeline(polyIn.getPipeline());
        setSurface(polyIn.getSurface());
        
        final int faceNormalFormat = PolyStreamFormat.getFaceNormalFormat(format());
        if(faceNormalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_COMPUTED)
                clearFaceNormal();
        else if(faceNormalFormat == PolyStreamFormat.FACE_NORMAL_FORMAT_QUANTIZED)
            polyEncoder.setFaceNormal(stream, faceNormalFormat, polyIn.getFaceNormal());
        
        final int layerCount = polyIn.layerCount();
        assert layerCount == layerCount();
        
        setTextureSalt(polyIn.getTextureSalt());

        for(int l = 0; l < layerCount; l++)
        {
            setMaxU(l, polyIn.getMaxU(l));
            setMaxV(l, polyIn.getMaxV(l));
            setMinU(l, polyIn.getMinU(l));
            setMinV(l, polyIn.getMinV(l));
            setEmissive(l, polyIn.isEmissive(l));
            setRenderLayer(l, polyIn.getRenderLayer(l));
            setLockUV(l, polyIn.isLockUV(l));
            setShouldContractUVs(l, polyIn.shouldContractUVs(l));
            setRotation(l, polyIn.getRotation(l));
            setTextureName(l, polyIn.getTextureName(l));
        }
        
        if(includeVertices)
        {
            final int vertexCount = polyIn.vertexCount();
            if(vertexCount() == vertexCount)
            {
                for(int i = 0; i < vertexCount; i++)
                    this.copyVertexFrom(i, polyIn, i);
            }
            else
                throw new UnsupportedOperationException("Polygon vertex counts must match when copying vertex data.");
            
            
        }
    }
    
    /**
     * Specialized version for CSG operations.
     * Never includes vertex info and does include marks and tags.
     */
    public void copyFromCSG(IPolygon polyIn)
    {
        copyFrom(polyIn, false);
        setTag(polyIn.getTag());
        setMark(polyIn.isMarked());
    }

    public void loadStandardDefaults()
    {
        setMaxU(0, 1f);
        setMaxU(1, 1f);
        setMaxU(2, 1f);
        
        setMaxV(0, 1f);
        setMaxV(1, 1f);
        setMaxV(2, 1f);
        
        clearFaceNormal();        
    }
}
