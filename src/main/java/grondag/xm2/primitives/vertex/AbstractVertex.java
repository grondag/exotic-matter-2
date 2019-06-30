package grondag.xm2.primitives.vertex;

import grondag.xm2.primitives.polygon.PolygonAccessor.VertexLayer;

public abstract class AbstractVertex<T extends AbstractVertex<T>> implements IMutableVertex {
    protected abstract VertexLayer<T>[] layerVertexArray();

    @SuppressWarnings("unchecked")
    @Override
    public final int getColor(int layerIndex) {
        return layerVertexArray()[layerIndex].colorGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getU(int layerIndex) {
        return layerVertexArray()[layerIndex].uGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final float getV(int layerIndex) {
        return layerVertexArray()[layerIndex].vGetter.get((T) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setColor(int layerIndex, int color) {
        layerVertexArray()[layerIndex].colorSetter.set((T) this, color);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setUV(int layerIndex, float u, float v) {
        layerVertexArray()[layerIndex].uvSetter.set((T) this, u, v);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setU(int layerIndex, float u) {
        layerVertexArray()[layerIndex].uSetter.set((T) this, u);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void setV(int layerIndex, float v) {
        layerVertexArray()[layerIndex].vSetter.set((T) this, v);
    }
}
