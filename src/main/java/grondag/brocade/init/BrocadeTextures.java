package grondag.brocade.init;

import static grondag.brocade.api.texture.TextureGroup.HIDDEN_TILES;
import static grondag.brocade.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.brocade.api.texture.TextureGroup.STATIC_TILES;
import static grondag.brocade.api.texture.TextureLayout.BORDER_13;
import static grondag.brocade.api.texture.TextureLayout.SIMPLE;
import static grondag.brocade.api.texture.TextureLayout.SPLIT_X_8;
import static grondag.brocade.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.brocade.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.brocade.api.texture.TextureRotation.ROTATE_180;
import static grondag.brocade.api.texture.TextureRotation.ROTATE_270;
import static grondag.brocade.api.texture.TextureRotation.ROTATE_90;
import static grondag.brocade.api.texture.TextureRotation.ROTATE_NONE;
import static grondag.brocade.api.texture.TextureRotation.ROTATE_RANDOM;
import static grondag.brocade.api.texture.TextureScale.GIANT;
import static grondag.brocade.api.texture.TextureScale.LARGE;
import static grondag.brocade.api.texture.TextureScale.MEDIUM;
import static grondag.brocade.api.texture.TextureScale.SINGLE;
import static grondag.brocade.api.texture.TextureScale.SMALL;
import static grondag.brocade.api.texture.TextureScale.TINY;

