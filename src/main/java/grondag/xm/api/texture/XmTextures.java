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
package grondag.xm.api.texture;

import static grondag.xm.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm.api.texture.TextureGroup.STATIC_TILES;
import static grondag.xm.api.texture.TextureLayoutMap.BORDER_13;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_CUTOUT_OKAY;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT;
import static grondag.xm.api.texture.TextureRotation.ROTATE_NONE;
import static grondag.xm.api.texture.TextureRotation.ROTATE_RANDOM;
import static grondag.xm.api.texture.TextureScale.GIANT;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.texture.TextureSetHelper.addBorderRandom;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public class XmTextures {
    private XmTextures() {}
    
    public static final TextureSet TILE_COBBLE = TextureSet.builder()
            .displayNameToken("cobble").baseTextureName("exotic-matter:blocks/cobble")
            .versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).rotation(ROTATE_RANDOM)
            .renderIntent(BASE_ONLY).groups(STATIC_TILES).build("exotic-matter:cobble");

    public static final TextureSet TILE_NOISE_STRONG = TextureSet.builder(TILE_COBBLE).displayNameToken("noise_strong")
            .baseTextureName("exotic-matter:blocks/noise_strong").build("exotic-matter:noise_strong");

    public static final TextureSet TILE_NOISE_MODERATE = TextureSet.builder(TILE_COBBLE).displayNameToken("noise_moderate")
            .baseTextureName("exotic-matter:blocks/noise_moderate").build("exotic-matter:noise_moderate");

    public static final TextureSet TILE_NOISE_LIGHT = TextureSet.builder(TILE_COBBLE).displayNameToken("noise_light")
            .baseTextureName("exotic-matter:blocks/noise_light").build("exotic-matter:noise_light");

    public static final TextureSet TILE_NOISE_SUBTLE = TextureSet.builder(TILE_COBBLE).displayNameToken("noise_subtle")
            .baseTextureName("exotic-matter:blocks/noise_subtle").build("exotic-matter:noise_subtle");

    public static final TextureSet TILE_NOISE_EXTREME = TextureSet.builder()
            .displayNameToken("noise_extreme").baseTextureName("exotic-matter:blocks/noise_extreme")
            .versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).rotation(ROTATE_RANDOM)
            .renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_TILES).build("exotic-matter:noise_extreme");
    
    public static final TextureSet WHITE = TextureSet.builder().displayNameToken("white").baseTextureName("exotic-matter:blocks/white").versionCount(1).scale(SINGLE)
            .layout(TextureLayoutMap.VERSION_X_8).rotation(ROTATE_NONE).groups(STATIC_TILES).build("exotic-matter:white");

    public static final TextureSet BORDER_SMOOTH_BLEND = TextureSet.builder().displayNameToken("border_smooth_blended")
            .baseTextureName("exotic-matter:blocks/border_smooth_blended").versionCount(1).scale(SINGLE).layout(BORDER_13).rotation(ROTATE_NONE)
            .renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_smooth_blended");

    public static final TextureSet BORDER_SINGLE_LINE = TextureSet.builder().displayNameToken("border_single_line")
            .baseTextureName("exotic-matter:blocks/border_single_line").versionCount(1).scale(SINGLE).layout(BORDER_13).rotation(ROTATE_NONE)
            .renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_single_line");

    public static final TextureSet BIGTEX_SANDSTONE = TextureSet.builder().displayNameToken("sandstone").baseTextureName("exotic-matter:blocks/sandstone").versionCount(1)
            .scale(GIANT).layout(TextureLayoutMap.SINGLE).rotation(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_NO_CUTOUT)
            .groups(STATIC_TILES).build("exotic-matter:sandstone");
    
    public static final TextureSet TILE_NOISE_BLUE_A = TextureSet.builder().displayNameToken("blue_noise_a")
            .baseTextureName("exotic-matter:blocks/noise_blue_0").versionCount(4)
            .scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSIONED)
            .rotation(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
            .groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_a");
    
    public static final TextureSet TILE_NOISE_BLUE_B = TextureSet.builder().displayNameToken("blue_noise_b")
            .baseTextureName("exotic-matter:blocks/noise_blue_1").versionCount(4)
            .scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSIONED)
            .rotation(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
            .groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_b");

    public static final TextureSet BORDER_GRITTY_SINGLE_LINE = addBorderRandom("exotic-matter", "border_gritty_single_line", false, false);
}
