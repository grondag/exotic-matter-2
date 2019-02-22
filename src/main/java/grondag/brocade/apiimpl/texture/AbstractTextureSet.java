package grondag.brocade.apiimpl.texture;

import grondag.brocade.api.texture.TextureLayout;
import grondag.brocade.api.texture.TextureRenderIntent;
import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureScale;

abstract class AbstractTextureSet {
    TextureLayout layout;
    TextureRotation rotation;
    TextureScale scale;
    TextureRenderIntent renderIntent;
    int versionCount;
    String baseTextureName;
    boolean renderNoBorderAsTile;
    String displayName;
    int zoomLevel;
    int textureGroupFlags;
    String sampleTextureName;
    
    protected void copyFrom(AbstractTextureSet template) {
        this.layout = template.layout;
        this.rotation = template.rotation;
        this.scale = template.scale;
        this.renderIntent = template.renderIntent;
        this.versionCount = template.versionCount;
        this.baseTextureName = template.baseTextureName;
        this.renderNoBorderAsTile = template.renderNoBorderAsTile;
        this.displayName = template.displayName;
        this.zoomLevel = template.zoomLevel;
        this.textureGroupFlags = template.textureGroupFlags;
        this.sampleTextureName = template.sampleTextureName;
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
    
    public String baseTextureName() {
        return baseTextureName;
    }

    public boolean renderNoBorderAsTile() {
        return renderNoBorderAsTile;
    }

    public String displayName() {
        return displayName;
    }

    public int zoomLevel() {
        return zoomLevel;
    }
    
    public String sampleTextureName() {
        return sampleTextureName;
    }
    
    public int textureGroupFlags() {
        return textureGroupFlags;
    }
}
