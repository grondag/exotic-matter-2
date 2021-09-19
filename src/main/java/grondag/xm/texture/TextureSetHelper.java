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
package grondag.xm.texture;

import static grondag.xm.api.texture.TextureGroup.STATIC_BORDERS;
import static grondag.xm.api.texture.TextureLayoutMap.BORDER_13;
import static grondag.xm.api.texture.TextureLayoutMap.BORDER_14;
import static grondag.xm.api.texture.TextureRenderIntent.OVERLAY_ONLY;
import static grondag.xm.api.texture.TextureScale.SINGLE;
import static grondag.xm.api.texture.TextureTransform.IDENTITY;

import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Internal
public enum TextureSetHelper {
	;

	public static TextureAtlas blockAtas() {
		return Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
	}

	public static TextureAtlasSprite missingSprite() {
		return blockAtas().getSprite(MissingTextureAtlasSprite.getLocation());
	}

	TextureSetHelper() {}

	static final TextureGroup[] BORDERS_STATIC = new TextureGroup[] { TextureGroup.STATIC_BORDERS };
	static final TextureGroup[] DUAL_STATIC =  new TextureGroup[] { TextureGroup.STATIC_TILES, TextureGroup.STATIC_BORDERS };

	public static TextureSet addBorderSingle(String modId, String name) {
		return TextureSet.builder().displayNameToken(name).baseTextureName(modId + ":block/" + name).versionCount(1).scale(SINGLE).layout(BORDER_13)
		.transform(IDENTITY).renderIntent(OVERLAY_ONLY).groups(STATIC_BORDERS).build(modId + ":" + name);
	}

	public static TextureSet addBorderRandom(String modId, String name, boolean allowTile, boolean renderNoBorderAsTile) {
		return TextureSet.builder().displayNameToken(name).baseTextureName(modId + ":block/" + name).versionCount(4).scale(SINGLE)
		.layout(renderNoBorderAsTile ? BORDER_14 : BORDER_13)
		.renderIntent(allowTile ? TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT : TextureRenderIntent.OVERLAY_ONLY)
		.groups(allowTile ? BORDERS_STATIC : DUAL_STATIC).transform(IDENTITY).build(modId + ":" + name);
	}

	public static TextureSet addBigTex(String modId, String name) {
		return addBigTex(modId, name, TextureScale.GIANT);
	}

	public static TextureSet addBigTex(String modId, String name, TextureScale scale) {
		return addBigTex(modId, name, scale, TextureTransform.ROTATE_BIGTEX);
	}

	public static TextureSet addBigTex(String modId, String name, TextureScale scale, TextureTransform transform) {
		return TextureSet.builder().displayNameToken(name).baseTextureName(modId + ":block/" + name).versionCount(1).scale(scale)
		.layout(TextureLayoutMap.SINGLE).transform(transform).renderIntent(TextureRenderIntent.BASE_ONLY).groups(TextureGroup.STATIC_TILES)
		.build(modId + ":" + name);
	}

	public static TextureSet addDecal(String modId, String idName, String fileName, TextureTransform rotation) {
		return TextureSet.builder().displayNameToken(idName).baseTextureName(modId + ":block/" + fileName).versionCount(1).scale(TextureScale.SINGLE)
		.layout(TextureLayoutMap.SINGLE).transform(rotation).renderIntent(TextureRenderIntent.OVERLAY_ONLY).groups(TextureGroup.STATIC_DETAILS)
		.build(modId + ":" + idName);
	}
}
