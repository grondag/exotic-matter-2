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
package grondag.xm.api.texture.core;

import static grondag.xm.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm.api.texture.TextureGroup.STATIC_TILES;
import static grondag.xm.api.texture.TextureLayoutMap.QUADRANT_ORIENTED_BORDER_SINGLE;
import static grondag.xm.api.texture.TextureLayoutMap.QUADRANT_ROTATED_SINGLE;
import static grondag.xm.api.texture.TextureLayoutMap.QUADRANT_ROTATED_VERSIONED;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_ONLY;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT;
import static grondag.xm.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm.api.texture.TextureScale.GIANT;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;
import static grondag.xm.api.texture.TextureTransform.ROTATE_RANDOM;
import static grondag.xm.texture.TextureSetHelper.addBigTex;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.Xm;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureTransform;

@Experimental
public enum CoreTextures {
	;

	public static final TextureSet TILE_COBBLE = TextureSet.builder()
			.displayNameToken("cobble").baseTextureName("exotic-matter:block/cobble")
			.versionCount(4).scale(SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(ROTATE_RANDOM)
			.renderIntent(BASE_ONLY).groups(STATIC_TILES).build("exotic-matter:cobble");

	public static final TextureSet BORDER_COBBLE = TextureSet.builder().displayNameToken("border_cobble")
			.baseTextureName("exotic-matter:block/border_cobble").versionCount(4).scale(SINGLE).layout(QUADRANT_ROTATED_VERSIONED).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_cobble");

	public static final TextureSet BORDER_SMOOTH_BLEND = TextureSet.builder().displayNameToken("border_smooth_blended")
			.baseTextureName("exotic-matter:block/border_smooth_blended").versionCount(1).scale(SINGLE).layout(QUADRANT_ROTATED_SINGLE).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_smooth_blended");

	public static final TextureSet BORDER_WEATHERED_BLEND = TextureSet.builder().displayNameToken("border_weathered_blend")
			.baseTextureName("exotic-matter:block/border_weathered_blended").versionCount(4).scale(SINGLE).layout(QUADRANT_ROTATED_VERSIONED).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_weathered_blend");

	public static final TextureSet BORDER_BEVEL = TextureSet.builder().displayNameToken("bevel")
			.baseTextureName("exotic-matter:block/border_bevel").versionCount(1).scale(SINGLE).layout(QUADRANT_ORIENTED_BORDER_SINGLE).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:bevel");

	public static final TextureSet BORDER_WEATHERED_LINE = TextureSet.builder().displayNameToken("border_weathered_line")
			.baseTextureName("exotic-matter:block/border_weathered_line").versionCount(4).scale(SINGLE).layout(QUADRANT_ROTATED_VERSIONED).transform(IDENTITY)
			.renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build("exotic-matter:border_weathered_line");

	public static final TextureSet BIGTEX_SANDSTONE = TextureSet.builder().displayNameToken("sandstone").baseTextureName("exotic-matter:block/sandstone").versionCount(1)
			.scale(GIANT).layout(TextureLayoutMap.SINGLE).transform(ROTATE_RANDOM).renderIntent(BASE_OR_OVERLAY_NO_CUTOUT)
			.groups(STATIC_TILES).build("exotic-matter:sandstone");

	public static final TextureSet BIGTEX_RAMMED_EARTH = addBigTex(Xm.MODID, "rammed_earth", TextureScale.LARGE, TextureTransform.IDENTITY);
	public static final TextureSet BIGTEX_COBBLE_SQUARES = addBigTex(Xm.MODID, "cobble_squares", TextureScale.LARGE, TextureTransform.IDENTITY);
	public static final TextureSet BIGTEX_GRANITE = addBigTex(Xm.MODID, "granite", TextureScale.LARGE);
	public static final TextureSet BIGTEX_SNOW = addBigTex(Xm.MODID, "snow");
}
