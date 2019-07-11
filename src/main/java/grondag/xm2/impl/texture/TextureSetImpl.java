package grondag.xm2.impl.texture;

import java.util.function.Consumer;

import grondag.xm2.api.texture.TextureRotation;
import grondag.xm2.api.texture.TextureSet;
import grondag.xm2.api.texture.TextureSetBuilder;
import grondag.xm2.state.ModelStateData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public class TextureSetImpl extends AbstractTextureSet implements TextureSet {
    public static TextureSetBuilder builder() {
        return new TextureSetBuilderImpl();
    }
    
    public static TextureSetBuilder builder(TextureSet template) {
        TextureSetBuilderImpl result = new TextureSetBuilderImpl();
        result.copyFrom((AbstractTextureSet) template);
        return result;
    }
    
    public final int index;
    public final Identifier id;
    public final int versionMask;
    public final int stateFlags;
    public final String baseTextureName;
    private final TextureLayoutHelper layoutHelper;
    
    TextureSetImpl(Identifier id, AbstractTextureSet template) {
        this.id = id;
        this.baseTextureName = id.getNamespace() + ":" + template.rawBaseTextureName;
        this.index = TextureSetRegistryImpl.INSTANCE.claimIndex();
        copyFrom(template);
        this.versionMask = Math.max(0, template.versionCount - 1);
        this.layoutHelper = TextureLayoutHelper.HELPERS[layout.ordinal()];
        
        int flags = template.scale.modelStateFlag | template.layout.modelStateFlag;

        // textures with randomization options also require position information

        if (template.rotation == TextureRotation.ROTATE_RANDOM) {
            flags |= (ModelStateData.STATE_FLAG_NEEDS_TEXTURE_ROTATION | ModelStateData.STATE_FLAG_NEEDS_POS);
        }

        if (template.versionCount > 1) {
            flags |= ModelStateData.STATE_FLAG_NEEDS_POS;
        }
        
        this.stateFlags = flags;
    }
    
    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int stateFlags() {
        return stateFlags;
    }
    
    @Override
    public void prestitch(Consumer<Identifier> stitcher) {
        layoutHelper.prestitch(this, stitcher);
    }

    @Override
    public String sampleTextureName() {
        return layoutHelper.sampleTextureName(this);
    }
    
    private Sprite sampleSprite;
    
    @Override
    public Sprite sampleSprite() {
        Sprite result = sampleSprite;
        if (result == null) {
            result = MinecraftClient.getInstance().getSpriteAtlas().getSprite(sampleTextureName());
            sampleSprite = result;
        }
        return result;
    }

    @Override
    public String textureName(int version) {
        return layoutHelper.buildTextureName(this, version & versionMask, 0);
    }

    @Override
    public String textureName(int version, int index) {
        return layoutHelper.buildTextureName(this, version & versionMask, index);
    }
    
    @Override
    public final String displayName() {
        return I18n.translate(displayNameToken);
    }
    
    @Override
    public int versionMask() {
        return versionMask;
    }
    
    @Override
    public String baseTextureName() {
        return baseTextureName;
    }
}
