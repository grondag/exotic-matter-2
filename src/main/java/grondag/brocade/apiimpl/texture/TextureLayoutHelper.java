package grondag.brocade.apiimpl.texture;

import static grondag.brocade.api.texture.TextureLayout.*;

import java.util.function.Consumer;

import grondag.brocade.api.texture.TextureLayout;
import grondag.brocade.api.texture.TextureSet;
import net.minecraft.util.Identifier;

public abstract class TextureLayoutHelper {
    static final TextureLayoutHelper HELPERS[] = new TextureLayoutHelper[TextureLayout.values().length];
    
    static {
        HELPERS[SPLIT_X_8.ordinal()] = new TextureLayoutHelper() {
            @Override
            public final String buildTextureName(TextureSet texture, int version, int index) {
                return buildTextureName_X_8(texture, index);
            }
        };
        
        HELPERS[SIMPLE.ordinal()] = new TextureLayoutHelper() { };
        
        HELPERS[BORDER_13.ordinal()] = new TextureLayoutHelper() {
            @Override
            public final String buildTextureName(TextureSet texture, int version, int index) {
                return buildTextureName_X_8(texture, version * BORDER_13.blockTextureCount + index);
            }

            @Override
            public void prestitch(TextureSet texture, Consumer<Identifier> stitcher) {
                // last texture (no border) only needed if indicated
                final int texCount = texture.renderNoBorderAsTile() ? BORDER_13.textureCount : BORDER_13.textureCount - 1;

                for (int i = 0; i < texture.versionCount(); i++) {
                    for (int j = 0; j < texCount; j++) {
                        stitcher.accept(new Identifier(texture.id().getNamespace(), buildTextureName_X_8(texture, i * BORDER_13.blockTextureCount + j)));
                    }
                }
            }

            @Override
            public String sampleTextureName(TextureSet texture) {
                return this.buildTextureName(texture, 0, 4);
            }
        };
        
        HELPERS[MASONRY_5.ordinal()] = new TextureLayoutHelper() {
            @Override
            public final String buildTextureName(TextureSet texture, int version, int index) {
                return buildTextureName_X_8(texture, version * MASONRY_5.blockTextureCount + index);
            }

            @Override
            public void prestitch(TextureSet texture, Consumer<Identifier> stitcher) {
                for (int i = 0; i < texture.versionCount(); i++) {
                    for (int j = 0; j < MASONRY_5.textureCount; j++) {
                        stitcher.accept(new Identifier(texture.id().getNamespace(), buildTextureName_X_8(texture, i * MASONRY_5.blockTextureCount + j)));
                    }
                }
            }
        };
        
        HELPERS[BIGTEX_ANIMATED.ordinal()] = new TextureLayoutHelper() { };
        
        HELPERS[QUADRANT_CONNECTED.ordinal()] = new TextureLayoutHelper() { };
    }
    
    protected TextureLayoutHelper() {};
    
    public static String buildTextureName_X_8(TextureSet texture, int offset) {
        return texture.baseTextureName() + "_" + (offset >> 3) + "_" + (offset & 7);
    }
    
    /**
     * Default implementation just prepends the folder. Suitable for single-file
     * textures. If textures have multiple versions, names should have a zero-based
     * _x suffix
     */
    public String buildTextureName(TextureSet texture, int version, int index) {
        return texture.versionCount() == 1 ? texture.baseTextureName() : (texture.baseTextureName() + "_" + version);
    }
    
    public void prestitch(TextureSet texture, Consumer<Identifier> stitcher) {
        for (int i = 0; i < texture.versionCount(); i++) {
            stitcher.accept(new Identifier(texture.id().getNamespace(), this.buildTextureName(texture, i, 0)));
        }
    }

    public String sampleTextureName(TextureSet texture) {
        return this.buildTextureName(texture, 0, 0);
    }
}
