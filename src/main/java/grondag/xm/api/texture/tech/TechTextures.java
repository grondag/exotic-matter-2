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
package grondag.xm.api.texture.tech;

import static grondag.xm.api.texture.TextureGroup.STATIC_DETAILS;
import static grondag.xm.api.texture.TextureRenderIntent.BASE_OR_OVERLAY_CUTOUT_OKAY;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.DIAGONAL;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureSet;

@Internal
public enum TechTextures {
	;

	public static final TextureSet DECAL_PLUS = TextureSet.builder().displayNameToken("decal_plus")
			.baseTextureName("exotic-matter:block/plus").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:decal_plus");

	public static final TextureSet DECAL_MINUS = TextureSet.builder().displayNameToken("decal_minus")
			.baseTextureName("exotic-matter:block/minus").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:decal_minus");

	public static final TextureSet DECAL_INPUT = TextureSet.builder().displayNameToken("decal_input")
			.baseTextureName("exotic-matter:block/input").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:decal_input");

	public static final TextureSet DECAL_OUTPUT = TextureSet.builder().displayNameToken("decal_output")
			.baseTextureName("exotic-matter:block/output").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:decal_output");

	public static final TextureSet CABLE_INPUT_DECAL = TextureSet.builder().displayNameToken("cable_input_decal")
			.baseTextureName("exotic-matter:block/cable_input_decal").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:cable_input_decal");

	public static final TextureSet CABLE_OUTPUT_DECAL = TextureSet.builder().displayNameToken("cable_output_decal")
			.baseTextureName("exotic-matter:block/cable_output_decal").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:cable_output_decal");

	public static final TextureSet CABLE_INPUT_ARROWS = TextureSet.builder().displayNameToken("cable_input_arrows")
			.baseTextureName("exotic-matter:block/cable_input_arrows").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(DIAGONAL)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:cable_input_arrows");

	public static final TextureSet CABLE_OUTPUT_ARROWS = TextureSet.builder().displayNameToken("cable_output_arrows")
			.baseTextureName("exotic-matter:block/cable_output_arrows").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.SINGLE).transform(DIAGONAL)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:cable_output_arrows");

	public static final TextureSet CABLE_CENTER_4PX = TextureSet.builder().displayNameToken("cable_center_4px")
			.baseTextureName("exotic-matter:block/cable_center_4px").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.QUADRANT_ROTATED_CABLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:cable_center_4px");

	public static final TextureSet CABLE_GLOWLINES_4PX = TextureSet.builder().displayNameToken("cable_glowlines_4px")
			.baseTextureName("exotic-matter:block/cable_glowlines_4px").versionCount(1)
			.scale(SINGLE).layout(TextureLayoutMap.QUADRANT_ROTATED_CABLE).transform(IDENTITY)
			.renderIntent(BASE_OR_OVERLAY_CUTOUT_OKAY).groups(STATIC_DETAILS).build("exotic-matter:cable_glowlines_4px");
}

