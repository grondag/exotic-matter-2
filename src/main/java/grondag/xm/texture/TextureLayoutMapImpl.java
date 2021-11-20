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

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.texture.TextureLayout;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureNameFunction;
import grondag.xm.api.texture.TextureSet;

@Internal
public class TextureLayoutMapImpl implements TextureLayoutMap {
	public static TextureLayoutMapImpl create(TextureLayout layout, TextureNameFunction nameFunc) {
		return new TextureLayoutMapImpl(layout, nameFunc);
	}

	public final TextureLayout layout;

	public final TextureNameFunction nameFunc;

	private TextureLayoutMapImpl(TextureLayout layout, TextureNameFunction nameFunc) {
		this.layout = layout;
		this.nameFunc = nameFunc;
	}

	@Override
	public TextureLayout layout() {
		return layout;
	}

	public final void prestitch(TextureSet texture, Consumer<ResourceLocation> stitcher) {
		for (int i = 0; i < texture.versionCount(); i++) {
			for (int j = 0; j < layout.textureCount; j++) {
				stitcher.accept(new ResourceLocation(nameFunc.apply(texture.baseTextureName(), i, j)));
			}
		}
	}

	public final String buildTextureName(TextureSet texture, int version, int index) {
		return nameFunc.apply(texture.baseTextureName(), version, index);
	}

	public final String sampleTextureName(TextureSet texture) {
		return nameFunc.apply(texture.baseTextureName(), 0, 0);
	}
}
