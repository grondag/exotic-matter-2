package grondag.brocade.api;

import net.minecraft.util.Identifier;

public interface TextureSet {
    /** Registration ID */
    Identifier id();
    
    /** Transient id for temporary serialization. Client values may not match server values.*/
    int index();
    
    TextureLayout layout();
    
    TextureRotation rotation();
    
    TextureScale scale();
    
    TextureRenderIntent renderIntent();
    
    /** number of alternate versions available */
    int versionCount();
}
