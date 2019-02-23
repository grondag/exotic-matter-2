package grondag.brocade.api.texture;

import grondag.brocade.apiimpl.texture.TextureSetRegistryImpl;
import net.minecraft.util.Identifier;

public interface TextureSetRegistry {
    public static TextureSetRegistry instance() {
        return TextureSetRegistryImpl.INSTANCE;
    }
    
    /**
     * Will always be associated with index 0.
     */
    public static final Identifier NONE = new Identifier("brocade", "none");
    
    /**
     * Max number of texture palettes that can be registered, loaded and represented
     * in model state.
     */
    public static final int MAX_TEXTURE_SETS = 4096;
    
    TextureSet getById(Identifier id);
    
    TextureSet getByIndex(int index);
    
    int size();
}
