package grondag.xm2.apiimpl.texture;

import grondag.xm2.api.texture.TextureGroup;
import grondag.xm2.api.texture.TextureLayout;
import grondag.xm2.api.texture.TextureRenderIntent;
import grondag.xm2.api.texture.TextureRotation;
import grondag.xm2.api.texture.TextureScale;
import grondag.xm2.api.texture.TextureSet;
import grondag.xm2.api.texture.TextureSetBuilder;
import net.minecraft.util.Identifier;

public class TextureSetBuilderImpl extends AbstractTextureSet implements TextureSetBuilder {
    @Override
    public TextureSetBuilder versionCount(int versionCount) {
        this.versionCount = versionCount;
        return this;
    }

    @Override
    public TextureSetBuilder scale(TextureScale scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public TextureSetBuilder layout(TextureLayout layout) {
        this.layout = layout;
        return this;
    }

    @Override
    public TextureSetBuilder rotation(TextureRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public TextureSetBuilder renderIntent(TextureRenderIntent renderIntent) {
        this.renderIntent = renderIntent;
        return this;
    }

    @Override
    public TextureSetBuilder groups(TextureGroup... groups) {
        this.textureGroupFlags = TextureGroup.makeTextureGroupFlags(groups);
        return this;
    }

    @Override
    public TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile) {
        this.renderNoBorderAsTile = renderNoBorderAsTile;
        return this;
    }

    @Override
    public TextureSetBuilder baseTextureName(String baseTextureName) {
        this.rawBaseTextureName = baseTextureName;
        return this;
    }

    @Override
    public TextureSetBuilder displayNameToken(String displayNameToken) {
        this.displayNameToken = displayNameToken;
        return this;
    }

    @Override
    public TextureSet build(Identifier id) {
        TextureSetImpl result = new TextureSetImpl(id, this);
        TextureSetRegistryImpl.INSTANCE.add(result);
        return result;
    }
}
