package grondag.brocade.apiimpl.texture;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.api.texture.TextureSetBuilder;
import grondag.brocade.model.state.ModelStateData;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public class TextureSetImpl extends AbstractTextureSet implements TextureSet {
    private static final AtomicInteger nextIndex = new AtomicInteger();
    
    public static TextureSetBuilder builder() {
        return new TextureSetBuilderImpl();
    }
    
    public final int index;
    public final Identifier id;
    public final int versionMask;
    public final int stateFlags;
    
    TextureSetImpl(Identifier id, AbstractTextureSet template) {
        this.id = id;
        this.index = nextIndex.getAndIncrement();
        copyFrom(template);
        this.versionMask = Math.max(0, template.versionCount - 1);
        
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
    public void prestitch(Consumer<String> stitcher) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Sprite sampleSprite() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String textureName(int version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String textureName(int version, int index) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int versionMask() {
        return versionMask;
    }
}
