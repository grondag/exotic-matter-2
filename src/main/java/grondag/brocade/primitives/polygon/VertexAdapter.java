package grondag.brocade.primitives.polygon;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.primitives.vertex.IMutableVertex;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;

/**
 * Wraps a poly w/ given vertex to supply IMutableVertex interface.
 * Intent is to use locally or thread-locally to keep allocation overhead down.<p>
 * 
 * Layer counts MUST match or will throw an exception. <p>
 * 
 * Needed because most polys don't have vertex objects internally.<p>
 * 
 * Does not support any methods that return a vertex reference to 
 * ensure safety. Not needed for intended use cases.<p>
 */
public abstract class VertexAdapter<T extends IPolygon> implements IMutableVertex
{
    protected int v;
    protected T p;
    
    public VertexAdapter<T> prepare(T poly, int vertexIndex)
    {
        this.p = poly;
        this.v = vertexIndex;
        return this;
    }
    
    @Override
    public float x()
    {
        return p.getVertexX(v);
    }

    @Override
    public float y()
    {
        return p.getVertexY(v);
    }

    @Override
    public float z()
    {
        return p.getVertexZ(v);
    }

    @Override
    public Vec3f pos()
    {
        return p.getPos(v);
    }

    @Override
    public Vec3f normal()
    {
        return p.getVertexNormal(v);
    }

    @Override
    public float normalX()
    {
        return p.getVertexNormalX(v);
    }

    @Override
    public float normalY()
    {
        return p.getVertexNormalY(v);
    }

    @Override
    public float normalZ()
    {
        return p.getVertexNormalZ(v);
    }

    @Override
    public boolean hasNormal()
    {
        return p.hasVertexNormal(v);
    }

    @Override
    public int getColor(int layerIndex)
    {
        return p.getVertexColor(layerIndex, v);
    }

    @Override
    public int getGlow()
    {
        return p.getVertexGlow(v);
    }

    @Override
    public float getU(int layerIndex)
    {
        return p.getVertexU(layerIndex, v);
    }

    @Override
    public float getV(int layerIndex)
    {
        return p.getVertexV(layerIndex, v);
    }

    @Override
    public int getLayerCount()
    {
        return p.layerCount();
    }

    @Override
    public final IMutableVertex interpolate(IMutableVertex jVertex, float t)
    {
        throw new UnsupportedOperationException();
    }
    
    public static class Fixed extends VertexAdapter<IPolygon>
    {
        @Override
        public Fixed prepare(IPolygon poly, int vertexIndex)
        {
            return (Fixed) super.prepare(poly, vertexIndex);
        }

        @Override
        public void setNormal(@Nullable Vec3f normal)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNormal(float x, float y, float z)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPos(Vec3f pos)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPos(float x, float y, float z)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setColor(int layerIndex, int color)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setGlow(int glow)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setUV(int layerIndex, float u, float v)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setU(int layerIndex, float u)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setV(int layerIndex, float v)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLayerCount(int layerCount)
        {
            throw new UnsupportedOperationException();
        }
    }
    
    public static class Mutable extends VertexAdapter<IMutablePolygon>
    {
        @Override
        public Mutable prepare(IMutablePolygon poly, int vertexIndex)
        {
            return (Mutable) super.prepare(poly, vertexIndex);
        }

        @Override
        public void setNormal(@Nullable Vec3f normal)
        {
            p.setVertexNormal(v, normal);
        }

        @Override
        public void setNormal(float x, float y, float z)
        {
            p.setVertexNormal(v, x, y, z);
        }

        @Override
        public void setPos(Vec3f pos)
        {
            p.setVertexPos(v, pos);
        }

        @Override
        public void setPos(float x, float y, float z)
        {
            p.setVertexPos(v, x, y, z);
        }

        @Override
        public void setColor(int layerIndex, int color)
        {
            p.setVertexColor(layerIndex, v, color);
        }

        @Override
        public void setGlow(int glow)
        {
            p.setVertexGlow(v, glow);
        }

        @Override
        public void setUV(int layerIndex, float u, float v)
        {
            p.setVertexUV(layerIndex, this.v, u, v);
        }

        @Override
        public void setU(int layerIndex, float u)
        {
            p.setVertexU(layerIndex, this.v, u);
        }

        @Override
        public void setV(int layerIndex, float v)
        {
            p.setVertexV(layerIndex, this.v, v);
        }
        
        @Override
        public final void setLayerCount(int layerCount)
        {
            if(p.layerCount() != layerCount)
                throw new UnsupportedOperationException("Layer counts must match when copying to vertex wrapper.");
        }
    }
    
    public static class Inner extends VertexAdapter<AbstractPolygon<?>>
    {
        @Override
        public Inner prepare(AbstractPolygon<?> poly, int vertexIndex)
        {
            return (Inner) super.prepare(poly, vertexIndex);
        }

        @Override
        public void setNormal(@Nullable Vec3f normal)
        {
            p.setVertexNormalImpl(v, normal);
        }

        @Override
        public void setNormal(float x, float y, float z)
        {
            p.setVertexNormalImpl(v, x, y, z);
        }

        @Override
        public void setPos(Vec3f pos)
        {
            p.setVertexPosImpl(v, pos);
        }

        @Override
        public void setPos(float x, float y, float z)
        {
            p.setVertexPosImpl(v, x, y, z);
        }
        
        @Override
        public void setColor(int layerIndex, int color)
        {
            p.setVertexColorImpl(layerIndex, v, color);
        }

        @Override
        public void setGlow(int glow)
        {
            p.setVertexGlowImpl(v, glow);
        }

        @Override
        public void setUV(int layerIndex, float u, float v)
        {
            p.setVertexUVImpl(layerIndex, this.v, u, v);
        }

        @Override
        public void setU(int layerIndex, float u)
        {
            p.setVertexUImpl(layerIndex, this.v, u);
        }

        @Override
        public void setV(int layerIndex, float v)
        {
            p.setVertexVImpl(layerIndex, this.v, v);
        }
        
        @Override
        public final void setLayerCount(int layerCount)
        {
            if(p.layerCount() != layerCount)
                throw new UnsupportedOperationException("Layer counts must match when copying to vertex wrapper.");
        }
    }
}
