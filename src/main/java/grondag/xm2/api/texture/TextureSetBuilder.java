package grondag.xm2.api.texture;

import net.minecraft.util.Identifier;

public interface TextureSetBuilder {
    TextureSetBuilder versionCount(int versionCount);

    TextureSetBuilder scale(TextureScale scale);

    TextureSetBuilder layout(TextureLayout layout);

    TextureSetBuilder rotation(TextureRotation rotation);

    TextureSetBuilder renderIntent(TextureRenderIntent renderIntent);

    TextureSetBuilder groups(TextureGroup... groups);

    TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile);
    
    TextureSetBuilder baseTextureName(String baseTextureName);
    
    TextureSetBuilder displayNameToken(String displayNameToken);
    
    TextureSet build(Identifier id);
    
    default TextureSet build(String nameSpace, String path) {
        return build(new Identifier(nameSpace, path));
    }
}
