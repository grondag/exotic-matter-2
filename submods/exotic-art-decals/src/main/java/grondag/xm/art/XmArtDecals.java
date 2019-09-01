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

import static grondag.xm.api.texture.TextureTransform.ROTATE_90;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;
import static grondag.xm.texture.TextureSetHelper.addDecal;

import grondag.xm.api.texture.TextureSet;

public class XmArtDecals {
    private static final String ASSETS = "exotic-art";
    
    public static final TextureSet DECAL_SKINNY_DIAGONAL_RIDGES = addDecal(ASSETS, "skinny_diagonal_ridges", "skinny_diagonal_ridges", IDENTITY);
    public static final TextureSet DECAL_THICK_DIAGONAL_CROSS_RIDGES = addDecal(ASSETS, "thick_diagonal_cross_ridges", "thick_diagonal_cross_ridges", IDENTITY);
    public static final TextureSet DECAL_THICK_DIAGONAL_RIDGES = addDecal(ASSETS, "thick_diagonal_ridges", "thick_diagonal_ridges", IDENTITY);
    public static final TextureSet DECAL_THIN_DIAGONAL_CROSS_RIDGES = addDecal(ASSETS, "thin_diagonal_cross_ridges", "thin_diagonal_cross_ridges", IDENTITY);
    public static final TextureSet DECAL_THIN_DIAGONAL_RIDGES = addDecal(ASSETS, "thin_diagonal_ridges", "thin_diagonal_ridges", IDENTITY);
    public static final TextureSet DECAL_THIN_DIAGONAL_CROSS_BARS = addDecal(ASSETS, "thin_diagonal_cross_bars", "thin_diagonal_cross_bars", IDENTITY);
    public static final TextureSet DECAL_THIN_DIAGONAL_BARS = addDecal(ASSETS, "thin_diagonal_bars", "thin_diagonal_bars", IDENTITY);
    public static final TextureSet DECAL_SKINNY_DIAGNAL_CROSS_BARS = addDecal(ASSETS, "skinny_diagonal_cross_bars", "skinny_diagonal_cross_bars", IDENTITY);
    public static final TextureSet DECAL_SKINNY_DIAGONAL_BARS = addDecal(ASSETS, "skinny_diagonal_bars", "skinny_diagonal_bars", IDENTITY);
    public static final TextureSet DECAL_DIAGONAL_CROSS_BARS = addDecal(ASSETS, "diagonal_cross_bars", "diagonal_cross_bars", IDENTITY);
    public static final TextureSet DECAL_DIAGONAL_BARS = addDecal(ASSETS, "diagonal_bars", "diagonal_bars", IDENTITY);
    public static final TextureSet DECAL_FAT_DIAGONAL_CROSS_BARS = addDecal(ASSETS, "fat_diagonal_cross_bars", "fat_diagonal_cross_bars", IDENTITY);
    public static final TextureSet DECAL_FAT_DIAGONAL_BARS = addDecal(ASSETS, "fat_diagonal_bars", "fat_diagonal_bars", IDENTITY);
    public static final TextureSet DECAL_DIAGONAL_CROSS_RIDGES = addDecal(ASSETS, "diagonal_cross_ridges", "diagonal_cross_ridges", IDENTITY);
    public static final TextureSet DECAL_DIAGONAL_RIDGES = addDecal(ASSETS, "diagonal_ridges", "diagonal_ridges", IDENTITY);
    public static final TextureSet DECAL_SKINNY_BARS = addDecal(ASSETS, "skinny_bars", "skinny_bars", IDENTITY);
    public static final TextureSet DECAL_FAT_BARS = addDecal(ASSETS, "fat_bars", "fat_bars", IDENTITY);
    public static final TextureSet DECAL_THICK_BARS = addDecal(ASSETS, "thick_bars", "thick_bars", IDENTITY);
    public static final TextureSet DECAL_THIN_BARS = addDecal(ASSETS, "thin_bars", "thin_bars", IDENTITY);
    public static final TextureSet DECAL_SKINNY_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "skinny_diagonal_ridges_90", "skinny_diagonal_ridges", ROTATE_90);
    public static final TextureSet DECAL_THICK_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "thick_diagonal_ridges_90", "thick_diagonal_ridges", ROTATE_90);
    public static final TextureSet DECAL_THIN_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "thin_diagonal_ridges_90", "thin_diagonal_ridges", ROTATE_90);
    public static final TextureSet DECAL_THIN_DIAGONAL_BARS_90 = addDecal(ASSETS, "thin_diagonal_bars_90", "thin_diagonal_bars", ROTATE_90);
    public static final TextureSet DECAL_SKINNY_DIAGONAL_BARS_90 = addDecal(ASSETS, "skinny_diagonal_bars_90", "skinny_diagonal_bars", ROTATE_90);
    public static final TextureSet DECAL_DIAGONAL_BARS_90 = addDecal(ASSETS, "diagonal_bars_90", "diagonal_bars", ROTATE_90);
    public static final TextureSet DECAL_FAT_DIAGONAL_BARS_90 = addDecal(ASSETS, "fat_diagonal_bars_90", "fat_diagonal_bars", ROTATE_90);
    public static final TextureSet DECAL_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "diagonal_ridges_90", "diagonal_ridges", ROTATE_90);
    public static final TextureSet DECAL_SKINNY_BARS_90 = addDecal(ASSETS, "skinny_bars_90", "skinny_bars", ROTATE_90);
    public static final TextureSet DECAL_FAT_BARS_90 = addDecal(ASSETS, "fat_bars_90", "fat_bars", ROTATE_90);
    public static final TextureSet DECAL_THICK_BARS_90 = addDecal(ASSETS, "thick_bars_90", "thick_bars", ROTATE_90);
    public static final TextureSet DECAL_THIN_BARS_90 = addDecal(ASSETS, "thin_bars_90", "thin_bars", ROTATE_90);
    public static final TextureSet DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "skinny_diagonal_ridges_random", "skinny_diagonal_ridges", ROTATE_RANDOM);
    public static final TextureSet DECAL_THICK_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "thick_diagonal_ridges_random", "thick_diagonal_ridges", ROTATE_RANDOM);
    public static final TextureSet DECAL_THIN_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "thin_diagonal_ridges_random", "thin_diagonal_ridges", ROTATE_RANDOM);
    public static final TextureSet DECAL_THIN_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "thin_diagonal_bars_random", "thin_diagonal_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_SKINNY_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "skinny_diagonal_bars_random", "skinny_diagonal_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "diagonal_bars_random", "diagonal_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_FAT_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "fat_diagonal_bars_random", "fat_diagonal_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "diagonal_ridges_random", "diagonal_ridges", ROTATE_RANDOM);
    public static final TextureSet DECAL_SKINNY_BARS_RANDOM = addDecal(ASSETS, "skinny_bars_random", "skinny_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_FAT_BARS_RANDOM = addDecal(ASSETS, "fat_bars_random", "fat_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_THICK_BARS_RANDOM = addDecal(ASSETS, "thick_bars_random", "thick_bars", ROTATE_RANDOM);
    public static final TextureSet DECAL_THIN_BARS_RANDOM = addDecal(ASSETS, "thin_bars_random", "thin_bars", ROTATE_RANDOM);

    public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_RIDGES = addDecal(ASSETS, "skinny_diagonal_ridges_seamless", "skinny_diagonal_ridges_seamless",
            IDENTITY);
    public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_CROSS_RIDGES = addDecal(ASSETS, "thick_diagonal_cross_ridges_seamless",
            "thick_diagonal_cross_ridges_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_RIDGES = addDecal(ASSETS, "thick_diagonal_ridges_seamless", "thick_diagonal_ridges_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_CROSS_RIDGES = addDecal(ASSETS, "thin_diagonal_cross_ridges_seamless",
            "thin_diagonal_cross_ridges_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_RIDGES = addDecal(ASSETS, "thin_diagonal_ridges_seamless", "thin_diagonal_ridges_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_CROSS_BARS = addDecal(ASSETS, "thin_diagonal_cross_bars_seamless", "thin_diagonal_cross_bars_seamless",
            IDENTITY);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_BARS = addDecal(ASSETS, "thin_diagonal_bars_seamless", "thin_diagonal_bars_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_SKINNY_DIAGNAL_CROSS_BARS = addDecal(ASSETS, "skinny_diagonal_cross_bars_seamless", "skinny_diagonal_cross_bars_seamless",
            IDENTITY);
    public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_BARS = addDecal(ASSETS, "skinny_diagonal_bars_seamless", "skinny_diagonal_bars_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_DIAGONAL_CROSS_BARS = addDecal(ASSETS, "diagonal_cross_bars_seamless", "diagonal_cross_bars_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_DIAGONAL_BARS = addDecal(ASSETS, "diagonal_bars_seamless", "diagonal_bars_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_CROSS_BARS = addDecal(ASSETS, "fat_diagonal_cross_bars_seamless", "fat_diagonal_cross_bars_seamless",
            IDENTITY);
    public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_BARS = addDecal(ASSETS, "fat_diagonal_bars_seamless", "fat_diagonal_bars_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_DIAGONAL_CROSS_RIDGES = addDecal(ASSETS, "diagonal_cross_ridges_seamless", "diagonal_cross_ridges_seamless", IDENTITY);
    public static final TextureSet DECAL_SOFT_DIAGONAL_RIDGES = addDecal(ASSETS, "diagonal_ridges_seamless", "diagonal_ridges_seamless", IDENTITY);

    public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "skinny_diagonal_ridges_90", "skinny_diagonal_ridges", ROTATE_90);
    public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "thick_diagonal_ridges_seamless_90", "thick_diagonal_ridges_seamless",
            ROTATE_90);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "thin_diagonal_ridges_seamless_90", "thin_diagonal_ridges_seamless",
            ROTATE_90);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_BARS_90 = addDecal(ASSETS, "thin_diagonal_bars_seamless_90", "thin_diagonal_bars_seamless", ROTATE_90);
    public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_BARS_90 = addDecal(ASSETS, "skinny_diagonal_bars_seamless_90", "skinny_diagonal_bars_seamless",
            ROTATE_90);
    public static final TextureSet DECAL_SOFT_DIAGONAL_BARS_90 = addDecal(ASSETS, "diagonal_bars_seamless_90", "diagonal_bars_seamless", ROTATE_90);
    public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_BARS_90 = addDecal(ASSETS, "fat_diagonal_bars_seamless_90", "fat_diagonal_bars_seamless", ROTATE_90);
    public static final TextureSet DECAL_SOFT_DIAGONAL_RIDGES_90 = addDecal(ASSETS, "diagonal_ridges_seamless_90", "diagonal_ridges_seamless", ROTATE_90);

    public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "skinny_diagonal_ridges_random", "skinny_diagonal_ridges",
            ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "thick_diagonal_ridges_seamless_random", "thick_diagonal_ridges_seamless",
            ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "thin_diagonal_ridges_seamless_random", "thin_diagonal_ridges_seamless",
            ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "thin_diagonal_bars_seamless_random", "thin_diagonal_bars_seamless",
            ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "skinny_diagonal_bars_seamless_random", "skinny_diagonal_bars_seamless",
            ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "diagonal_bars_seamless_random", "diagonal_bars_seamless", ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_BARS_RANDOM = addDecal(ASSETS, "fat_diagonal_bars_seamless_random", "fat_diagonal_bars_seamless",
            ROTATE_RANDOM);
    public static final TextureSet DECAL_SOFT_DIAGONAL_RIDGES_RANDOM = addDecal(ASSETS, "diagonal_ridges_seamless_random", "diagonal_ridges_seamless", ROTATE_RANDOM);

}
