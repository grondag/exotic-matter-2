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
package grondag.xm.api.texture.unstable;

import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_90;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;
import static grondag.xm.texture.TextureSetHelper.addBigTex;
import static grondag.xm.texture.TextureSetHelper.addBorderRandom;
import static grondag.xm.texture.TextureSetHelper.addBorderSingle;
import static grondag.xm.texture.TextureSetHelper.addDecal;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.Xm;
import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;

@Experimental
public enum UnstableTextures {
	;
	public static final TextureSet BORDER_GRITTY_SINGLE_LINE = addBorderRandom("exotic-matter", "border_gritty_single_line", false, false);

	public static final TextureSet BIGTEX_MARBLE = addBigTex(Xm.MODID, "marble");
	public static final TextureSet BIGTEX_WEATHERED_STONE = addBigTex(Xm.MODID, "weathered_smooth_stone");
	public static final TextureSet BIGTEX_ASPHALT = addBigTex(Xm.MODID, "asphalt");
	public static final TextureSet BIGTEX_WORN_ASPHALT = addBigTex(Xm.MODID, "worn_asphalt");
	public static final TextureSet BIGTEX_WOOD = TextureSet.builder().displayNameToken("wood").baseTextureName("exotic-matter:block/wood").versionCount(1)
			.scale(TextureScale.GIANT).layout(TextureLayoutMap.SINGLE).transform(IDENTITY).renderIntent(TextureRenderIntent.BASE_ONLY)
			.groups(TextureGroup.STATIC_TILES).build("exotic-matter:wood");
	public static final TextureSet BIGTEX_WOOD_FLIP = TextureSet.builder(BIGTEX_WOOD).displayNameToken("wood_flip").transform(ROTATE_90)
			.build("exotic-matter:wood_flip");

	public static final TextureSet BIGTEX_SLATE = addBigTex(Xm.MODID, "slate");
	public static final TextureSet BIGTEX_ROUGH_ROCK = addBigTex(Xm.MODID, "rough_rock");
	public static final TextureSet BIGTEX_CRACKED_EARTH = addBigTex(Xm.MODID, "cracked_earth");



	public static final TextureSet MASONRY_SIMPLE = TextureSet.builder().displayNameToken("masonry_simple").baseTextureName(Xm.MODID + ":block/masonry_simple")
			.versionCount(1).scale(TextureScale.SINGLE).layout(TextureLayoutMap.MASONRY_5).transform(IDENTITY).renderIntent(TextureRenderIntent.OVERLAY_ONLY)
			.groups(TextureGroup.STATIC_BORDERS).build(Xm.MODID + ":masonry_simple");

	public static final TextureSet BORDER_SINGLE_PINSTRIPE = addBorderSingle(Xm.MODID, "border_single_pinstripe");
	public static final TextureSet BORDER_INSET_PINSTRIPE = addBorderSingle(Xm.MODID, "border_inset_pinstripe");
	public static final TextureSet BORDER_GRITTY_INSET_PINSTRIPE = addBorderRandom(Xm.MODID, "border_gritty_inset_pinstripe", false, false);
	public static final TextureSet BORDER_SINGLE_BOLD_LINE = addBorderSingle(Xm.MODID, "border_single_bold_line");
	public static final TextureSet BORDER_SINGLE_FAT_LINE = addBorderSingle(Xm.MODID, "border_single_fat_line");
	public static final TextureSet BORDER_GRITTY_FAT_LINE = addBorderRandom(Xm.MODID, "border_gritty_fat_line", false, false);
	public static final TextureSet BORDER_DOUBLE_MIXED_LINES = addBorderSingle(Xm.MODID, "border_double_mixed_lines");
	public static final TextureSet BORDER_DOUBLE_PINSTRIPES = addBorderSingle(Xm.MODID, "border_double_pinstripes");
	public static final TextureSet BORDER_INSET_DOUBLE_PINSTRIPES = addBorderSingle(Xm.MODID, "border_inset_double_pinstripes");
	public static final TextureSet BORDER_TRIPLE_MIXED_LINES = addBorderSingle(Xm.MODID, "border_triple_mixed_lines");
	public static final TextureSet BORDER_DOUBLE_DOUBLE = addBorderSingle(Xm.MODID, "border_double_double");
	public static final TextureSet BORDER_WHITEWALL = addBorderSingle(Xm.MODID, "border_whitewall");
	public static final TextureSet BORDER_GRITTY_WHITEWALL = addBorderRandom(Xm.MODID, "border_gritty_whitewall", false, false);

