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

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.resources.ResourceLocation;

// TODO: add fallback texture specification
@Experimental
public interface TextureSetBuilder {
	TextureSetBuilder versionCount(int versionCount);

	TextureSetBuilder scale(TextureScale scale);

	TextureSetBuilder layout(TextureLayoutMap layout);

	TextureSetBuilder transform(TextureTransform rotation);

	TextureSetBuilder renderIntent(TextureRenderIntent renderIntent);

	TextureSetBuilder groups(TextureGroup... groups);

	TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile);

	/**
	 * Include namespace!
	 */
	TextureSetBuilder baseTextureName(String baseTextureName);

	TextureSetBuilder displayNameToken(String displayNameToken);

	TextureSet build(ResourceLocation id);

	default TextureSet build(String nameSpace, String path) {
		return build(new ResourceLocation(nameSpace, path));
	}

	default TextureSet build(String idString) {
		return build(new ResourceLocation(idString));
	}
}
