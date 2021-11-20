/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.texture;

import static grondag.xm.api.texture.TextureGroup.ALWAYS_HIDDEN;
import static grondag.xm.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm.api.texture.TextureGroup.STATIC_TILES;
import static grondag.xm.api.texture.TextureLayoutMap.QUADRANT_ROTATED_SINGLE;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_CUTOUT_OKAY;
import static grondag.xm.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;

import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public final class XmTextures {
	private XmTextures() { }

	public static final TextureSet TILE_NOISE_STRONG = TextureSet.builder()
			.displayNameToken("noise_strong").baseTextureName("exotic-matter:block/noise_strong")
			.versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(ROTATE_RANDOM)
			.renderIntent(BASE_ONLY).groups(STATIC_TILES).build("exotic-matter:noise_strong");

	public static final TextureSet TILE_NOISE_MODERATE = TextureSet.builder(TILE_NOISE_STRONG).displayNameToken("noise_moderate")
			.baseTextureName("exotic-matter:block/noise_moderate").build("exotic-matter:noise_moderate");

	public static final TextureSet TILE_NOISE_LIGHT = TextureSet.builder(TILE_NOISE_STRONG).displayNameToken("noise_light")
			.baseTextureName("exotic-matter:block/noise_light").build("exotic-matter:noise_light");

	public static final TextureSet TILE_NOISE_SUBTLE = TextureSet.builder(TILE_NOISE_STRONG).displayNameToken("noise_subtle")
			.baseTextureName("exotic-matter:block/noise_subtle").build("exotic-matter:noise_subtle");

	public static final TextureSet TILE_NOISE_EXTREME = TextureSet.builder()
			.displayNameToken("noise_extreme").baseTextureName("exotic-matter:block/noise_extreme")
			.versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(ROTATE_RANDOM)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_TILES).build("exotic-matter:noise_extreme");

	public static final TextureSet WHITE = TextureSet.builder().displayNameToken("white").baseTextureName("exotic-matter:block/white").versionCount(1).scale(SINGLE)
			.layout(TextureLayoutMap.VERSION_X_8).transform(IDENTITY).groups(STATIC_TILES).build("exotic-matter:white");

	/** Used as filler in mixed quadrants. */
	public static final TextureSet EMPTY = TextureSet.builder().displayNameToken("empty").baseTextureName("exotic-matter:block/empty").versionCount(1).scale(SINGLE)
			.layout(TextureLayoutMap.SINGLE).transform(IDENTITY).groups(ALWAYS_HIDDEN).build("exotic-matter:empty");

	public static final TextureSet BORDER_SINGLE_LINE = TextureSet.builder().displayNameToken("border_single_line")
			.baseTextureName("exotic-matter:block/border_single_line").versionCount(1).scale(SINGLE).layout(QUADRANT_ROTATED_SINGLE).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_single_line");

	public static final TextureSet TILE_NOISE_BLUE_A = TextureSet.builder().displayNameToken("blue_noise_a")
			.baseTextureName("exotic-matter:block/noise_blue_0").versionCount(4)
			.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSIONED)
			.transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_a");

	public static final TextureSet TILE_NOISE_BLUE_B = TextureSet.builder().displayNameToken("blue_noise_b")
			.baseTextureName("exotic-matter:block/noise_blue_1").versionCount(4)
			.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSIONED)
			.transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_b");

	public static final TextureLayoutMap NOISE_LAYOUT = TextureLayoutMap.create(TextureLayout.SIMPLE, (s, v, i) -> s + (v < 4 ? "_0_" + v : "_1_" + (v - 4)));

	public static final TextureSet TILE_NOISE_BLUE = TextureSet.builder().displayNameToken("blue_noise")
			.baseTextureName("exotic-matter:block/noise_blue").versionCount(8)
			.scale(TextureScale.SINGLE).layout(NOISE_LAYOUT)
			.transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY)
			.groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-matter:blue_noise_b");
}