	public static final TextureSet BORDER_PINSTRIPE_DASH = addBorderSingle(Xm.MODID, "border_pinstripe_dash");
	public static final TextureSet BORDER_INSET_DOTS_1 = addBorderSingle(Xm.MODID, "border_inset_dots_1");
	public static final TextureSet BORDER_INSET_DOTS_2 = addBorderSingle(Xm.MODID, "border_inset_dots_2");
	public static final TextureSet BORDER_INSET_PIN_DOTS = addBorderSingle(Xm.MODID, "border_inset_pin_dots");
	public static final TextureSet BORDER_CHANNEL_DOTS = addBorderSingle(Xm.MODID, "border_channel_dots");
	public static final TextureSet BORDER_CHANNEL_PIN_DOTS = addBorderSingle(Xm.MODID, "border_channel_pin_dots");

	public static final TextureSet BORDER_CHANNEL_CHECKERBOARD = addBorderSingle(Xm.MODID, "border_channel_checkerboard");
	public static final TextureSet BORDER_CHECKERBOARD = addBorderSingle(Xm.MODID, "border_checkerboard");
	public static final TextureSet BORDER_GRITTY_CHECKERBOARD = addBorderRandom(Xm.MODID, "border_gritty_checkerboard", false, false);

	public static final TextureSet BORDER_GROOVY_STRIPES = addBorderSingle(Xm.MODID, "border_groovy_stripes");
	public static final TextureSet BORDER_GRITTY_GROOVES = addBorderRandom(Xm.MODID, "border_gritty_grooves", false, false);
	public static final TextureSet BORDER_GROOVY_PINSTRIPES = addBorderSingle(Xm.MODID, "border_groovy_pinstripes");
	public static final TextureSet BORDER_GRITTY_PINSTRIPE_GROOVES = addBorderRandom(Xm.MODID, "border_gritty_pinstripe_grooves", false, false);

	public static final TextureSet BORDER_ZIGZAG = addBorderSingle(Xm.MODID, "border_zigzag");
	public static final TextureSet BORDER_INVERSE_ZIGZAG = addBorderSingle(Xm.MODID, "border_inverse_zigzag");
	public static final TextureSet BORDER_CAUTION = addBorderSingle(Xm.MODID, "border_caution");
	public static final TextureSet BORDER_FILMSTRIP = addBorderSingle(Xm.MODID, "border_filmstrip");
	public static final TextureSet BORDER_CHANNEL_LINES = addBorderSingle(Xm.MODID, "border_channel_lines");
	public static final TextureSet BORDER_SIGNAL = addBorderSingle(Xm.MODID, "border_signal");
	public static final TextureSet BORDER_GRITTY_SIGNAL = addBorderRandom(Xm.MODID, "border_gritty_signal", false, false);
	public static final TextureSet BORDER_LOGIC = addBorderRandom(Xm.MODID, "border_logic", true, false);
	public static final TextureSet BORDER_INVERSE_TILE_1 = addBorderRandom(Xm.MODID, "border_inverse_logic_1", true, true);
	public static final TextureSet BORDER_INVERSE_TILE_2 = addBorderRandom(Xm.MODID, "border_inverse_logic_2", true, true);

