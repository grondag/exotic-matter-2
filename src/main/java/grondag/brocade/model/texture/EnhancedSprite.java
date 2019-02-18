package grondag.brocade.model.texture;

import grondag.exotic_matter.model.render.QuadBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Adds a few extra for convenience / performance.
 *
 */
public class EnhancedSprite extends TextureAtlasSprite
{
    /**
     * Same as minU but with addition of safety margin to prevent bleeding.
     */
    protected float safeMinU;
    
    /**
     * Same as maxU but with addition of safety margin to prevent bleeding.
     */
    protected float safeMaxU;

    /**
     * Same as minV but with addition of safety margin to prevent bleeding.
     */
    protected float safeMinV;
    
    /**
     * Same as maxV but with addition of safety margin to prevent bleeding.
     */
    protected float safeMaxV;

    /**
     * Width of texture adjusted for safety margin, in coordinates of atlas.
     */
    protected float safeSpanU;
    
    /**
     * Height of texture adjusted for safety margin, in coordinates of atlas.
     */
    protected float safeSpanV;
    
    /**
     * Width of texture in coordinates of atlas.
     */
    protected float spanU;
    
    /**
     * Height of texture in coordinates of atlas.
     */
    protected float spanV;
    
    public EnhancedSprite(String spriteName)
    {
        super(spriteName);
    }

    @Override
    public void initSprite(int inX, int inY, int originInX, int originInY, boolean rotatedIn)
    {
        super.initSprite(inX, inY, originInX, originInY, rotatedIn);
        this.spanU = this.getMaxU() - this.getMinU();
        this.spanV = this.getMaxV() - this.getMinV();

        this.safeMinU = this.getMinU() + this.spanU * QuadBakery.UV_EPS;
        this.safeMaxU = this.getMaxU() - this.spanU * QuadBakery.UV_EPS;
        this.safeMinV = this.getMinV() + this.spanV * QuadBakery.UV_EPS;
        this.safeMaxV = this.getMaxV() - this.spanV * QuadBakery.UV_EPS;
        
        this.safeSpanU = this.safeMaxU - this.safeMinU;
        this.safeSpanV = this.safeMaxV - this.safeMinV;
    }
    
    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     */
    public float unitInterpolatedU(float u)
    {
        return this.getMinU() + this.spanU * u;
    }
    
    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     */
    public float unitInterpolatedU(double u)
    {
        return this.unitInterpolatedU((float)u);
    }
    
    /**
     * Gets a V coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     */
    public float unitInterpolatedV(float v)
    {
        return this.getMinV() + this.spanV * v;
    }
    
    /**
     * Gets a V coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     */
    public float unitInterpolatedV(double v)
    {
        return this.unitInterpolatedV((float)v);
    }
    
    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     * Leaves a small margin around edge of texture to prevent bleeding.
     */
    public float safeInterpolatedU(float u)
    {
        return this.safeMinU + this.safeSpanU * u;
    }
    
    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     * Leaves a small margin around edge of texture to prevent bleeding.
     */
    public float safeInterpolatedU(double u)
    {
        return this.safeInterpolatedU((float)u);
    }
    
    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     * Leaves a small margin around edge of texture to prevent bleeding.
     */
    public float safeInterpolatedV(float v)
    {
        return this.safeMinV + this.safeSpanV * v;
    }
    
    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 1 returns uMax. Other arguments return in-between values.
     * Leaves a small margin around edge of texture to prevent bleeding.
     */
    public float safeInterpolatedV(double v)
    {
        return this.safeInterpolatedV((float)v);
    }
    
    /**
     * Same as minU but with addition of safety margin to prevent bleeding.
     */
    public float safeMinU() { return this.safeMinU; }
    
    /**
     * Same as maxU but with addition of safety margin to prevent bleeding.
     */
    public float safeMaxU() { return this.safeMaxU; }

    /**
     * Same as minV but with addition of safety margin to prevent bleeding.
     */
    public float safeMinV() { return this. safeMinV; }
    
    /**
     * Same as maxV but with addition of safety margin to prevent bleeding.
     */
    public float safeMaxV() { return this.safeMaxV; }

    /**
     * Width of texture adjusted for safety margin, in coordinates of atlas.
     */
    public float safeSpanU() { return this.safeSpanU; }
    
    /**
     * Height of texture adjusted for safety margin, in coordinates of atlas.
     */
    public float safeSpanV() { return this.safeSpanV; }
    
    /**
     * Width of texture in coordinates of atlas.
     */
    public float spanU() { return this.spanU; }
    
    /**
     * Height of texture in coordinates of atlas.
     */
    public float spanV() { return this.spanV; }
}
