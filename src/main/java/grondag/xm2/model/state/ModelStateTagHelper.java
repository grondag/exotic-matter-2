package grondag.xm2.model.state;

import grondag.fermion.serialization.NBTDictionary;
import net.minecraft.nbt.CompoundTag;

public abstract class ModelStateTagHelper {
    private ModelStateTagHelper() {}

    static final String NBT_MODEL_BITS = NBTDictionary.claim("modelState");

    static final String NBT_SHAPE = NBTDictionary.claim("shape");
    
    /**
     * Stores string containing registry names of textures, vertex processors
     */
    static final String NBT_LAYERS = NBTDictionary.claim("layers");

    /**
     * Removes model state from the tag if present.
     */
    public static final void clearNBTValues(CompoundTag tag) {
        if (tag == null)
            return;
        tag.remove(NBT_MODEL_BITS);
        tag.remove(NBT_SHAPE);
        tag.remove(NBT_LAYERS);
    }
}
