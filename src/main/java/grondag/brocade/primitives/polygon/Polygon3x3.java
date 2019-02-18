package grondag.brocade.primitives.polygon;

import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.Layer;
import grondag.exotic_matter.model.primitives.polygon.PolygonAccessor.VertexLayer;

public class Polygon3x3 extends AbstractPolygonNx3<Polygon3x3>
{
    @SuppressWarnings("unchecked")
    private static final Layer<Polygon3x3>[] LAYERS = new Layer[3];

    static
    {
        LAYERS[0] = new Layer<Polygon3x3>()
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

        LAYERS[1] = new Layer<Polygon3x3>()
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

                this.vMaxSetter = (p, v) -> p.vMax1 = v;
                this.vMinGetter = p -> p.vMin1;
                this.vMinSetter = (p, v) -> p.vMin1 = v;
            }
        };

        LAYERS[2] = new Layer<Polygon3x3>()
        {
            {
                this.textureGetter = p -> p.texture2;
                this.textureSetter = (p, v) -> p.texture2 = v;

                this.uMaxGetter = p -> p.uMax2;
                this.uMaxSetter = (p, v) -> p.uMax2 = v;

                this.uMinGetter = p -> p.uMin2;
                this.uMinSetter = (p, v) -> p.uMin2 = v;

                this.vMaxGetter = p -> p.vMax2;
                this.vMaxSetter = (p, v) -> p.vMax2 = v;

                this.vMaxSetter = (p, v) -> p.vMax2 = v;
                this.vMinGetter = p -> p.vMin2;
                this.vMinSetter = (p, v) -> p.vMin2 = v;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static final VertexLayer<Polygon3x3>[][] VERTEX_LAYERS = new VertexLayer[3][4];

    static
    {
        VERTEX_LAYERS[0][0] = new VertexLayer<Polygon3x3>()
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

        VERTEX_LAYERS[0][1] = new VertexLayer<Polygon3x3>()
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

        VERTEX_LAYERS[0][2] = new VertexLayer<Polygon3x3>()
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

        VERTEX_LAYERS[0][3] = VERTEX_LAYERS[0][0];

        ///////////

        VERTEX_LAYERS[1][0] = new VertexLayer<Polygon3x3>()
        {
            {
                this.colorGetter = v -> v.color1_0;
                this.colorSetter = (v, color) -> v.color1_0 = color;

                this.uGetter = v -> v.u1_0;
                this.uSetter = (v, u) -> v.u1_0 = u;

                this.vGetter = v -> v.v1_0;
                this.vSetter = (v, vVal) -> v.v1_0 = vVal;

                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u1_0 = u;
                    v.v1_0 = vVal;
                };
            }
        };

        VERTEX_LAYERS[1][1] = new VertexLayer<Polygon3x3>()
        {
            {
                this.colorGetter = v -> v.color1_1;
                this.colorSetter = (v, color) -> v.color1_1 = color;

                this.uGetter = v -> v.u1_1;
                this.uSetter = (v, u) -> v.u1_1 = u;

                this.vGetter = v -> v.v1_1;
                this.vSetter = (v, vVal) -> v.v1_1 = vVal;

                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u1_1 = u;
                    v.v1_1 = vVal;
                };
            }
        };

        VERTEX_LAYERS[1][2] = new VertexLayer<Polygon3x3>()
        {
            {
                this.colorGetter = v -> v.color1_2;
                this.colorSetter = (v, color) -> v.color1_2 = color;

                this.uGetter = v -> v.u1_2;
                this.uSetter = (v, u) -> v.u1_2 = u;

                this.vGetter = v -> v.v1_2;
                this.vSetter = (v, vVal) -> v.v1_2 = vVal;

                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u1_2 = u;
                    v.v1_2 = vVal;
                };
            }
        };

        VERTEX_LAYERS[1][3] = VERTEX_LAYERS[1][0];
        
        ///////////

        VERTEX_LAYERS[2][0] = new VertexLayer<Polygon3x3>()
        {
            {
                this.colorGetter = v -> v.color2_0;
                this.colorSetter = (v, color) -> v.color2_0 = color;

                this.uGetter = v -> v.u2_0;
                this.uSetter = (v, u) -> v.u2_0 = u;

                this.vGetter = v -> v.v2_0;
                this.vSetter = (v, vVal) -> v.v2_0 = vVal;

                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u2_0 = u;
                    v.v2_0 = vVal;
                };
            }
        };

        VERTEX_LAYERS[2][1] = new VertexLayer<Polygon3x3>()
        {
            {
                this.colorGetter = v -> v.color2_1;
                this.colorSetter = (v, color) -> v.color2_1 = color;

                this.uGetter = v -> v.u2_1;
                this.uSetter = (v, u) -> v.u2_1 = u;

                this.vGetter = v -> v.v2_1;
                this.vSetter = (v, vVal) -> v.v2_1 = vVal;

                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u2_1 = u;
                    v.v2_1 = vVal;
                };
            }
        };

        VERTEX_LAYERS[2][2] = new VertexLayer<Polygon3x3>()
        {
            {
                this.colorGetter = v -> v.color2_2;
                this.colorSetter = (v, color) -> v.color2_2 = color;

                this.uGetter = v -> v.u2_2;
                this.uSetter = (v, u) -> v.u2_2 = u;

                this.vGetter = v -> v.v2_2;
                this.vSetter = (v, vVal) -> v.v2_2 = vVal;

                this.uvSetter = (v, u, vVal) -> 
                {
                    v.u2_2 = u;
                    v.v2_2 = vVal;
                };
            }
        };

        VERTEX_LAYERS[2][3] = VERTEX_LAYERS[2][0];
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

    private String texture2;
    private float uMin2;
    private float uMax2;
    private float vMin2;
    private float vMax2;

    private float u0_0;
    private float v0_0;
    private int color0_0 = 0xFFFFFFFF;

    private float u0_1;
    private float v0_1;
    private int color0_1 = 0xFFFFFFFF;

    private float u0_2;
    private float v0_2;
    private int color0_2 = 0xFFFFFFFF;

    ////////

    private float u1_0;
    private float v1_0;
    private int color1_0 = 0xFFFFFFFF;

    private float u1_1;
    private float v1_1;
    private int color1_1 = 0xFFFFFFFF;

    private float u1_2;
    private float v1_2;
    private int color1_2 = 0xFFFFFFFF;

    ////////

    private float u2_0;
    private float v2_0;
    private int color2_0 = 0xFFFFFFFF;

    private float u2_1;
    private float v2_1;
    private int color2_1 = 0xFFFFFFFF;

    private float u2_2;
    private float v2_2;
    private int color2_2 = 0xFFFFFFFF;

    @Override
    public int layerCount()
    {
        return 3;
    }

    @Override
    protected final VertexLayer<Polygon3x3>[][] layerVertexArray()
    {
        return VERTEX_LAYERS;
    }

    @Override
    protected final Layer<Polygon3x3>[] layerAccess()
    {
        return LAYERS;
    }

}
