package grondag.brocade.primitives.polygon;

import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.Layer;
import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.VertexLayer;

public class Polygon1x4 extends AbstractPolygonNx4<Polygon1x4>
{
    @SuppressWarnings("unchecked")
    private static final Layer<Polygon1x4>[] LAYERS = new Layer[1];
    
    static
    {
        LAYERS[0] = new Layer<Polygon1x4>()
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
    
    @SuppressWarnings("unchecked")
    private static final VertexLayer<Polygon1x4>[][] VERTEX_LAYERS = new VertexLayer[1][4];
    
    static
    {
        VERTEX_LAYERS[0][0] = new VertexLayer<Polygon1x4>()
        {
            {
                this.colorGetter = v -> v.color0_0;
                this.colorSetter = (v, color) -> v.color0_0 = color;
                
                this.uGetter = v -> v.u0_0;
                this.uSetter = (v, u) -> v.u0_0 = u;
                
                this.vGetter = v -> v.v0_0;
                this.vSetter = (v, vVal) -> v.v0_0 = vVal;
                
                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u0_0 = u;
                    v.v0_0 = vVal;
                };
            }
        };
        
        VERTEX_LAYERS[0][1] = new VertexLayer<Polygon1x4>()
        {
            {
                this.colorGetter = v -> v.color0_2;
                this.colorSetter = (v, color) -> v.color0_2 = color;
                
                this.uGetter = v -> v.u0_2;
                this.uSetter = (v, u) -> v.u0_2 = u;
                
                this.vGetter = v -> v.v0_2;
                this.vSetter = (v, vVal) -> v.v0_2 = vVal;
                
                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u0_2 = u;
                    v.v0_2 = vVal;
                };
            }
        };
        
        VERTEX_LAYERS[0][2] = new VertexLayer<Polygon1x4>()
        {
            {
                this.colorGetter = v -> v.color0_1;
                this.colorSetter = (v, color) -> v.color0_1 = color;
                
                this.uGetter = v -> v.u0_1;
                this.uSetter = (v, u) -> v.u0_1 = u;
                
                this.vGetter = v -> v.v0_1;
                this.vSetter = (v, vVal) -> v.v0_1 = vVal;
                
                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u0_1 = u;
                    v.v0_1 = vVal;
                };
            }
        };
        
        VERTEX_LAYERS[0][3] = new VertexLayer<Polygon1x4>()
        {
            {
                this.colorGetter = v -> v.color0_3;
                this.colorSetter = (v, color) -> v.color0_3 = color;
                
                this.uGetter = v -> v.u0_3;
                this.uSetter = (v, u) -> v.u0_3 = u;
                
                this.vGetter = v -> v.v0_3;
                this.vSetter = (v, vVal) -> v.v0_3 = vVal;
                
                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u0_3 = u;
                    v.v0_3 = vVal;
                };
            }
        };
    }
    
    private String texture0;
    private float uMin0;
    private float uMax0;
    private float vMin0;
    private float vMax0;
    
    private float u0_0;
    private float v0_0;
    private int color0_0 = 0xFFFFFFFF;
    
    private float u0_1;
    private float v0_1;
    private int color0_1 = 0xFFFFFFFF;
    
    private float u0_2;
    private float v0_2;
    private int color0_2 = 0xFFFFFFFF;
    
    private float u0_3;
    private float v0_3;
    private int color0_3 = 0xFFFFFFFF;
    
    @Override
    public final int layerCount()
    {
        return 1;
    }

    @Override
    protected final VertexLayer<Polygon1x4>[][] layerVertexArray()
    {
        return VERTEX_LAYERS;
    }

    @Override
    protected final Layer<Polygon1x4>[] layerAccess()
    {
        return LAYERS;
    }
}
