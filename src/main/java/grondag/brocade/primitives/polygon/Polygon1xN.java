package grondag.brocade.primitives.polygon;

import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.Layer;
import grondag.exotic_matter.model.primitives.vertex.PackedVertex1;

public class Polygon1xN extends AbstractLargeImmutablePolygon<Polygon1xN>
{
    @SuppressWarnings("unchecked")
    private static final Layer<Polygon1xN>[] LAYERS = new Layer[1];
    
    static
    {
        LAYERS[0] = new Layer<Polygon1xN>()
        {
            {
                this.textureGetter = p -> p.texture0;
                this.textureSetter = (p, v) -> p.texture0 = v;
                
                this.uMaxGetter = p -> p.uMax0;
                this.uMaxSetter = (p, v) -> p.uMax0 = v;
                
                this.uMinGetter = p -> p.uMin0;
                this.uMinSetter = (p, v) -> p.uMin0 = v;
                
                this.vMaxGetter = p -> p.vMax0;
                this.vMaxSetter = (p, v) -> p.vMax0 = v;
                
                this.vMaxSetter = (p, v) -> p.vMax0 = v;
                this.vMinGetter = p -> p.vMin0;
                this.vMinSetter = (p, v) -> p.vMin0 = v;
            }
        };
    }
    
    private String texture0;
    private float uMin0;
    private float uMax0;
    private float vMin0;
    private float vMax0;
    
    public Polygon1xN(int vertexCount)
    {
        super(vertexCount, PackedVertex1.class);
    }

    @Override
    public final int layerCount()
    {
        return 1;
    }

    @Override
    protected final Layer<Polygon1xN>[] layerAccess()
    {
        return LAYERS;
    }
}
