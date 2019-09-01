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

import static grondag.xm.api.texture.TextureTransform.ROTATE_180;
import static grondag.xm.api.texture.TextureTransform.ROTATE_270;
import static grondag.xm.api.texture.TextureTransform.ROTATE_90;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;
import static grondag.xm.texture.TextureSetHelper.addDecal;

import grondag.xm.api.texture.TextureSet;

public class XmArtDevices {
    private static final String ASSETS = "exotic-art";
    
    public static final TextureSet DECAL_SMALL_DOT = addDecal(ASSETS, "small_dot", "small_dot", IDENTITY);
    public static final TextureSet DECAL_MEDIUM_DOT = addDecal(ASSETS, "medium_dot", "medium_dot", IDENTITY);
    public static final TextureSet DECAL_LARGE_DOT = addDecal(ASSETS, "big_dot", "big_dot", IDENTITY);
    public static final TextureSet DECAL_SMALL_SQUARE = addDecal(ASSETS, "small_square", "small_square", IDENTITY);
    public static final TextureSet DECAL_MEDIUM_SQUARE = addDecal(ASSETS, "medium_square", "medium_square", IDENTITY);
    public static final TextureSet DECAL_LARGE_SQUARE = addDecal(ASSETS, "big_square", "big_square", IDENTITY);
    public static final TextureSet DECAL_BIG_TRIANGLE = addDecal(ASSETS, "big_triangle", "big_triangle", IDENTITY);
    public static final TextureSet DECAL_BIG_DIAMOND = addDecal(ASSETS, "big_diamond", "big_diamond", IDENTITY);
    public static final TextureSet DECAL_BIG_PENTAGON = addDecal(ASSETS, "big_pentagon", "big_pentagon", IDENTITY);
    public static final TextureSet DECAL_BIG_HEXAGON = addDecal(ASSETS, "big_hexagon", "big_hexagon", IDENTITY);
    public static final TextureSet DECAL_STAR_16 = addDecal(ASSETS, "star_16", "star_16", IDENTITY);
    public static final TextureSet DECAL_STAR_12 = addDecal(ASSETS, "star_12", "star_12", IDENTITY);
    public static final TextureSet DECAL_STAR_8 = addDecal(ASSETS, "star_8", "star_8", IDENTITY);
    public static final TextureSet DECAL_STAR_5 = addDecal(ASSETS, "star_5", "star_5", IDENTITY);
    public static final TextureSet DECAL_STAR_4 = addDecal(ASSETS, "star_4", "star_4", IDENTITY);
    public static final TextureSet DECAL_TWO_DOTS = addDecal(ASSETS, "two_dots", "two_dots", IDENTITY);
    public static final TextureSet DECAL_TWO_DOTS_RANDOM = addDecal(ASSETS, "two_dots", "two_dots", ROTATE_RANDOM);
    public static final TextureSet DECAL_DUST = addDecal(ASSETS, "dust", "dust", IDENTITY);
    public static final TextureSet DECAL_MIX = addDecal(ASSETS, "mix", "mix", IDENTITY);
    public static final TextureSet DECAL_MIX_90 = addDecal(ASSETS, "mix_90", "mix", ROTATE_90);
    public static final TextureSet DECAL_MIX_180 = addDecal(ASSETS, "mix_180", "mix", ROTATE_180);
    public static final TextureSet DECAL_MIX_270 = addDecal(ASSETS, "mix_270", "mix", ROTATE_270);
    public static final TextureSet DECAL_DRIP = addDecal(ASSETS, "drip", "drip", IDENTITY);
    public static final TextureSet DECAL_FUNNEL = addDecal(ASSETS, "funnel", "funnel", IDENTITY);
    public static final TextureSet DECAL_ARROW = addDecal(ASSETS, "arrow", "arrow", IDENTITY);
    public static final TextureSet DECAL_ARROW_90 = addDecal(ASSETS, "arrow_90", "arrow", ROTATE_90);
    public static final TextureSet DECAL_ARROW_180 = addDecal(ASSETS, "arrow_180", "arrow", ROTATE_180);
    public static final TextureSet DECAL_ARROW_270 = addDecal(ASSETS, "arrow_270", "arrow", ROTATE_270);

    public static final TextureSet MATERIAL_GRADIENT = addDecal(ASSETS, "material_gradient", "material_gradient", IDENTITY);

    public static final TextureSet DECAL_BUILDER = addDecal(ASSETS, "symbol_builder", "symbol_builder", IDENTITY);
    public static final TextureSet DECAL_CHEST = addDecal(ASSETS, "symbol_chest", "symbol_chest", IDENTITY);

    public static final TextureSet MACHINE_POWER_ON = addDecal(ASSETS, "on", "on", IDENTITY);
    public static final TextureSet MACHINE_POWER_OFF = addDecal(ASSETS, "off", "off", IDENTITY);

    public static final TextureSet MACHINE_GAUGE_INNER = addDecal(ASSETS, "gauge_inner", "gauge_inner_256", IDENTITY);
    public static final TextureSet MACHINE_GAUGE_MAIN = addDecal(ASSETS, "gauge_main", "gauge_main_256", IDENTITY);
    public static final TextureSet MACHINE_GAGUE_MARKS = addDecal(ASSETS, "gauge_background", "gauge_background_256", IDENTITY);
    public static final TextureSet MACHINE_GAUGE_FULL_MARKS = addDecal(ASSETS, "gauge_marks", "gauge_marks_256", IDENTITY);

    public static final TextureSet MACHINE_POWER_BACKGROUND = addDecal(ASSETS, "power_background", "power_background_128", IDENTITY);
    public static final TextureSet MACHINE_POWER_FOREGROUND = addDecal(ASSETS, "power_foreground", "power_foreground_128", IDENTITY);
    public static final TextureSet DECAL_NO = addDecal(ASSETS, "no", "no_128", IDENTITY);
    public static final TextureSet DECAL_MATERIAL_SHORTAGE = addDecal(ASSETS, "material_shortage", "material_shortage", IDENTITY);
    public static final TextureSet DECAL_ELECTRICITY = addDecal(ASSETS, "electricity", "electricity_64", IDENTITY);
    public static final TextureSet DECAL_CMY = addDecal(ASSETS, "cmy", "cmy", IDENTITY);
    public static final TextureSet DECAL_FLAME = addDecal(ASSETS, "flame", "flame_64", IDENTITY);

}
