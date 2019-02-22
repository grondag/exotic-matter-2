package grondag.brocade.api.texture;

import net.minecraft.util.Identifier;

public interface TextureSetBuilder {
    TextureSetBuilder versionCount(int versionCount);

    TextureSetBuilder scale(TextureScale scale);

    TextureSetBuilder layout(TextureLayout layout);

    TextureSetBuilder rotation(TextureRotation rotation);

    TextureSetBuilder renderIntent(TextureRenderIntent renderIntent);

    TextureSetBuilder groups(TextureGroup... groups);

    TextureSetBuilder zoomLevel(int zoomLevel);

    TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile);
    
    TextureSetBuilder baseTextureName(String baseTextureName);
    
    TextureSetBuilder sampleTextureName(String sampleTextureName);
    
    TextureSetBuilder displayName(String displayName);
    
    TextureSet build(Identifier id);
}
