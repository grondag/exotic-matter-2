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

package grondag.xm.texture;

import org.jetbrains.annotations.ApiStatus.Internal;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureTransform;

@Internal
abstract class AbstractTextureSet {
	TextureLayoutMapImpl layoutMap = (TextureLayoutMapImpl) TextureLayoutMap.SINGLE;
	TextureTransform transform = TextureTransform.IDENTITY;
	TextureScale scale = TextureScale.SINGLE;
	TextureRenderIntent renderIntent = TextureRenderIntent.BASE_ONLY;
	int textureGroupFlags = TextureGroup.ALWAYS_HIDDEN.bitFlag;
	int versionCount = 1;
	boolean renderNoBorderAsTile = false;
	String rawBaseTextureName;
	String displayNameToken;

	protected void copyFrom(AbstractTextureSet template) {
		layoutMap = template.layoutMap;
		transform = template.transform;
		scale = template.scale;
		renderIntent = template.renderIntent;
		versionCount = template.versionCount;
		rawBaseTextureName = template.rawBaseTextureName;
		renderNoBorderAsTile = template.renderNoBorderAsTile;
		displayNameToken = template.displayNameToken;
		textureGroupFlags = template.textureGroupFlags;
	}

	public TextureLayoutMap map() {
		return layoutMap;
	}

	public TextureTransform transform() {
		return transform;
	}

	public TextureScale scale() {
		return scale;
	}

	public TextureRenderIntent renderIntent() {
		return renderIntent;
	}

	public int versionCount() {
		return versionCount;
	}

	public boolean renderNoBorderAsTile() {
		return renderNoBorderAsTile;
	}

	public String displayNameToken() {
		return displayNameToken;
	}

	public int textureGroupFlags() {
		return textureGroupFlags;
	}
}
