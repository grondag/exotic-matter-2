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

import static grondag.xm.api.texture.TextureRotation.ROTATE_NONE;
import static grondag.xm.texture.TextureSetHelper.addBorderRandom;
import static grondag.xm.texture.TextureSetHelper.addBorderSingle;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;

public class XmArtBorders {
    private static final String ASSETS = "exotic-art";
    
    public static final TextureSet MASONRY_SIMPLE = TextureSet.builder().displayNameToken("masonry_simple").baseTextureName(ASSETS + ":blocks/masonry_simple")
            .versionCount(1).scale(TextureScale.SINGLE).layout(TextureLayoutMap.MASONRY_5).rotation(ROTATE_NONE).renderIntent(TextureRenderIntent.OVERLAY_ONLY)
            .groups(TextureGroup.STATIC_BORDERS).build(ASSETS + ":masonry_simple");

    public static final TextureSet BORDER_SINGLE_PINSTRIPE = addBorderSingle(ASSETS, "border_single_pinstripe");
    public static final TextureSet BORDER_INSET_PINSTRIPE = addBorderSingle(ASSETS, "border_inset_pinstripe");
    public static final TextureSet BORDER_GRITTY_INSET_PINSTRIPE = addBorderRandom(ASSETS, "border_gritty_inset_pinstripe", false, false);
    public static final TextureSet BORDER_SINGLE_LINE = addBorderSingle(ASSETS, "border_single_line");
    public static final TextureSet BORDER_SINGLE_BOLD_LINE = addBorderSingle(ASSETS, "border_single_bold_line");
    public static final TextureSet BORDER_SINGLE_FAT_LINE = addBorderSingle(ASSETS, "border_single_fat_line");
    public static final TextureSet BORDER_GRITTY_FAT_LINE = addBorderRandom(ASSETS, "border_gritty_fat_line", false, false);
    public static final TextureSet BORDER_DOUBLE_MIXED_LINES = addBorderSingle(ASSETS, "border_double_mixed_lines");
    public static final TextureSet BORDER_DOUBLE_PINSTRIPES = addBorderSingle(ASSETS, "border_double_pinstripes");
    public static final TextureSet BORDER_INSET_DOUBLE_PINSTRIPES = addBorderSingle(ASSETS, "border_inset_double_pinstripes");
    public static final TextureSet BORDER_TRIPLE_MIXED_LINES = addBorderSingle(ASSETS, "border_triple_mixed_lines");
    public static final TextureSet BORDER_DOUBLE_DOUBLE = addBorderSingle(ASSETS, "border_double_double");
    public static final TextureSet BORDER_WHITEWALL = addBorderSingle(ASSETS, "border_whitewall");
    public static final TextureSet BORDER_GRITTY_WHITEWALL = addBorderRandom(ASSETS, "border_gritty_whitewall", false, false);

    public static final TextureSet BORDER_PINSTRIPE_DASH = addBorderSingle(ASSETS, "border_pinstripe_dash");
    public static final TextureSet BORDER_INSET_DOTS_1 = addBorderSingle(ASSETS, "border_inset_dots_1");
    public static final TextureSet BORDER_INSET_DOTS_2 = addBorderSingle(ASSETS, "border_inset_dots_2");
    public static final TextureSet BORDER_INSET_PIN_DOTS = addBorderSingle(ASSETS, "border_inset_pin_dots");
    public static final TextureSet BORDER_CHANNEL_DOTS = addBorderSingle(ASSETS, "border_channel_dots");
    public static final TextureSet BORDER_CHANNEL_PIN_DOTS = addBorderSingle(ASSETS, "border_channel_pin_dots");

    public static final TextureSet BORDER_CHANNEL_CHECKERBOARD = addBorderSingle(ASSETS, "border_channel_checkerboard");
    public static final TextureSet BORDER_CHECKERBOARD = addBorderSingle(ASSETS, "border_checkerboard");
    public static final TextureSet BORDER_GRITTY_CHECKERBOARD = addBorderRandom(ASSETS, "border_gritty_checkerboard", false, false);

    public static final TextureSet BORDER_GROOVY_STRIPES = addBorderSingle(ASSETS, "border_groovy_stripes");
    public static final TextureSet BORDER_GRITTY_GROOVES = addBorderRandom(ASSETS, "border_gritty_grooves", false, false);
    public static final TextureSet BORDER_GROOVY_PINSTRIPES = addBorderSingle(ASSETS, "border_groovy_pinstripes");
    public static final TextureSet BORDER_GRITTY_PINSTRIPE_GROOVES = addBorderRandom(ASSETS, "border_gritty_pinstripe_grooves", false, false);

    public static final TextureSet BORDER_ZIGZAG = addBorderSingle(ASSETS, "border_zigzag");
    public static final TextureSet BORDER_INVERSE_ZIGZAG = addBorderSingle(ASSETS, "border_inverse_zigzag");
    public static final TextureSet BORDER_CAUTION = addBorderSingle(ASSETS, "border_caution");
    public static final TextureSet BORDER_FILMSTRIP = addBorderSingle(ASSETS, "border_filmstrip");
    public static final TextureSet BORDER_CHANNEL_LINES = addBorderSingle(ASSETS, "border_channel_lines");
    public static final TextureSet BORDER_SIGNAL = addBorderSingle(ASSETS, "border_signal");
    public static final TextureSet BORDER_GRITTY_SIGNAL = addBorderRandom(ASSETS, "border_gritty_signal", false, false);
    public static final TextureSet BORDER_LOGIC = addBorderRandom(ASSETS, "border_logic", true, false);
    public static final TextureSet BORDER_INVERSE_TILE_1 = addBorderRandom(ASSETS, "border_inverse_logic_1", true, true);
    public static final TextureSet BORDER_INVERSE_TILE_2 = addBorderRandom(ASSETS, "border_inverse_logic_2", true, true);
}
