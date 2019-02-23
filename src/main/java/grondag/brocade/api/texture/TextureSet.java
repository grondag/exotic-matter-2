package grondag.brocade.api.texture;

import java.util.function.Consumer;

import grondag.brocade.apiimpl.texture.TextureSetImpl;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public interface TextureSet {
    public static TextureSetBuilder builder() {
        return TextureSetImpl.builder();
    }
    
    public static TextureSetBuilder builder(TextureSet template) {
        return TextureSetImpl.builder(template);
    }
    
    /** Registration ID */
    Identifier id();
    
    /** Transient id for temporary serialization. Client values may not match server values.*/
    int index();
    
    /**
     * Passes strings to consumer for all textures to be included in texture stitch.
     */
    void prestitch(Consumer<String> stitcher);
    
    /**
     * For use by TESR and GUI to conveniently and quickly access default sprite
     */
    Sprite sampleSprite();
    
    /**
     * Returns the actual texture name for purpose of finding a texture sprite. For
     * palettes with a single texture per version.
     */
    String textureName(int version);
    
    /**
     * Returns the actual texture name for purpose of finding a texture sprite. For
     * palettes with multiple textures per version.
     */
    String textureName(int version, int index);
    
    /**
     * Masks the version number provided by consumers - alternators that drive
     * number generation may support larger number of values. Implies number of
     * texture versions must be a power of 2
     */
    public int versionMask();
    
    TextureLayout layout();
    
    TextureRotation rotation();
    
    TextureScale scale();
    
    /**
     * Determines layer that should be used for rendering this texture.
     */
    TextureRenderIntent renderIntent();
    
    /** number of alternate versions available - must be a power of 2 */
    int versionCount();
    
    /**
     * Base texture file name - used to construct other text name methods. Exposed
     * to enable programmatic construction of semantically different palates that
     * use the same underlying texture file(s).
     */
    String baseTextureName();
    
    /** for border-layout textures, controls if "no border" texture is rendered */
    boolean renderNoBorderAsTile();
    
    /**
     * Player-friendly, localized name for this texture palette
     */
    String displayName();
    
    /**
     * Use {@link #sampleSprite()} when possible, not all texture formats work
     * well without specific UV mapping.
     */
    String sampleTextureName();
    
    /**
     * Used by modelstate to know which world state must be retrieved to drive this
     * texture (rotation and block version)
     */
    public int stateFlags();

    public int textureGroupFlags();
}
