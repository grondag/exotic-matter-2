package grondag.brocade.apiimpl.texture;

import grondag.brocade.api.texture.TextureGroup;
import grondag.brocade.api.texture.TextureLayout;
import grondag.brocade.api.texture.TextureRenderIntent;
import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureScale;
import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.api.texture.TextureSetBuilder;
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
        this.baseTextureName = baseTextureName;
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
