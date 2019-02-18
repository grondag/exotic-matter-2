package grondag.brocade.model.render;

import java.lang.reflect.Field;

import javax.annotation.Nullable;

import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.varia.ReflectionHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.QuadGatheringTransformer;

public class LitBakedQuad extends BakedQuad
{
    private static @Nullable final Field parentField;
    static
    {
        Field pf = null;
        try
        {
            pf = ReflectionHelper.getAccessibleField(QuadGatheringTransformer.class, "parent");
        }
        catch (NoSuchFieldException e)
        {
            ExoticMatter.INSTANCE.error("Unable to wrap forge lighter, rendering will be borked", e);
        }
        parentField = pf;
    }
    
    private static final ThreadLocal<Wrapper> wrappers = new ThreadLocal<Wrapper>()
    {
        @Override
        protected Wrapper initialValue()
        {
            return new Wrapper();
        }
    };
    
    @SuppressWarnings("null")
    private static class Wrapper
    {
        private LitBakedQuad quad;
        private int ligherVertex = 0;
        private int bufferVertex = 0;
        
        protected final ForwardingVertexConsumer buffer = new ForwardingVertexConsumer()
        {
            @Override
            public void put(final int element, float... data)
            {
                VertexFormatElement e = wrapped.getVertexFormat().getElement(element);
                
                if(e.getUsage() == EnumUsage.COLOR)
                {
                    quad.interpolateVertexColors(ligherVertex++, data);
                }                    
                wrapped.put(element, data);
            }
        };
        
        protected final ForwardingVertexConsumer lighter = new ForwardingVertexConsumer()
        {
            @Override
            public void put(final int element, float... data)
            {
                VertexFormatElement e = wrapped.getVertexFormat().getElement(element);
                
                if(e.getUsage() == EnumUsage.NORMAL)
                {
                    wrapped.put(element, quad.normals[bufferVertex++]);
                }
                else
                    wrapped.put(element, data);
            }
        };
        
        public final void wrap(IVertexConsumer lighter, LitBakedQuad quad) throws IllegalArgumentException, IllegalAccessException
        {
            this.ligherVertex = 0;
            this.bufferVertex = 0;
            this.buffer.wrapped = (IVertexConsumer) parentField.get(lighter);
            this.lighter.wrapped = lighter;
            this.quad = quad;
            parentField.set(lighter, buffer);
        }
        
        public final void unwrap()
        {
            if(this.buffer.wrapped != null && this.lighter.wrapped != null)
            try
            {
                parentField.set(lighter.wrapped, buffer.wrapped);
            }
            catch(Exception e) {};
            this.buffer.wrapped = null;
            this.lighter.wrapped = null;
        }
    }
    
    private final int glowBits;
    private final float[][] normals;
    private int hash = 0;
    
    public LitBakedQuad(int[] vertexDataIn, float[][] normals, int tintIndexIn, @Nullable EnumFacing faceIn, TextureAtlasSprite spriteIn, boolean applyDiffuseLighting, VertexFormat format, int glowBits)
    {
        super(vertexDataIn, tintIndexIn, faceIn, spriteIn, applyDiffuseLighting, format);
        assert format == DefaultVertexFormats.BLOCK : "unsupported format for pre-lit quad";
        this.glowBits = glowBits;
        this.normals = normals;
    }

    private static boolean wrapError = false;
    
    @Override
    public void pipe(IVertexConsumer lighter)
    {
        if(parentField == null || wrapError)
        {
            super.pipe(lighter);
        }
        else
        {
            Wrapper w = wrappers.get();
            try
            {
                w.wrap(lighter, this);
                super.pipe(w.lighter);
            }
            catch (Exception e)
            {
                wrapError = true;
                ExoticMatter.INSTANCE.error("Unable to enable glow rendering due to unexpected error. Glow rendering will be disabled.", e);
            }
            finally
            {
                w.unwrap();
            }
        }
    }

    public void interpolateVertexColors(int vertexIndex, float[] colors)
    {
        final int glow = this.getVertexGlow(vertexIndex);
        if(glow == 0) return;
        
        if(glow == 15)
        {
            //The 1 here points to the color element of the block vertex format
            //This is brittle, but easy and fast, and right now that is only format supported for pre-lit quads.
            LightUtil.unpack(this.vertexData, colors, this.format, vertexIndex, 1);
            return;
        }
        
        final float wOriginal = glow / 15f;
        final float wShaded = 1 - wOriginal;
        final float r = colors[0] * wShaded;
        final float g = colors[1] * wShaded;
        final float b = colors[2] * wShaded;
        
        LightUtil.unpack(this.vertexData, colors, this.format, vertexIndex, 1);
        colors[0] = colors[0] * wOriginal + r;
        colors[1] = colors[1] * wOriginal + g;
        colors[2] = colors[2] * wOriginal + b;
    }
    
    /**
     * Returns value 0-15 representing how emmissive the vertex is.
     * 0 means normal lighting, normal shading for diffuse and AO.
     * 15 means full lighting, no shading or coloring for diffuse or AO
     */
    public int getVertexGlow(int vertexIndex)
    {
        return (this.glowBits >> (vertexIndex * 4)) & 0xF;
    }
    
    @Override
    public int hashCode()
    {
        int hash = this.hash;
        
        if(hash == 0)
        {
            hash = 1;
            for (int i = 0; i < this.vertexData.length; i++) {
                hash = 31 * hash + this.vertexData[i];
            }
            hash = 31 * hash + (this.format == null ? 0 : this.format.hashCode());
            hash = 31 * hash + (this.sprite == null ? 0 : this.sprite.hashCode());
    
            int otherStuff = this.applyDiffuseLighting ? 1 : 0;
            otherStuff |= this.face == null ? 0 : this.face.ordinal() << 1;
            otherStuff |= this.glowBits << 4;
            otherStuff |= this.tintIndex << 8;
    
            hash = 31 * hash + otherStuff;
            
            for(float[] n: this.normals)
            {
                hash = 31 * hash + Float.floatToRawIntBits(n[0]);
                hash = 31 * hash + Float.floatToRawIntBits(n[1]);
                hash = 31 * hash + Float.floatToRawIntBits(n[2]);
            }
            
            this.hash = hash;
        }
        return hash;
    }
    
    @Override
    public boolean equals(@Nullable Object other)
    {
        if(other instanceof LitBakedQuad)
        {
            LitBakedQuad otherQuad = (LitBakedQuad)other;
            
            if(!(this.format == otherQuad.format
                    &&  this.sprite == otherQuad.sprite
                    &&  this.face == otherQuad.face
                    &&  this.applyDiffuseLighting == otherQuad.applyDiffuseLighting
                    &&  this.tintIndex == otherQuad.tintIndex)) return false;
                    
            final int[] v1 = this.vertexData;
            final int[] v2 = otherQuad.vertexData;
            if(v1.length != v2.length) return false;
            
            for (int i = 0; i < v1.length; i++) 
            {
                if(v1[i] != v2[i]) return false;
            }
            
            final float[][] n1 = this.normals;
            final float[][] n2 = otherQuad.normals;
            if(n1.length != n2.length) return false;
            
            for (int i = 0; i < n1.length; i++) 
            {
                if(n1[i][0] != n2[i][0]) return false;
                if(n1[i][1] != n2[i][1]) return false;
                if(n1[i][2] != n2[i][2]) return false;
            }
            
            return true;
        }
        else
        {
            return false;
        }
    }
    
}
