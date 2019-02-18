package grondag.brocade.primitives.vertex;

import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.VertexLayer;

public class PackedVertex1 extends AbstractPackedVertex<PackedVertex1>
{
    @SuppressWarnings("unchecked")
    private static final VertexLayer<PackedVertex1>[] LAYERS = new VertexLayer[1];
    
    static
    {
        LAYERS[0] = new VertexLayer<PackedVertex1>()
        {
            {
                this.colorGetter = v -> v.color0;
                this.colorSetter = (v, color) -> v.color0 = color;
                
                this.uGetter = v -> v.u0;
                this.uSetter = (v, u) -> v.u0 = u;
                
                this.vGetter = v -> v.v0;
                this.vSetter = (v, vVal) -> v.v0 = vVal;
                
                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u0 = u;
                    v.v0 = vVal;
                };
            }
        };
    }

    private float u0;
    private float v0;
    private int color0;
    
    @Override
    protected final VertexLayer<PackedVertex1>[] layerVertexArray()
    {
        return (VertexLayer<PackedVertex1>[]) LAYERS;
    }

    @Override
    public int getLayerCount()
    {
        return 1;
    }
}
