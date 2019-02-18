package grondag.brocade.primitives.polygon;

import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.Layer;
import grondag.exotic_matter.model.primitives.vertex.PackedVertex2;

public class Polygon2xN extends AbstractLargeImmutablePolygon<Polygon2xN>
{
    @SuppressWarnings("unchecked")
    private static final Layer<Polygon2xN>[] LAYERS = new Layer[2];
    
    static
    {
        LAYERS[0] = new Layer<Polygon2xN>()
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
                
                this.vMinGetter = p -> p.vMin0;
                this.vMinSetter = (p, v) -> p.vMin0 = v;
            }
        };
        
        LAYERS[1] = new Layer<Polygon2xN>()
        {
            {
                this.textureGetter = p -> p.texture1;
                this.textureSetter = (p, v) -> p.texture1 = v;
                
                this.uMaxGetter = p -> p.uMax1;
                this.uMaxSetter = (p, v) -> p.uMax1 = v;
                
                this.uMinGetter = p -> p.uMin1;
                this.uMinSetter = (p, v) -> p.uMin1 = v;
                
                this.vMaxGetter = p -> p.vMax1;
                this.vMaxSetter = (p, v) -> p.vMax1 = v;
                
                this.vMinGetter = p -> p.vMin1;
                this.vMinSetter = (p, v) -> p.vMin1 = v;
            }
        };
    }
    
    private String texture0;
    private float uMin0;
    private float uMax0;
    private float vMin0;
    private float vMax0;
    
    private String texture1;
    private float uMin1;
    private float uMax1;
    private float vMin1;
    private float vMax1;
    
    public Polygon2xN(int vertexCount)
    {
        super(vertexCount, PackedVertex2.class);
    }

    @Override
    public final int layerCount()
    {
        return 2;
    }

    @Override
    protected final Layer<Polygon2xN>[] layerAccess()
    {
        return LAYERS;
    }
}
