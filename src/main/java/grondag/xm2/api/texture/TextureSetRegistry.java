package grondag.xm2.api.texture;

import grondag.xm2.Xm;
import grondag.xm2.impl.texture.TextureSetRegistryImpl;
import net.minecraft.util.Identifier;

public interface TextureSetRegistry {
    public static TextureSetRegistry instance() {
        return TextureSetRegistryImpl.INSTANCE;
    }
    
    /**
     * Will always be associated with index 0.
     */
    public static final Identifier NONE_ID = new Identifier(Xm.MODID, "none");

    //TODO: make this larger after state refactor
    /**
     * Max number of texture palettes that can be registered, loaded and represented
     * in model state.
     */
    public static final int MAX_TEXTURE_SETS = 4096;
    
    TextureSet getById(Identifier id);
    
    TextureSet getByIndex(int index);
    
    int size();
}
