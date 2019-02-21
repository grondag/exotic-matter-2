package grondag.brocade.model.render;

import grondag.brocade.primitives.polygon.IPolygon;
import grondag.brocade.primitives.vertex.IVec3f;
import grondag.fermion.world.Rotation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;

public class QuadBakery {
    // Still fuzzy on how the lightmap coordinates work, but this does the job.
    // It mimics the lightmap that would be returned from a block in full
    // brightness.
    public static final int MAX_LIGHT = 15 * 0x20;
    public static final float MAX_LIGHT_FLOAT = (float) MAX_LIGHT / 0xFFFF;
    public static final float[] LIGHTMAP_FULLBRIGHT = { MAX_LIGHT_FLOAT, MAX_LIGHT_FLOAT };

    /**
     * Temporary Workaround for Forge #5073
     */
    private static final VertexFormat ITEM_ALTERNATE;

    static {
        ITEM_ALTERNATE = new VertexFormat();
        ITEM_ALTERNATE.add(VertexFormats.POSITION_ELEMENT);
        ITEM_ALTERNATE.add(VertexFormats.COLOR_ELEMENT);
        ITEM_ALTERNATE.add(VertexFormats.NORMAL_ELEMENT);
        ITEM_ALTERNATE.add(VertexFormats.PADDING_ELEMENT);
        ITEM_ALTERNATE.add(VertexFormats.UV_ELEMENT);
    }

    private static class Workspace {
        // Dimensions are vertex 0-4 and u/v 0-1.
        final float[][] uvData = new float[4][2];
        final float[][] normalData = new float[4][3];

        /**
         * Generic holder
         */
        final float[] packData = new float[4];
    }

    private static final ThreadLocal<Workspace> workspace = new ThreadLocal<Workspace>() {
        @Override
        protected Workspace initialValue() {
            return new Workspace();
        }
    };

    public static void applyTextureRotation(Rotation rotation, float[][] uvData) {
        switch (rotation) {
        case ROTATE_NONE:
        default:
            break;

        case ROTATE_90:
            for (int i = 0; i < 4; i++) {
                float uOld = uvData[i][0];
                float vOld = uvData[i][1];
                uvData[i][0] = vOld;
                uvData[i][1] = 1 - uOld;
            }
            break;

        case ROTATE_180:
            for (int i = 0; i < 4; i++) {
                float uOld = uvData[i][0];
                float vOld = uvData[i][1];
                uvData[i][0] = 1 - uOld;
                uvData[i][1] = 1 - vOld;
            }
            break;

        case ROTATE_270:
            for (int i = 0; i < 4; i++) {
                float uOld = uvData[i][0];
                float vOld = uvData[i][1];
                uvData[i][0] = 1 - vOld;
                uvData[i][1] = uOld;
            }
            break;

        }
    }

    /**
     * Like {@link #createBakedQuad(int, IPolygon, boolean)} but assumes layerIndex
     * 0;
     */
    public static BakedQuad createBakedQuad(IPolygon raw, boolean forceItemFormat) {
        return createBakedQuad(0, raw, forceItemFormat);
    }

