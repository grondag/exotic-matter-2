/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm.init;

import static grondag.xm.api.texture.TextureGroup.HIDDEN_TILES;
import static grondag.xm.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm.api.texture.TextureGroup.STATIC_TILES;
import static grondag.xm.api.texture.TextureLayoutMap.BORDER_13;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm.api.texture.TextureRotation.ROTATE_180;
import static grondag.xm.api.texture.TextureRotation.ROTATE_270;
import static grondag.xm.api.texture.TextureRotation.ROTATE_90;
import static grondag.xm.api.texture.TextureRotation.ROTATE_NONE;
import static grondag.xm.api.texture.TextureRotation.ROTATE_RANDOM;
import static grondag.xm.api.texture.TextureScale.GIANT;
import static grondag.xm.api.texture.TextureScale.LARGE;
import static grondag.xm.api.texture.TextureScale.MEDIUM;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureScale.SMALL;
import static grondag.xm.api.texture.TextureScale.TINY;

import grondag.xm.Xm;
import grondag.xm.XmConfig;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.texture.TextureSetImpl;
import grondag.xm.texture.TextureSetRegistryImpl;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;

public class XmTextures {

    /**
     * Main purpose of being here is to force instantiation of other static members.
     */
    public static void init() {
        Xm.LOG.debug("Registering Exotic Matter textures");

        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEX).register((atlas, registry) -> {
            if (atlas == MinecraftClient.getInstance().getSpriteAtlas()) {
                TextureSetRegistryImpl texReg = TextureSetRegistryImpl.INSTANCE;
                final int limit = texReg.size();
                for (int i = 0; i < limit; i++) {
                    TextureSetImpl set = texReg.getByIndex(i);
                    assert set != null;
                    if (set.used()) {
                        set.prestitch(id -> registry.register(id));
                    }
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
            .baseTextureName(XmConfig.BLOCKS.showHiddenTextures ? "exotic-matter:blocks/bigtex_single" : "exotic-matter:blocks/noise_moderate_0_0").versionCount(1)
            .scale(SMALL).layout(TextureLayoutMap.SINGLE).rotation(ROTATE_NONE).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:bigtex_test_single");

    public static final TextureSet BIGTEX_TEST1 = TextureSet.builder().displayNameToken("big_tex_test1")
            .baseTextureName(XmConfig.BLOCKS.showHiddenTextures ? "exotic-matter:blocks/bigtex" : "exotic-matter:blocks/noise_moderate_0").versionCount(4).scale(TINY)
            .layout(TextureLayoutMap.VERSIONED).rotation(ROTATE_RANDOM).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:big_tex_test1");

    public static final TextureSet BIGTEX_TEST2 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test2").scale(SMALL).build("exotic-matter:big_tex_test2");

    public static final TextureSet BIGTEX_TEST3 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test3").scale(MEDIUM).build("exotic-matter:big_tex_test3");

    public static final TextureSet BIGTEX_TEST4 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test4").scale(LARGE).build("exotic-matter:big_tex_test4");

    public static final TextureSet BIGTEX_TEST5 = TextureSet.builder(BIGTEX_TEST1).displayNameToken("big_tex_test5").scale(GIANT).build("exotic-matter:big_tex_test5");

    public static final TextureSet TEST = TextureSet.builder().displayNameToken("test")
            .baseTextureName(XmConfig.BLOCKS.showHiddenTextures ? "exotic-matter:blocks/test" : "exotic-matter:blocks/noise_moderate_0").versionCount(2).scale(SINGLE)
            .layout(TextureLayoutMap.VERSIONED).rotation(ROTATE_NONE).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:test");

    public static final TextureSet TEST_ZOOM = TextureSet.builder(TEST).displayNameToken("test_zoom").scale(TINY).build("exotic-matter:test_zoom");

    public static final TextureSet TEST_90 = TextureSet.builder(TEST).displayNameToken("test_90").rotation(ROTATE_90).build("exotic-matter:test_90");

    public static final TextureSet TEST_90_ZOOM = TextureSet.builder(TEST_90).displayNameToken("test_90_zoom").scale(TINY).build("exotic-matter:test_90_zoom");

    public static final TextureSet TEST_180 = TextureSet.builder(TEST).displayNameToken("test_180").rotation(ROTATE_90).build("exotic-matter:test_180");

    public static final TextureSet TEST_180_ZOOM = TextureSet.builder(TEST_180).displayNameToken("test_180_zoom").scale(TINY).build("exotic-matter:test_180_zoom");

    public static final TextureSet TEST_270 = TextureSet.builder(TEST).displayNameToken("test_270").rotation(ROTATE_90).build("exotic-matter:test_270");

    public static final TextureSet TEST_270_ZOOM = TextureSet.builder(TEST_270).displayNameToken("test_270_zoom").scale(TINY).build("exotic-matter:test_270_zoom");

    public static final TextureSet TEST_4X4 = TextureSet.builder().displayNameToken("test4x4")
            .baseTextureName(XmConfig.BLOCKS.showHiddenTextures ? "exotic-matter:blocks/test4x4" : "exotic-matter:blocks/noise_moderate_0_0").versionCount(1).scale(SMALL)
            .layout(TextureLayoutMap.SINGLE).rotation(ROTATE_NONE).renderIntent(BASE_ONLY).groups(HIDDEN_TILES).build("exotic-matter:test4x4");

    public static final TextureSet TEST_4x4_90 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_90").rotation(ROTATE_90).build("exotic-matter:test4x4_90");

    public static final TextureSet TEST_4x4_180 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_180").rotation(ROTATE_180).build("exotic-matter:test4x4_180");

    public static final TextureSet TEST_4x4_270 = TextureSet.builder(TEST_4X4).displayNameToken("test4x4_270").rotation(ROTATE_270).build("exotic-matter:test4x4_270");

    // ======================================================================
    // TILES - REGULAR
    //
    // Textures of general interest for any mod
    // ======================================================================

    public static final TextureSet BLOCK_COBBLE = TextureSet.builder().displayNameToken("cobble").baseTextureName("exotic-matter:blocks/cobble").versionCount(4)
            .scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).rotation(ROTATE_RANDOM).renderIntent(BASE_ONLY).groups(STATIC_TILES).build("exotic-matter:cobble");

    public static final TextureSet BLOCK_COBBLE_ZOOM = TextureSet.builder(BLOCK_COBBLE).displayNameToken("cobble_zoom").scale(TINY).build("exotic-matter:cobble_zoom");

    public static final TextureSet BLOCK_NOISE_STRONG = TextureSet.builder(BLOCK_COBBLE).displayNameToken("noise_strong")
            .baseTextureName("exotic-matter:blocks/noise_strong").build("exotic-matter:noise_strong");

    public static final TextureSet BLOCK_NOISE_STRONG_ZOOM = TextureSet.builder(BLOCK_NOISE_STRONG).displayNameToken("noise_strong_zoom").scale(TINY)
            .build("exotic-matter:noise_strong_zoom");

    public static final TextureSet BLOCK_NOISE_MODERATE = TextureSet.builder(BLOCK_COBBLE).displayNameToken("noise_moderate")
            .baseTextureName("exotic-matter:blocks/noise_moderate").build("exotic-matter:noise_moderate");

    public static final TextureSet BLOCK_NOISE_MODERATE_ZOOM = TextureSet.builder(BLOCK_NOISE_STRONG).displayNameToken("noise_moderate_zoom").scale(TINY)
            .build("exotic-matter:noise_moderate_zoom");

    public static final TextureSet BLOCK_NOISE_SUBTLE = TextureSet.builder(BLOCK_COBBLE).displayNameToken("noise_subtle")
            .baseTextureName("exotic-matter:blocks/noise_subtle").build("exotic-matter:noise_subtle");

    public static final TextureSet BLOCK_NOISE_SUBTLE_ZOOM = TextureSet.builder(BLOCK_NOISE_STRONG).displayNameToken("noise_subtle_zoom").scale(TINY)
            .build("exotic-matter:noise_subtle_zoom");

    public static final TextureSet WHITE = TextureSet.builder().displayNameToken("white").baseTextureName("exotic-matter:blocks/white").versionCount(1).scale(SINGLE)
            .layout(TextureLayoutMap.VERSION_X_8).rotation(ROTATE_NONE).groups(STATIC_TILES).build("exotic-matter:white");

    public static final TextureSet BORDER_SMOOTH_BLEND = TextureSet.builder().displayNameToken("border_smooth_blended")
            .baseTextureName("exotic-matter:blocks/border_smooth_blended").versionCount(1).scale(SINGLE).layout(BORDER_13).rotation(ROTATE_NONE)
            .renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_smooth_blended");

    public static final TextureSet SANDSTONE = TextureSet.builder().displayNameToken("sandstone").baseTextureName("exotic-matter:blocks/sandstone").versionCount(1)
            .scale(MEDIUM).layout(TextureLayoutMap.SINGLE).rotation(ROTATE_RANDOM).renderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .groups(STATIC_TILES).build("exotic-matter:sandstone");

    public static final TextureSet SANDSTONE_ZOOM = TextureSet.builder(SANDSTONE).displayNameToken("sandstone_zoom").scale(LARGE).build("exotic-matter:sandstone_zoom");

    public static final TextureSet SANDSTONE_ZOOM2 = TextureSet.builder(SANDSTONE).displayNameToken("sandstone_zoom2").scale(GIANT)
            .build("exotic-matter:sandstone_zoom2");
}
