package grondag.brocade.apiimpl.texture;

import java.util.HashMap;

import grondag.brocade.api.texture.TextureGroup;
import grondag.brocade.api.texture.TextureLayout;
import grondag.brocade.api.texture.TextureRenderIntent;
import grondag.brocade.api.texture.TextureRotation;
import grondag.brocade.api.texture.TextureScale;
import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.api.texture.TextureSetRegistry;
import grondag.fermion.structures.NullHandler;
import net.minecraft.util.Identifier;

public class TextureSetRegistryImpl implements TextureSetRegistry {
    public static final TextureSetRegistryImpl INSTANCE = new TextureSetRegistryImpl();
    public static final TextureSetImpl DEFAULT_TEXTURE_SET;
    
    private final TextureSetImpl[] array = new TextureSetImpl[MAX_TEXTURE_SETS];
    private final HashMap<Identifier, TextureSetImpl> map = new HashMap<>();
    private int nextIndex = 0;
    
    synchronized void add(TextureSetImpl newSet) {
        if(array[newSet.index] == null && !map.containsKey(newSet.id)) {
            array[newSet.index] = newSet;
            map.put(newSet.id, newSet);
        };
    }
    
    synchronized int claimIndex() {
        return nextIndex++;
    }
    
    @Override
    public TextureSetImpl getById(Identifier id) {
        return NullHandler.defaultIfNull(map.get(id), DEFAULT_TEXTURE_SET);
    }

    @Override
    public TextureSetImpl getByIndex(int index) {
        return index < 0 || index >= nextIndex ? DEFAULT_TEXTURE_SET : array[index];
    }

    @Override
    public int size() {
        return nextIndex;
    }
    
    static {
        DEFAULT_TEXTURE_SET = (TextureSetImpl) TextureSet.builder().displayNameToken("none").baseTextureName("noise_moderate").versionCount(4).scale(TextureScale.SINGLE)
                .layout(TextureLayout.SPLIT_X_8).rotation(TextureRotation.ROTATE_RANDOM).renderIntent(TextureRenderIntent.BASE_ONLY)
                .groups(TextureGroup.ALWAYS_HIDDEN).build(TextureSetRegistry.NONE);
    }
}
