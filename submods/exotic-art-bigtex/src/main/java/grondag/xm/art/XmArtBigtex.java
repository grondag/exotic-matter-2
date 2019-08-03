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

package grondag.xm.art;

import static grondag.xm.api.texture.TextureRotation.ROTATE_90;
import static grondag.xm.api.texture.TextureRotation.ROTATE_NONE;
import static grondag.xm.texture.TextureSetHelper.addBigTex;
import static grondag.xm.texture.TextureSetHelper.addZoom;
import static grondag.xm.texture.TextureSetHelper.addZoom2;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;

public class XmArtBigtex {
    private static final String ASSETS = "exotic-art";
    
    public static final TextureSet BIGTEX_MARBLE = addBigTex(ASSETS, "marble");
    public static final TextureSet BIGTEX_MARBLE_ZOOM = addZoom(BIGTEX_MARBLE);
    public static final TextureSet BIGTEX_MARBLE_ZOOM_X2 = addZoom2(BIGTEX_MARBLE);

    public static final TextureSet BIGTEX_WEATHERED_STONE = addBigTex(ASSETS, "weathered_smooth_stone");
    public static final TextureSet BIGTEX_WEATHERED_STONE_ZOOM = addZoom(BIGTEX_WEATHERED_STONE);
    public static final TextureSet BIGTEX_WEATHERED_STONE_ZOOM_X2 = addZoom2(BIGTEX_WEATHERED_STONE);

    public static final TextureSet BIGTEX_SANDSTONE = addBigTex(ASSETS, "sandstone");
    public static final TextureSet BIGTEX_SANDSTONE_ZOOM = addZoom(BIGTEX_SANDSTONE);
    public static final TextureSet BIGTEX_SANDSTONE_ZOOM_X2 = addZoom2(BIGTEX_SANDSTONE);

    public static final TextureSet BIGTEX_ASPHALT = addBigTex(ASSETS, "asphalt");
    public static final TextureSet BIGTEX_ASPHALT_ZOOM = addZoom(BIGTEX_ASPHALT);
    public static final TextureSet BIGTEX_ASPHALT_ZOOM_X2 = addZoom2(BIGTEX_ASPHALT);

    public static final TextureSet BIGTEX_WORN_ASPHALT = addBigTex(ASSETS, "worn_asphalt");
    public static final TextureSet BIGTEX_WORN_ASPHALT_ZOOM = addZoom(BIGTEX_WORN_ASPHALT);
    public static final TextureSet BIGTEX_WORN_ASPHALT_ZOOM_X2 = addZoom2(BIGTEX_WORN_ASPHALT);

    public static final TextureSet BIGTEX_WOOD = TextureSet.builder().displayNameToken("wood").baseTextureName("exotic-art:blocks/wood").versionCount(1)
            .scale(TextureScale.MEDIUM).layout(TextureLayoutMap.SINGLE).rotation(ROTATE_NONE).renderIntent(TextureRenderIntent.BASE_ONLY)
            .groups(TextureGroup.STATIC_TILES).build("exotic-art:wood");
    public static final TextureSet BIGTEX_WOOD_ZOOM = addZoom(BIGTEX_WOOD);
    public static final TextureSet BIGTEX_WOOD_ZOOM_X2 = addZoom2(BIGTEX_WOOD_ZOOM);

    public static final TextureSet BIGTEX_WOOD_FLIP = TextureSet.builder(BIGTEX_WOOD).displayNameToken("wood_flip").rotation(ROTATE_90)
            .build("exotic-art:wood_flip");

    public static final TextureSet BIGTEX_WOOD_ZOOM_FLIP = addZoom(BIGTEX_WOOD_FLIP);
    public static final TextureSet BIGTEX_WOOD_ZOOM_X2_FLIP = addZoom2(BIGTEX_WOOD_ZOOM_FLIP);

    public static final TextureSet BIGTEX_GRANITE = addBigTex(ASSETS, "granite");
    public static final TextureSet BIGTEX_GRANITE_ZOOM = addZoom(BIGTEX_GRANITE);
    public static final TextureSet BIGTEX_GRANITE_ZOOM_X2 = addZoom2(BIGTEX_GRANITE);

    public static final TextureSet BIGTEX_SLATE = addBigTex(ASSETS, "slate");
    public static final TextureSet BIGTEX_SLATE_ZOOM = addZoom(BIGTEX_SLATE);
    public static final TextureSet BIGTEX_SLATE_ZOOM_X2 = addZoom2(BIGTEX_SLATE);

    public static final TextureSet BIGTEX_ROUGH_ROCK = addBigTex(ASSETS, "rough_rock");
    public static final TextureSet BIGTEX_ROUGH_ROCK_ZOOM = addZoom(BIGTEX_ROUGH_ROCK);
    public static final TextureSet BIGTEX_ROUGH_ROCK_ZOOM_X2 = addZoom2(BIGTEX_ROUGH_ROCK);

    public static final TextureSet BIGTEX_CRACKED_EARTH = addBigTex(ASSETS, "cracked_earth");
    public static final TextureSet BIGTEX_CRACKED_EARTH_ZOOM = addZoom(BIGTEX_CRACKED_EARTH);
    public static final TextureSet BIGTEX_CRACKED_EARTH_ZOOM_X2 = addZoom2(BIGTEX_CRACKED_EARTH);
}
