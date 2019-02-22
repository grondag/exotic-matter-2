package grondag.brocade.model.texture;

import java.util.function.Consumer;

import grondag.brocade.api.texture.TextureLayout;
import grondag.brocade.api.texture.TextureRenderIntent;
import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureScale;
import grondag.brocade.api.texture.TextureRotation.TextureRotationSetting;
import grondag.brocade.model.state.ModelStateData;
import grondag.fermion.IGrondagMod;
import net.minecraft.client.resource.language.I18n;

class TexturePallette implements ITexturePalette {
    public final IGrondagMod mod;

    public final String systemName;

    public final String textureBaseName;

    /** number of texture versions must be a power of 2 */
    public final int textureVersionCount;

    public final TextureScale textureScale;
    public final TextureLayout textureLayout;

    /**
     * Used to display appropriate label for texture. 0 = no zoom, 1 = 2x zoom, 2 =
     * 4x zoom
     */
    public final int zoomLevel;

    /**
     * Masks the version number provided by consumers - alternators that drive
     * number generation may support larger number of values. Implies number of
     * texture versions must be a power of 2
     */
    public final int textureVersionMask;

    /**
     * Governs default rendering rotation for texture and what rotations are
     * allowed.
     */
    public final TextureRotationSetting rotation;

    /**
     * Determines layer that should be used for rendering this texture.
     */
    public final TextureRenderIntent renderIntent;

    /**
     * Globally unique id
     */
    public final int ordinal;

    /**
     * Used by modelstate to know which world state must be retrieved to drive this
     * texture (rotation and block version)
     */
    public final int stateFlags;

    public final int textureGroupFlags;

    /**
     * Number of ticks each frame should be rendered on the screen before
     * progressing to the next frame.
     */
    public final int ticksPerFrame;

    /** for border-layout textures, controls if "no border" texture is rendered */
    public final boolean renderNoBorderAsTile;

    protected TexturePallette(String systemName, String textureBaseName, TexturePaletteSpec info) {
        this.mod = info.mod;
        this.ordinal = TexturePaletteRegistry.nextOrdinal++;
        this.systemName = systemName;
        this.textureBaseName = textureBaseName;
        this.textureVersionCount = info.textureVersionCount;
        this.textureVersionMask = Math.max(0, info.textureVersionCount - 1);
        this.textureScale = info.textureScale;
        this.textureLayout = info.layout;
        this.rotation = info.rotation;
        this.renderIntent = info.renderIntent;
        this.textureGroupFlags = info.textureGroupFlags;
        this.zoomLevel = info.zoomLevel;
        this.ticksPerFrame = info.ticksPerFrame;
        this.renderNoBorderAsTile = info.renderNoBorderAsTile;

        int flags = this.textureScale.modelStateFlag | this.textureLayout.modelStateFlag;

        // textures with randomization options also require position information

        if (info.rotation.rotationType() == TextureRotation.RANDOM) {
            flags |= (ModelStateData.STATE_FLAG_NEEDS_TEXTURE_ROTATION | ModelStateData.STATE_FLAG_NEEDS_POS);
        }

        if (info.textureVersionCount > 1) {
            flags |= ModelStateData.STATE_FLAG_NEEDS_POS;
        }
        this.stateFlags = flags;

    }

    @Override
    public void prestitch(Consumer<String> stitcher) {
        this.textureLayout.prestitch(this, stitcher);
    }

    @Override
    public String getSampleTextureName() {
        return this.textureLayout.sampleTextureName(this);
    }

    /**
     * See {@link #getSampleSprite()}
     */
    private EnhancedSprite sampleSprite;

    @Override
    public EnhancedSprite getSampleSprite() {
        EnhancedSprite result = sampleSprite;
        if (result == null) {
            result = this.textureLayout.createSampleSprite(this);
            sampleSprite = result;
        }
        return result;
    }

    @Override
    public String getTextureName(int version) {
        return this.textureLayout.buildTextureName(this, version & this.textureVersionMask, 0);
    }

    /**
     * @see grondag.hard_science.superblock.texture.ITexturePallette#textureName(int,
     *      int)
     */
    @Override
    public String getTextureName(int version, int index) {
        return this.textureLayout.buildTextureName(this, version & this.textureVersionMask, index);
    }

    @Override
    public final String displayName() {
        // trim off the .zoom suffixes to get the localization string
        final String token = "texture." + this.systemName.replaceAll(".zoom", "");
        final String texName = I18n.translate(token.toLowerCase());

        switch (this.zoomLevel) {
        case 1:
            return I18n.translate("texture.zoom2x_format", texName);
        case 2:
            return I18n.translate("texture.zoom4x_format", texName);
        default:
            return texName;
        }
    }

    @Override
    public final String textureBaseName() {
        return this.textureBaseName;
    }

    /** number of texture versions must be a power of 2 */
    @Override
    public final int textureVersionCount() {
        return this.textureVersionCount;
    }

    @Override
    public final TextureScale textureScale() {
        return this.textureScale;
    }

    @Override
    public final TextureLayout textureLayout() {
        return this.textureLayout;
    }

    /**
     * Used to display appropriate label for texture. 0 = no zoom, 1 = 2x zoom, 2 =
     * 4x zoom
     */
    @Override
    public final int zoomLevel() {
        return this.zoomLevel;
    }

    /**
     * Masks the version number provided by consumers - alternators that drive
     * number generation may support larger number of values. Implies number of
     * texture versions must be a power of 2
     */
    @Override
    public final int textureVersionMask() {
        return this.textureVersionMask;
    }

    /**
     * Governs default rendering rotation for texture and what rotations are
     * allowed.
     */
    @Override
    public final TextureRotationSetting rotation() {
        return this.rotation;
    }

    /**
     * Determines layer that should be used for rendering this texture.
     */
    @Override
    public final TextureRenderIntent renderIntent() {
        return this.renderIntent;
    }

    /**
     * Globally unique id
     */
    @Override
    public final int ordinal() {
        return this.ordinal;
    }

    /**
     * Used by modelstate to know which world state must be retrieved to drive this
     * texture (rotation and block version)
     */
    @Override
    public final int stateFlags() {
        return this.stateFlags;
    }

    @Override
    public final int textureGroupFlags() {
        return this.textureGroupFlags;
    }

    /**
     * Number of ticks each frame should be rendered on the screen before
     * progressing to the next frame.
     */
    @Override
    public final int ticksPerFrame() {
        return this.ticksPerFrame;
    }

    /** for border-layout textures, controls if "no border" texture is rendered */
    @Override
    public final boolean renderNoBorderAsTile() {
        return this.renderNoBorderAsTile;
    }

    @Override
    public final String systemName() {
        return this.systemName;
    }

    @Override
    public final IGrondagMod mod() {
        return this.mod;
    }

}