package grondag.brocade.model.texture;

import grondag.brocade.model.texture.TextureRotationType.TextureRotationSetting;
import grondag.fermion.IGrondagMod;
import grondag.fermion.world.Rotation;

/**
 * Container holding mutable values for the various parameters that go
 * into constructing a texture palette.
 *
 */
public class TexturePaletteSpec
{
    public IGrondagMod mod;
    public int textureVersionCount = 1;
    public TextureScale textureScale = TextureScale.SINGLE; 
    public TextureLayout layout = TextureLayout.SIMPLE; 
    public TextureRotationSetting rotation = TextureRotationType.CONSISTENT.with(Rotation.ROTATE_NONE);
    public TextureRenderIntent renderIntent = TextureRenderIntent.BASE_ONLY; 
    public int textureGroupFlags = TextureGroup.ALWAYS_HIDDEN.bitFlag;
    public int zoomLevel = 0;
    /** number of ticks to display each frame */
    public int ticksPerFrame = 2;
    /** for border-layout textures, controls if "no border" texture is rendered */
    public boolean renderNoBorderAsTile = false;
    
    public TexturePaletteSpec(IGrondagMod mod)
    {
        this.mod = mod;
    }
    
    public TexturePaletteSpec(ITexturePalette source)
    {
        this.mod = source.mod();
        this.textureVersionCount = source.textureVersionCount();
        this.textureScale = source.textureScale();
        this.layout = source.textureLayout();
        this.rotation = source.rotation();
        this.renderIntent = source.renderIntent();
        this.textureGroupFlags = source.textureGroupFlags();
        this.zoomLevel = source.zoomLevel();
        this.ticksPerFrame = source.ticksPerFrame();
        this.renderNoBorderAsTile = source.renderNoBorderAsTile();
    }

    /**
     * @see TexturePallette#textureVersionCount
     */
    public TexturePaletteSpec withVersionCount(int textureVersionCount)
    {
        this.textureVersionCount = textureVersionCount;
        return this;
    }
    
    /**
     * @see TexturePallette#textureScale
     */
    public TexturePaletteSpec withScale(TextureScale textureScale)
    {
        this.textureScale = textureScale;
        return this;
    }
    
    /**
     * @see TexturePallette#layout
     */
    public TexturePaletteSpec withLayout(TextureLayout layout)
    {
        this.layout = layout;
        return this;
    }
    
    /**
     * @see TexturePallette#rotation
     */
    public TexturePaletteSpec withRotation(TextureRotationSetting rotation)
    {
        this.rotation = rotation;
        return this;
    }
    
    /**
     * @see TexturePallette#renderIntent
     */
    public TexturePaletteSpec withRenderIntent(TextureRenderIntent renderIntent)
    {
        this.renderIntent = renderIntent;
        return this;
    }
    
    /**
     * @see TexturePallette#textureGroupFlags
     */
    public TexturePaletteSpec withGroups(TextureGroup... groups)
    {
        this.textureGroupFlags = TextureGroup.makeTextureGroupFlags(groups);
        return this;
    }
    
    /**
     * @see TexturePallette#zoomLevel
     */
    public TexturePaletteSpec withZoomLevel(int zoomLevel)
    {
        this.zoomLevel = zoomLevel;
        return this;
    }
    
    /**
     * @see TexturePallette#ticksPerFrame
     */
    public TexturePaletteSpec withTicksPerFrame(int ticksPerFrame)
    {
        this.ticksPerFrame = ticksPerFrame;
        return this;
    }
    
    public TexturePaletteSpec withRenderNoBorderAsTile(boolean renderAsTile)
    {
        this.renderNoBorderAsTile = renderAsTile;
        return this;
    }
}