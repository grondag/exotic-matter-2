/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.texture;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetBuilder;
import grondag.xm.api.texture.TextureTransform;

@Internal
public class TextureSetBuilderImpl extends AbstractTextureSet implements TextureSetBuilder {
	@Override
	public TextureSetBuilder versionCount(int versionCount) {
		this.versionCount = versionCount;
		return this;
	}

	@Override
	public TextureSetBuilder scale(TextureScale scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public TextureSetBuilder layout(TextureLayoutMap layout) {
		layoutMap = (TextureLayoutMapImpl) layout;
		return this;
	}

	@Override
	public TextureSetBuilder transform(TextureTransform rotation) {
		transform = rotation;
		return this;
	}

	@Override
	public TextureSetBuilder renderIntent(TextureRenderIntent renderIntent) {
		this.renderIntent = renderIntent;
		return this;
	}

	@Override
	public TextureSetBuilder groups(TextureGroup... groups) {
		textureGroupFlags = TextureGroup.makeTextureGroupFlags(groups);
		return this;
	}

	@Override
	public TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile) {
		this.renderNoBorderAsTile = renderNoBorderAsTile;
		return this;
	}

	@Override
	public TextureSetBuilder baseTextureName(String baseTextureName) {
		rawBaseTextureName = baseTextureName;
		return this;
	}

	@Override
	public TextureSetBuilder displayNameToken(String displayNameToken) {
		this.displayNameToken = displayNameToken;
		return this;
	}

	@Override
	public TextureSet build(ResourceLocation id) {
		TextureSetImpl result;

		if (TextureSetRegistryImpl.INSTANCE.contains(id)) {
			result = TextureSetRegistryImpl.INSTANCE.get(id);
		} else {
			result = new TextureSetImpl(id, this);
		}

		return result;
	}
}
