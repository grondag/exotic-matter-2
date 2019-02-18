package grondag.brocade.primitives.polygon;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.Vertex;
import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.VertexLayer;
import grondag.exotic_matter.model.primitives.vertex.Vec3f;

public abstract class AbstractSmallPolygon<T extends AbstractSmallPolygon<T>> extends AbstractPolygon<T>
{
    protected abstract Vertex<T>[] vertexArray();
    protected abstract VertexLayer<T>[][] layerVertexArray();

    @SuppressWarnings("unchecked")
    @Override
    public final Vec3f getVertexNormal(int vertexIndex)
    {
        Vec3f result = vertexArray()[vertexIndex].normalGetter.get((T) this);
        return result == null ? getFaceNormal() : result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public float getVertexNormalX(int vertexIndex)
    {
        float result = vertexArray()[vertexIndex].normXGetter.get((T) this);
        return Float.isNaN(result) ? this.getFaceNormal().x() : result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public float getVertexNormalY(int vertexIndex)
    {
        float result = vertexArray()[vertexIndex].normYGetter.get((T) this);
        return Float.isNaN(result) ? this.getFaceNormal().y() : result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public float getVertexNormalZ(int vertexIndex)
    {
        float result = vertexArray()[vertexIndex].normZGetter.get((T) this);
        return Float.isNaN(result) ? this.getFaceNormal().z() : result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final Vec3f getPos(int vertexIndex)
    {
        return vertexArray()[vertexIndex].posGetter.get((T) this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public float getVertexX(int vertexIndex)
    {
        return vertexArray()[vertexIndex].xGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public float getVertexY(int vertexIndex)
    {
        return vertexArray()[vertexIndex].yGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public float getVertexZ(int vertexIndex)
    {
        return vertexArray()[vertexIndex].zGetter.get((T) this);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public final int getVertexColor(int layerIndex, int vertexIndex)
    {
        return layerVertexArray()[layerIndex][vertexIndex].colorGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final int getVertexGlow(int vertexIndex)
    {
        return vertexArray()[vertexIndex].glowGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getVertexU(int layerIndex, int vertexIndex)
    {
        return layerVertexArray()[layerIndex][vertexIndex].uGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getVertexV(int layerIndex, int vertexIndex)
    {
        return layerVertexArray()[layerIndex][vertexIndex].vGetter.get((T) this);
    }
    
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexPosImpl(int vertexIndex, float x, float y, float z)
    {
        vertexArray()[vertexIndex].xyzSetter.set((T) this, x, y, z);
    }

    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexPosImpl(int vertexIndex, Vec3f pos)
    {
        vertexArray()[vertexIndex].posSetter.set((T) this, pos);
    }
    
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexNormalImpl(int vertexIndex, @Nullable Vec3f normal)
    {
        vertexArray()[vertexIndex].normalSetter.set((T) this, normal);
    }

    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexNormalImpl(int vertexIndex, float x, float y, float z)
    {
        vertexArray()[vertexIndex].normXYZSetter.set((T) this, x, y, z);
    }
    
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexColorImpl(int layerIndex, int vertexIndex, int color)
    {
        layerVertexArray()[layerIndex][vertexIndex].colorSetter.set((T) this, color);
    }

    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexUVImpl(int layerIndex, int vertexIndex, float u, float v)
    {
        layerVertexArray()[layerIndex][vertexIndex].uvSetter.set((T) this, u, v);
    }

    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexUImpl(int layerIndex, int vertexIndex, float u)
    {
        layerVertexArray()[layerIndex][vertexIndex].uSetter.set((T) this, u);
    }
    
    /** supports mutable interface */
    @SuppressWarnings("unchecked")
    @Override
    protected final void setVertexVImpl(int layerIndex, int vertexIndex, float v)
    {
        layerVertexArray()[layerIndex][vertexIndex].vSetter.set((T) this, v);
    }
    
    @Override
    protected void setVertexLayerImpl(int layerIndex, int vertexIndex, float u, float v, int color)
    {
        setVertexColorImpl(layerIndex, vertexIndex, color);
        setVertexUVImpl(layerIndex, vertexIndex, u, v);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setVertexGlowImpl(int vertexIndex, int glow)
    {
        vertexArray()[vertexIndex].glowSetter.set((T)this, glow);
        
    }
}