	public static final TextureSet DECAL_SKINNY_DIAGONAL_RIDGES = addDecal(Xm.MODID, "skinny_diagonal_ridges", "skinny_diagonal_ridges", IDENTITY);
	public static final TextureSet DECAL_THICK_DIAGONAL_CROSS_RIDGES = addDecal(Xm.MODID, "thick_diagonal_cross_ridges", "thick_diagonal_cross_ridges", IDENTITY);
	public static final TextureSet DECAL_THICK_DIAGONAL_RIDGES = addDecal(Xm.MODID, "thick_diagonal_ridges", "thick_diagonal_ridges", IDENTITY);
	public static final TextureSet DECAL_THIN_DIAGONAL_CROSS_RIDGES = addDecal(Xm.MODID, "thin_diagonal_cross_ridges", "thin_diagonal_cross_ridges", IDENTITY);
	public static final TextureSet DECAL_THIN_DIAGONAL_RIDGES = addDecal(Xm.MODID, "thin_diagonal_ridges", "thin_diagonal_ridges", IDENTITY);
	public static final TextureSet DECAL_THIN_DIAGONAL_CROSS_BARS = addDecal(Xm.MODID, "thin_diagonal_cross_bars", "thin_diagonal_cross_bars", IDENTITY);
	public static final TextureSet DECAL_THIN_DIAGONAL_BARS = addDecal(Xm.MODID, "thin_diagonal_bars", "thin_diagonal_bars", IDENTITY);
	public static final TextureSet DECAL_SKINNY_DIAGNAL_CROSS_BARS = addDecal(Xm.MODID, "skinny_diagonal_cross_bars", "skinny_diagonal_cross_bars", IDENTITY);
	public static final TextureSet DECAL_SKINNY_DIAGONAL_BARS = addDecal(Xm.MODID, "skinny_diagonal_bars", "skinny_diagonal_bars", IDENTITY);
	public static final TextureSet DECAL_DIAGONAL_CROSS_BARS = addDecal(Xm.MODID, "diagonal_cross_bars", "diagonal_cross_bars", IDENTITY);
	public static final TextureSet DECAL_DIAGONAL_BARS = addDecal(Xm.MODID, "diagonal_bars", "diagonal_bars", IDENTITY);
	public static final TextureSet DECAL_FAT_DIAGONAL_CROSS_BARS = addDecal(Xm.MODID, "fat_diagonal_cross_bars", "fat_diagonal_cross_bars", IDENTITY);
	public static final TextureSet DECAL_FAT_DIAGONAL_BARS = addDecal(Xm.MODID, "fat_diagonal_bars", "fat_diagonal_bars", IDENTITY);
	public static final TextureSet DECAL_DIAGONAL_CROSS_RIDGES = addDecal(Xm.MODID, "diagonal_cross_ridges", "diagonal_cross_ridges", IDENTITY);
	public static final TextureSet DECAL_DIAGONAL_RIDGES = addDecal(Xm.MODID, "diagonal_ridges", "diagonal_ridges", IDENTITY);
	public static final TextureSet DECAL_SKINNY_BARS = addDecal(Xm.MODID, "skinny_bars", "skinny_bars", IDENTITY);
	public static final TextureSet DECAL_FAT_BARS = addDecal(Xm.MODID, "fat_bars", "fat_bars", IDENTITY);
	public static final TextureSet DECAL_THICK_BARS = addDecal(Xm.MODID, "thick_bars", "thick_bars", IDENTITY);
	public static final TextureSet DECAL_THIN_BARS = addDecal(Xm.MODID, "thin_bars", "thin_bars", IDENTITY);
	public static final TextureSet DECAL_SKINNY_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "skinny_diagonal_ridges_90", "skinny_diagonal_ridges", ROTATE_90);
	public static final TextureSet DECAL_THICK_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "thick_diagonal_ridges_90", "thick_diagonal_ridges", ROTATE_90);
	public static final TextureSet DECAL_THIN_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "thin_diagonal_ridges_90", "thin_diagonal_ridges", ROTATE_90);
	public static final TextureSet DECAL_THIN_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "thin_diagonal_bars_90", "thin_diagonal_bars", ROTATE_90);
	public static final TextureSet DECAL_SKINNY_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "skinny_diagonal_bars_90", "skinny_diagonal_bars", ROTATE_90);
	public static final TextureSet DECAL_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "diagonal_bars_90", "diagonal_bars", ROTATE_90);
	public static final TextureSet DECAL_FAT_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "fat_diagonal_bars_90", "fat_diagonal_bars", ROTATE_90);
	public static final TextureSet DECAL_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "diagonal_ridges_90", "diagonal_ridges", ROTATE_90);
	public static final TextureSet DECAL_SKINNY_BARS_90 = addDecal(Xm.MODID, "skinny_bars_90", "skinny_bars", ROTATE_90);
	public static final TextureSet DECAL_FAT_BARS_90 = addDecal(Xm.MODID, "fat_bars_90", "fat_bars", ROTATE_90);
	public static final TextureSet DECAL_THICK_BARS_90 = addDecal(Xm.MODID, "thick_bars_90", "thick_bars", ROTATE_90);
	public static final TextureSet DECAL_THIN_BARS_90 = addDecal(Xm.MODID, "thin_bars_90", "thin_bars", ROTATE_90);
	public static final TextureSet DECAL_SKINNY_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "skinny_diagonal_ridges_random", "skinny_diagonal_ridges", ROTATE_RANDOM);
	public static final TextureSet DECAL_THICK_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "thick_diagonal_ridges_random", "thick_diagonal_ridges", ROTATE_RANDOM);
	public static final TextureSet DECAL_THIN_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "thin_diagonal_ridges_random", "thin_diagonal_ridges", ROTATE_RANDOM);
	public static final TextureSet DECAL_THIN_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "thin_diagonal_bars_random", "thin_diagonal_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_SKINNY_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "skinny_diagonal_bars_random", "skinny_diagonal_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "diagonal_bars_random", "diagonal_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_FAT_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "fat_diagonal_bars_random", "fat_diagonal_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "diagonal_ridges_random", "diagonal_ridges", ROTATE_RANDOM);
	public static final TextureSet DECAL_SKINNY_BARS_RANDOM = addDecal(Xm.MODID, "skinny_bars_random", "skinny_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_FAT_BARS_RANDOM = addDecal(Xm.MODID, "fat_bars_random", "fat_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_THICK_BARS_RANDOM = addDecal(Xm.MODID, "thick_bars_random", "thick_bars", ROTATE_RANDOM);
	public static final TextureSet DECAL_THIN_BARS_RANDOM = addDecal(Xm.MODID, "thin_bars_random", "thin_bars", ROTATE_RANDOM);

	public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_RIDGES = addDecal(Xm.MODID, "skinny_diagonal_ridges_seamless", "skinny_diagonal_ridges_seamless",
			IDENTITY);
	public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_CROSS_RIDGES = addDecal(Xm.MODID, "thick_diagonal_cross_ridges_seamless",
			"thick_diagonal_cross_ridges_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_RIDGES = addDecal(Xm.MODID, "thick_diagonal_ridges_seamless", "thick_diagonal_ridges_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_CROSS_RIDGES = addDecal(Xm.MODID, "thin_diagonal_cross_ridges_seamless",
			"thin_diagonal_cross_ridges_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_RIDGES = addDecal(Xm.MODID, "thin_diagonal_ridges_seamless", "thin_diagonal_ridges_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_CROSS_BARS = addDecal(Xm.MODID, "thin_diagonal_cross_bars_seamless", "thin_diagonal_cross_bars_seamless",
			IDENTITY);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_BARS = addDecal(Xm.MODID, "thin_diagonal_bars_seamless", "thin_diagonal_bars_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_SKINNY_DIAGNAL_CROSS_BARS = addDecal(Xm.MODID, "skinny_diagonal_cross_bars_seamless", "skinny_diagonal_cross_bars_seamless",
			IDENTITY);
	public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_BARS = addDecal(Xm.MODID, "skinny_diagonal_bars_seamless", "skinny_diagonal_bars_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_DIAGONAL_CROSS_BARS = addDecal(Xm.MODID, "diagonal_cross_bars_seamless", "diagonal_cross_bars_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_DIAGONAL_BARS = addDecal(Xm.MODID, "diagonal_bars_seamless", "diagonal_bars_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_CROSS_BARS = addDecal(Xm.MODID, "fat_diagonal_cross_bars_seamless", "fat_diagonal_cross_bars_seamless",
			IDENTITY);
	public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_BARS = addDecal(Xm.MODID, "fat_diagonal_bars_seamless", "fat_diagonal_bars_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_DIAGONAL_CROSS_RIDGES = addDecal(Xm.MODID, "diagonal_cross_ridges_seamless", "diagonal_cross_ridges_seamless", IDENTITY);
	public static final TextureSet DECAL_SOFT_DIAGONAL_RIDGES = addDecal(Xm.MODID, "diagonal_ridges_seamless", "diagonal_ridges_seamless", IDENTITY);

	public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "skinny_diagonal_ridges_90", "skinny_diagonal_ridges", ROTATE_90);
	public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "thick_diagonal_ridges_seamless_90", "thick_diagonal_ridges_seamless",
			ROTATE_90);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "thin_diagonal_ridges_seamless_90", "thin_diagonal_ridges_seamless",
			ROTATE_90);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "thin_diagonal_bars_seamless_90", "thin_diagonal_bars_seamless", ROTATE_90);
	public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "skinny_diagonal_bars_seamless_90", "skinny_diagonal_bars_seamless",
			ROTATE_90);
	public static final TextureSet DECAL_SOFT_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "diagonal_bars_seamless_90", "diagonal_bars_seamless", ROTATE_90);
	public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_BARS_90 = addDecal(Xm.MODID, "fat_diagonal_bars_seamless_90", "fat_diagonal_bars_seamless", ROTATE_90);
	public static final TextureSet DECAL_SOFT_DIAGONAL_RIDGES_90 = addDecal(Xm.MODID, "diagonal_ridges_seamless_90", "diagonal_ridges_seamless", ROTATE_90);

	public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "skinny_diagonal_ridges_random", "skinny_diagonal_ridges",
			ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_THICK_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "thick_diagonal_ridges_seamless_random", "thick_diagonal_ridges_seamless",
			ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "thin_diagonal_ridges_seamless_random", "thin_diagonal_ridges_seamless",
			ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_THIN_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "thin_diagonal_bars_seamless_random", "thin_diagonal_bars_seamless",
			ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_SKINNY_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "skinny_diagonal_bars_seamless_random", "skinny_diagonal_bars_seamless",
			ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "diagonal_bars_seamless_random", "diagonal_bars_seamless", ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_FAT_DIAGONAL_BARS_RANDOM = addDecal(Xm.MODID, "fat_diagonal_bars_seamless_random", "fat_diagonal_bars_seamless",
			ROTATE_RANDOM);
	public static final TextureSet DECAL_SOFT_DIAGONAL_RIDGES_RANDOM = addDecal(Xm.MODID, "diagonal_ridges_seamless_random", "diagonal_ridges_seamless", ROTATE_RANDOM);

	public static final TextureSet TILE_DOTS = TextureSet.builder().displayNameToken("dots").baseTextureName("exotic-matter:block/dots").versionCount(4)
			.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(ROTATE_RANDOM).renderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:dots");

	public static final TextureSet TILE_DOTS_SUBTLE = TextureSet.builder().displayNameToken("dots_subtle").versionCount(4).scale(TextureScale.SINGLE)
			.layout(TextureLayoutMap.VERSION_X_8).baseTextureName("exotic-matter:block/dots_subtle").transform(ROTATE_RANDOM)
			.renderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT).groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS)
			.build("exotic-matter:dots_subtle");

	public static final TextureSet TILE_DOTS_INVERSE = TextureSet.builder(TILE_DOTS_SUBTLE).displayNameToken("dots_inverse")
			.baseTextureName("exotic-matter:block/dots_inverse").build("exotic-matter:dots_inverse");

	public static final TextureSet TILE_DOTS_INVERSE_SUBTLE = TextureSet.builder(TILE_DOTS_SUBTLE).displayNameToken("dots_inverse_subtle")
			.baseTextureName("exotic-matter:block/dots_inverse_subtle").build("exotic-matter:dots_inverse_subtle");

}