    /**
     * Creates a baked quad - does not mutate the given instance. Will use ITEM
     * vertex format if forceItemFormat is true. Use this for item models. Doing so
     * will disable pre-baked lighting and cause the quad to include normals.
     * <p>
     * 
     * Expects that lightmaps are represented by vertex glow bits. For example, if
     * the quad is full brightness, then glow should be 255 for all vertices. Any
     * transformation to alpha or lightmap that uses glow bits should already be
     * applied by painer before this is called.
     */
    @SuppressWarnings("unused")
    public static BakedQuad createBakedQuad(int layerIndex, IPolygon raw, boolean forceItemFormat) {
        final float spanU = raw.getMaxU(layerIndex) - raw.getMinU(layerIndex);
        final float spanV = raw.getMaxV(layerIndex) - raw.getMinV(layerIndex);

        final Sprite textureSprite = MinecraftClient.getInstance().getSpriteAtlas()
                .getSprite(raw.getTextureName(layerIndex));

        final Workspace w = workspace.get();

        final float[][] uvData = w.uvData;
        final float[][] normalData = w.normalData;
        final float[] packData = w.packData;

        int glowBits = 0;
        for (int v = 0; v < 4; v++) {
            int g = raw.isEmissive(layerIndex) ? 255 : raw.getVertexGlow(v);
            if (g != 0) {
                // round to nearest 0-15
                g = (g + 9) / 17;
                glowBits |= (g << (v * 4));
            }

            uvData[v][0] = raw.getVertexU(layerIndex, v);
            uvData[v][1] = raw.getVertexV(layerIndex, v);

            IVec3f normal = raw.getVertexNormal(v);
            if (normal == null)
                normal = raw.getFaceNormal();
            normal.toArray(normalData[v]);
        }

        // apply texture rotation
        applyTextureRotation(raw.getRotation(layerIndex), uvData);

        // scale UV coordinates to size of texture sub-region
        for (int v = 0; v < 4; v++) {
            uvData[v][0] = raw.getMinU(layerIndex) + spanU * uvData[v][0];
            uvData[v][1] = raw.getMinV(layerIndex) + spanV * uvData[v][1];
        }

        if (raw.shouldContractUVs(layerIndex)) {
            contractUVs(textureSprite, uvData);
        }

        int[] vertexData = new int[28];

        /**
         * The item vertex consumer expects to get Item vertex format. (Includes
         * normal.) But to render lightmap we have to use Block format, which uses two
         * bytes that would normally be used for normals to contain brightness
         * information. Note that this means any per-vertex normals generated by meshes
         * will not be used if the quad is full brightness and not being rendered as an
         * item. This should be OK, because we generally don't care about shading for
         * full-brightness render.
         */
        VertexFormat format = forceItemFormat || glowBits == 0
//                ? net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM
                ? ITEM_ALTERNATE
                : VertexFormats.POSITION_COLOR_UV_LMAP;

        final float spriteMinU = textureSprite.getMinU();
        final float spriteSpanU = textureSprite.getMaxU() - spriteMinU;
        final float spriteMinV = textureSprite.getMinV();
        final float spriteSpanV = textureSprite.getMaxV() - spriteMinV;

        // Convert this to Fabric Rebder API
//        for(int v = 0; v < 4; v++)
//        {
//            for(int e = 0; e < format.getElementCount(); e++)
//            {
//                switch(format.getElement(e).getType())
//                {
//                case POSITION:
//                    raw.getPos(v).toArray(packData);
//                    LightUtil.pack(packData, vertexData, format, v, e);
//                    break;
//
//                case NORMAL: 
//                {
//                    LightUtil.pack(normalData[v], vertexData, format, v, e);
//                    break;
//                }
//                case COLOR:
//                {
//                    final int color = raw.getVertexColor(layerIndex, v);
//                    packData[0] = ((float) (color >> 16 & 0xFF)) / 255f;
//                    packData[1] = ((float) (color >> 8 & 0xFF)) / 255f;
//                    packData[2] = ((float) (color  & 0xFF)) / 255f;
//                    packData[3] = ((float) (color >> 24 & 0xFF)) / 255f;
//                    LightUtil.pack(packData, vertexData, format, v, e);
//                    break;
//                }
//                case UV: 
//                    if(format.getElement(e).getIndex() == 0)
//                    {
//                        // This block handles the normal case: texture UV coordinates
//                        // doing interpolation here vs using sprite methods to avoid wasteful multiply and divide by 16
//                        packData[0] = spriteMinU + uvData[v][0] * spriteSpanU;
//                        packData[1] = spriteMinV + uvData[v][1] * spriteSpanV;
//                        LightUtil.pack(packData, vertexData, format, v, e);
//                    }
//                    else
//                    {
//                        // There are 2 UV elements when we are using a BLOCK vertex format
//                        // The 2nd accepts pre-baked lightmaps.  
//                        final float glow = (float)(((glowBits >> (v * 4)) & 0xF) * 0x20) / 0xFFFF;
//                                
//                        packData[0] = glow;
//                        packData[1] = glow;
//
//                        LightUtil.pack(packData, vertexData, format, v, e);
//                    }
//                    break;
//
//                default:
//                    // NOOP, padding or weirdness
//                }
//            }
//        }
//        
//        BakedQuad quad = format == ITEM_ALTERNATE
//                ? new CachedBakedQuad(vertexData, -1, raw.getActualFace(), textureSprite, true, format)
//                : new LitBakedQuad(vertexData, normalData, -1, raw.getActualFace(), textureSprite, true, format, glowBits);
//        
//        return QuadCache.INSTANCE.getCachedQuad(quad);
        return null;
    }

    /**
     * UV shrinkage amount to prevent visible seams
     */
    public static final float UV_EPS = 1f / 0x100;

    /**
     * Prevents visible seams along quad boundaries due to slight overlap with
     * neighboring textures or empty texture buffer. Borrowed from Forge as
     * implemented by Fry in UnpackedBakedQuad.build(). Array dimensions are vertex
     * 0-3, u/v 0-1
     */
    public static void contractUVs(Sprite textureSprite, float[][] uvData) {
        // TODO: reimplement or scrap

//        float tX = textureSprite.getOriginX() / textureSprite.getMinU();
//        float tY = textureSprite.getOriginY() / textureSprite.getMinV();
//        float tS = tX > tY ? tX : tY;
//        float ep = 1f / (tS * 0x100);
//
//        //uve refers to the uv element number in the format
//        //we will always have uv data directly
//        float center[] = new float[2];
//
//        for(int v = 0; v < 4; v++)
//        {
//            center[0] += uvData[v][0] / 4;
//            center[1] += uvData[v][1] / 4;
//        }
//
//        for(int v = 0; v < 4; v++)
//        {
//            for (int i = 0; i < 2; i++)
//            {
//                float uo = uvData[v][i];
//                float un = uo * (1 - UV_EPS) + center[i] * UV_EPS;
//                float ud = uo - un;
//                float aud = ud;
//                if(aud < 0) aud = -aud;
//                if(aud < ep) // not moving a fraction of a pixel
//                {
//                    float udc = uo - center[i];
//                    if(udc < 0) udc = -udc;
//                    if(udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
//                    {
//                        un = (uo + center[i]) / 2;
//                    }
//                    else // move at least by a fraction
//                    {
//                        un = uo + (ud < 0 ? ep : -ep);
//                    }
//                }
//                uvData[v][i] = un;
//            }
//        }
    }
}