import grondag.brocade.Brocade;
import grondag.brocade.api.texture.TextureRenderIntent;
import grondag.brocade.api.texture.TextureSet;
import grondag.brocade.apiimpl.texture.TextureSetImpl;
import grondag.brocade.apiimpl.texture.TextureSetRegistryImpl;
import grondag.fermion.config.FermionConfig;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class BrocadeTextures {
    
    /** 
     * Main purpose of being here is to force instantiation of other static members.
     */
    public static void init() {
        Brocade.LOG.debug("Registering Brocade textures");
        
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((atlas, registry) -> {
            if(atlas == MinecraftClient.getInstance().getSpriteAtlas()) {
                TextureSetRegistryImpl texReg = TextureSetRegistryImpl.INSTANCE;
                final int limit = texReg.size();
                for(int i = 0; i < limit; i++) {
                    TextureSetImpl set = texReg.getByIndex(i);
                    set.prestitch(id -> registry.register(id));
                }
            }
        });
    }
    
    // ======================================================================
    // TEST/DEBUG TEXTURES - NOT LOADED UNLESS NEEDED
    // ======================================================================

    // but still load placeholders so we don't lose test texture attributes on
    // blocks if test textures are temporarily disabled

    public static final TextureSet BIGTEX_TEST_SINGLE = TextureSet.builder().displayNameToken("bigtex_test_single")
            .baseTextureName(FermionConfig.BLOCKS.showHiddenTextures ? "blocks/bigtex_single" : "blocks/noise_moderate_0_0")
            .versionCount(1).scale(SMALL).layout(SIMPLE).rotation(ROTATE_NONE).renderIntent(BASE_ONLY)
            .groups(HIDDEN_TILES).build("brocade", "bigtex_test_single");

    public static final TextureSet BIGTEX_TEST1 = TextureSet.builder().displayNameToken("big_tex_test1")
            .baseTextureName(FermionConfig.BLOCKS.showHiddenTextures ? "blocks/bigtex" : "blocks/noise_moderate_0")
            .versionCount(4).scale(TINY).layout(SIMPLE).rotation(ROTATE_RANDOM).renderIntent(BASE_ONLY)
            .groups(HIDDEN_TILES).build("brocade", "big_tex_test1");

    public static final TextureSet BIGTEX_TEST2 = TextureSet.builder(BIGTEX_TEST1)
            .displayNameToken("big_tex_test2").scale(SMALL).build("brocade", "big_tex_test2");
    
    public static final TextureSet BIGTEX_TEST3 = TextureSet.builder(BIGTEX_TEST1)
            .displayNameToken("big_tex_test3").scale(MEDIUM).build("brocade", "big_tex_test3");
            
    public static final TextureSet BIGTEX_TEST4 = TextureSet.builder(BIGTEX_TEST1)
            .displayNameToken("big_tex_test4").scale(LARGE).build("brocade", "big_tex_test4");
    
    public static final TextureSet BIGTEX_TEST5 = TextureSet.builder(BIGTEX_TEST1)
            .displayNameToken("big_tex_test5").scale(GIANT).build("brocade", "big_tex_test5");

    public static final TextureSet TEST = TextureSet.builder().displayNameToken("test")
            .baseTextureName(FermionConfig.BLOCKS.showHiddenTextures ? "blocks/test" : "blocks/noise_moderate_0")
            .versionCount(2).scale(SINGLE).layout(SIMPLE).rotation(ROTATE_NONE)
            .renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("brocade","test");
    
    public static final TextureSet TEST_ZOOM = TextureSet.builder(TEST).displayNameToken("test_zoom")
            .scale(TINY).build("brocade", "test_zoom");

    public static final TextureSet TEST_90 = TextureSet.builder(TEST).displayNameToken("test_90")
            .rotation(ROTATE_90).build("brocade", "test_90");
    
    public static final TextureSet TEST_90_ZOOM = TextureSet.builder(TEST_90).displayNameToken("test_90_zoom")
            .scale(TINY).build("brocade", "test_90_zoom");

    public static final TextureSet TEST_180 = TextureSet.builder(TEST).displayNameToken("test_180")
            .rotation(ROTATE_90).build("brocade", "test_180");
    
    public static final TextureSet TEST_180_ZOOM = TextureSet.builder(TEST_180).displayNameToken("test_180_zoom")
            .scale(TINY).build("brocade", "test_180_zoom");
    
    public static final TextureSet TEST_270 = TextureSet.builder(TEST).displayNameToken("test_270")
            .rotation(ROTATE_90).build("brocade", "test_270");
    
    public static final TextureSet TEST_270_ZOOM = TextureSet.builder(TEST_270).displayNameToken("test_270_zoom")
            .scale(TINY).build("brocade", "test_270_zoom");

    public static final TextureSet TEST_4X4 = TextureSet.builder().displayNameToken("test4x4")
            .baseTextureName(FermionConfig.BLOCKS.showHiddenTextures ? "blocks/test4x4" : "blocks/noise_moderate_0_0")
            .versionCount(1).scale(SMALL).layout(SIMPLE).rotation(ROTATE_NONE).renderIntent(BASE_ONLY)
            .groups(HIDDEN_TILES).build("brocade", "test4x4");

    public static final TextureSet TEST_4x4_90 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_90")
            .rotation(ROTATE_90).build("brocade", "test4x4_90");
    
    public static final TextureSet TEST_4x4_180 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_180")
            .rotation(ROTATE_180).build("brocade", "test4x4_180");
    
    public static final TextureSet TEST_4x4_270 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_270")
            .rotation(ROTATE_270).build("brocade", "test4x4_270");
    
    // ======================================================================
    // TILES - REGULAR
    //
    // Textures of general interest for any mod
    // ======================================================================

    public static final TextureSet BLOCK_COBBLE = TextureSet.builder().displayNameToken("cobble")
            .baseTextureName("blocks/cobble").versionCount(4).scale(SINGLE).layout(SPLIT_X_8).rotation(ROTATE_RANDOM)
            .renderIntent(BASE_ONLY).groups(STATIC_TILES).build("brocade", "cobble");

    public static final TextureSet BLOCK_COBBLE_ZOOM = TextureSet.builder(BLOCK_COBBLE).displayNameToken("cobble_zoom")
            .scale(TINY).build("brocade", "cobble_zoom");

    public static final TextureSet BLOCK_NOISE_STRONG = TextureSet.builder(BLOCK_COBBLE).displayNameToken("noise_strong")
            .baseTextureName("blocks/noise_strong").build("brocade", "noise_strong");
    
    public static final TextureSet BLOCK_NOISE_STRONG_ZOOM = TextureSet.builder(BLOCK_NOISE_STRONG).displayNameToken("noise_strong_zoom")
            .scale(TINY).build("brocade", "noise_strong_zoom");

    public static final TextureSet BLOCK_NOISE_MODERATE = TextureSet.builder(BLOCK_COBBLE).displayNameToken("noise_moderate")
            .baseTextureName("blocks/noise_moderate").build("brocade", "noise_moderate");
    
    public static final TextureSet BLOCK_NOISE_MODERATE_ZOOM = TextureSet.builder(BLOCK_NOISE_STRONG).displayNameToken("noise_moderate_zoom")
            .scale(TINY).build("brocade", "noise_moderate_zoom");
    
    public static final TextureSet BLOCK_NOISE_SUBTLE = TextureSet.builder(BLOCK_COBBLE).displayNameToken("noise_subtle")
            .baseTextureName("blocks/noise_subtle").build("brocade", "noise_subtle");
    
    public static final TextureSet BLOCK_NOISE_SUBTLE_ZOOM = TextureSet.builder(BLOCK_NOISE_STRONG).displayNameToken("noise_subtle_zoom")
            .scale(TINY).build("brocade", "noise_subtle_zoom");

    public static final TextureSet WHITE = TextureSet.builder().displayNameToken("white")
            .baseTextureName("blocks/white").versionCount(1).scale(SINGLE).layout(SPLIT_X_8)
            .rotation(ROTATE_NONE).groups(STATIC_TILES).build("brocade", "white");

    public static final TextureSet BORDER_SMOOTH_BLEND = TextureSet.builder().displayNameToken("border_smooth_blended")
            .baseTextureName("blocks/border_smooth_blended").versionCount(1).scale(SINGLE).layout(BORDER_13)
            .rotation(ROTATE_NONE).renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("brocade", "border_smooth_blended");
    
    public static final TextureSet BORDER_CAUTION = TextureSet.builder().displayNameToken("border_caution")
            .baseTextureName("blocks/border_caution").versionCount(1).scale(SINGLE).layout(BORDER_13)
            .rotation(ROTATE_NONE).renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("brocade", "border_caution");
    
    
    public static final TextureSet SANDSTONE = TextureSet.builder().displayNameToken("sandstone")
            .baseTextureName("blocks/sandstone")
            .versionCount(1).scale(MEDIUM).layout(SIMPLE).rotation(ROTATE_RANDOM).renderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .groups(STATIC_TILES).build("brocade", "sandstone");

    public static final TextureSet SANDSTONE_ZOOM = TextureSet.builder(SANDSTONE)
            .displayNameToken("sandstone_zoom").scale(LARGE).build("brocade", "sandstone_zoom");
    
    public static final TextureSet SANDSTONE_ZOOM2 = TextureSet.builder(SANDSTONE)
            .displayNameToken("sandstone_zoom2").scale(GIANT).build("brocade", "sandstone_zoom2");
}
