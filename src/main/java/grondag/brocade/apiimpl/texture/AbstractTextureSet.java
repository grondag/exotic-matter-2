package grondag.brocade.apiimpl.texture;

import grondag.brocade.api.texture.TextureGroup;
import grondag.brocade.api.texture.TextureLayout;
import grondag.brocade.api.texture.TextureRenderIntent;
import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureScale;

abstract class AbstractTextureSet {
    TextureLayout layout = TextureLayout.SIMPLE;
    TextureRotation rotation = TextureRotation.ROTATE_NONE;
    TextureScale scale = TextureScale.SINGLE;
    TextureRenderIntent renderIntent = TextureRenderIntent.BASE_ONLY;
    int textureGroupFlags = TextureGroup.ALWAYS_HIDDEN.bitFlag;
    int versionCount = 1;
    boolean renderNoBorderAsTile = false;
    String rawBaseTextureName;
    String displayNameToken;
    
    protected void copyFrom(AbstractTextureSet template) {
        this.layout = template.layout;
        this.rotation = template.rotation;
        this.scale = template.scale;
        this.renderIntent = template.renderIntent;
        this.versionCount = template.versionCount;
        this.rawBaseTextureName = template.rawBaseTextureName;
        this.renderNoBorderAsTile = template.renderNoBorderAsTile;
        this.displayNameToken = template.displayNameToken;
        this.textureGroupFlags = template.textureGroupFlags;
    }
    
    public TextureLayout layout() {
        return layout;
    }

    public TextureRotation rotation() {
        return rotation;
    }

    public TextureScale scale() {
        return scale;
    }

    public TextureRenderIntent renderIntent() {
        return renderIntent;
    }

    public int versionCount() {
        return versionCount;
    }
    
    public boolean renderNoBorderAsTile() {
        return renderNoBorderAsTile;
    }

    public String displayName() {
        return displayNameToken;
    }

    public int textureGroupFlags() {
        return textureGroupFlags;
    }
}
