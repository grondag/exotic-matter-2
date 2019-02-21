package grondag.brocade.init;

import static grondag.exotic_matter.model.texture.TextureRotationType.*;
import static grondag.exotic_matter.world.Rotation.*;

import grondag.exotic_matter.ConfigXM;
import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.model.texture.ITexturePalette;
import grondag.exotic_matter.model.texture.TextureGroup;
import grondag.exotic_matter.model.texture.TextureLayout;
import grondag.exotic_matter.model.texture.TexturePaletteRegistry;
import grondag.exotic_matter.model.texture.TexturePaletteSpec;
import grondag.exotic_matter.model.texture.TextureRenderIntent;
import grondag.exotic_matter.model.texture.TextureScale;

public class ModTextures {
    // ======================================================================
    // TEST/DEBUG TEXTURES - NOT LOADED UNLESS NEEDED
    // ======================================================================

    // but still load placeholders so we don't lose test texture attributes on
    // blocks if test textures are temporarily disabled

    public static final ITexturePalette BIGTEX_TEST_SINGLE = TexturePaletteRegistry.addTexturePallette(
            "bigtex_test_single", ConfigXM.BLOCKS.showHiddenTextures ? "bigtex_single" : "noise_moderate_0_0",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(1).withScale(TextureScale.SMALL)
                    .withLayout(TextureLayout.SIMPLE).withRotation(CONSISTENT.with(ROTATE_NONE))
                    .withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));

    public static final ITexturePalette BIGTEX_TEST1 = TexturePaletteRegistry.addTexturePallette("big_tex_test1",
            ConfigXM.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate_0",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(4).withScale(TextureScale.TINY)
                    .withLayout(TextureLayout.SIMPLE).withRotation(RANDOM.with(ROTATE_NONE))
                    .withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));

    public static final ITexturePalette BIGTEX_TEST2 = TexturePaletteRegistry.addTexturePallette("big_tex_test2",
            ConfigXM.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate_0",
            new TexturePaletteSpec(BIGTEX_TEST1).withScale(TextureScale.SMALL));
    public static final ITexturePalette BIGTEX_TEST3 = TexturePaletteRegistry.addTexturePallette("big_tex_test3",
            ConfigXM.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate_0",
            new TexturePaletteSpec(BIGTEX_TEST1).withScale(TextureScale.MEDIUM));
    public static final ITexturePalette BIGTEX_TEST4 = TexturePaletteRegistry.addTexturePallette("big_tex_test4",
            ConfigXM.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate_0",
            new TexturePaletteSpec(BIGTEX_TEST1).withScale(TextureScale.LARGE));
    public static final ITexturePalette BIGTEX_TEST5 = TexturePaletteRegistry.addTexturePallette("big_tex_test5",
            ConfigXM.BLOCKS.showHiddenTextures ? "bigtex" : "noise_moderate_0",
            new TexturePaletteSpec(BIGTEX_TEST1).withScale(TextureScale.GIANT));

    public static final ITexturePalette TEST = TexturePaletteRegistry.addTexturePallette("test",
            ConfigXM.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(2).withScale(TextureScale.SINGLE)
                    .withLayout(TextureLayout.SIMPLE).withRotation(RANDOM.with(ROTATE_NONE))
                    .withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));
    public static final ITexturePalette TEST_ZOOM = TexturePaletteRegistry.addZoomedPallete(TEST);

    public static final ITexturePalette TEST_90 = TexturePaletteRegistry.addTexturePallette("test_90",
            ConfigXM.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0",
            new TexturePaletteSpec(TEST).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette TEST_90_ZOOM = TexturePaletteRegistry.addZoomedPallete(TEST_90);

    public static final ITexturePalette TEST_180 = TexturePaletteRegistry.addTexturePallette("test_180",
            ConfigXM.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0",
            new TexturePaletteSpec(TEST).withRotation(FIXED.with(ROTATE_180)));
    public static final ITexturePalette TEST_180_ZOOM = TexturePaletteRegistry.addZoomedPallete(TEST_180);

    public static final ITexturePalette TEST_270 = TexturePaletteRegistry.addTexturePallette("test_270",
            ConfigXM.BLOCKS.showHiddenTextures ? "test" : "noise_moderate_0",
            new TexturePaletteSpec(TEST).withRotation(FIXED.with(ROTATE_270)));
    public static final ITexturePalette TEST_270_ZOOM = TexturePaletteRegistry.addZoomedPallete(TEST_270);

    public static final ITexturePalette TEST_4X4 = TexturePaletteRegistry.addTexturePallette("test4x4",
            ConfigXM.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(1).withScale(TextureScale.SMALL)
                    .withLayout(TextureLayout.SIMPLE).withRotation(FIXED.with(ROTATE_NONE))
                    .withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.HIDDEN_TILES));

    public static final ITexturePalette TEST_4x4_90 = TexturePaletteRegistry.addTexturePallette("test4x4_90",
            ConfigXM.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0",
            new TexturePaletteSpec(TEST_4X4).withRotation(FIXED.with(ROTATE_90)));
    public static final ITexturePalette TEST_4x4_180 = TexturePaletteRegistry.addTexturePallette("test4x4_180",
            ConfigXM.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0",
            new TexturePaletteSpec(TEST_4X4).withRotation(FIXED.with(ROTATE_180)));
    public static final ITexturePalette TEST_4x4_270 = TexturePaletteRegistry.addTexturePallette("test4x4_270",
            ConfigXM.BLOCKS.showHiddenTextures ? "test4x4" : "noise_moderate_0_0",
            new TexturePaletteSpec(TEST_4X4).withRotation(FIXED.with(ROTATE_270)));

    // ======================================================================
    // TILES - REGULAR
    //
    // Textures of general interest for any mod
    // ======================================================================

    public static final ITexturePalette BLOCK_COBBLE = TexturePaletteRegistry.addTexturePallette("cobble", "cobble",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(4).withScale(TextureScale.SINGLE)
                    .withLayout(TextureLayout.SPLIT_X_8).withRotation(RANDOM.with(ROTATE_NONE))
                    .withRenderIntent(TextureRenderIntent.BASE_ONLY).withGroups(TextureGroup.STATIC_TILES));

    public static final ITexturePalette BLOCK_COBBLE_ZOOM = TexturePaletteRegistry.addZoomedPallete(BLOCK_COBBLE);

    public static final ITexturePalette BLOCK_NOISE_STRONG = TexturePaletteRegistry.addTexturePallette("noise_strong",
            "noise_strong", new TexturePaletteSpec(BLOCK_COBBLE));

    public static final ITexturePalette BLOCK_NOISE_STRONG_ZOOM = TexturePaletteRegistry
            .addZoomedPallete(BLOCK_NOISE_STRONG);

    public static final ITexturePalette BLOCK_NOISE_MODERATE = TexturePaletteRegistry
            .addTexturePallette("noise_moderate", "noise_moderate", new TexturePaletteSpec(BLOCK_COBBLE));

    public static final ITexturePalette BLOCK_NOISE_MODERATE_ZOOM = TexturePaletteRegistry
            .addZoomedPallete(BLOCK_NOISE_MODERATE);

    public static final ITexturePalette BLOCK_NOISE_SUBTLE = TexturePaletteRegistry.addTexturePallette("noise_subtle",
            "noise_subtle", new TexturePaletteSpec(BLOCK_COBBLE));

    public static final ITexturePalette BLOCK_NOISE_SUBTLE_ZOOM = TexturePaletteRegistry
            .addZoomedPallete(BLOCK_NOISE_SUBTLE);

    public static final ITexturePalette WHITE = TexturePaletteRegistry.addTexturePallette("white", "white",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(1).withScale(TextureScale.SINGLE)
                    .withLayout(TextureLayout.SPLIT_X_8).withRotation(FIXED.with(ROTATE_NONE))
                    .withGroups(TextureGroup.STATIC_TILES));

    public static final ITexturePalette BORDER_SMOOTH_BLEND = TexturePaletteRegistry.addTexturePallette(
            "border_smooth_blended", "border_smooth_blended",
            new TexturePaletteSpec(ExoticMatter.INSTANCE).withVersionCount(1).withScale(TextureScale.SINGLE)
                    .withLayout(TextureLayout.BORDER_13).withRotation(FIXED.with(ROTATE_NONE))
                    .withRenderIntent(TextureRenderIntent.OVERLAY_ONLY).withGroups(TextureGroup.STATIC_BORDERS));
}
