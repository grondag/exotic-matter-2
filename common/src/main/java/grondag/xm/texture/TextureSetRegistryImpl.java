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

import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetRegistry;
import grondag.xm.api.texture.TextureTransform;

public class TextureSetRegistryImpl implements TextureSetRegistry {
	public static final TextureSetImpl DEFAULT_TEXTURE_SET;

	public static final TextureSetRegistryImpl INSTANCE = new TextureSetRegistryImpl();

	static {
		DEFAULT_TEXTURE_SET = (TextureSetImpl) TextureSet.builder().displayNameToken("none").baseTextureName("exotic-matter:block/noise_moderate").versionCount(4)
				.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(TextureTransform.ROTATE_RANDOM)
				.renderIntent(TextureRenderIntent.BASE_ONLY).groups(TextureGroup.ALWAYS_HIDDEN).build(TextureSetRegistry.NONE_ID);

		DEFAULT_TEXTURE_SET.use();
	}

	public static TextureSet noTexture() {
		return INSTANCE.get(TextureSetRegistry.NONE_ID);
	}

	private final Object2ObjectOpenHashMap<ResourceLocation, TextureSetImpl> MAP = new Object2ObjectOpenHashMap<>();

	@Override
	public TextureSetImpl get(ResourceLocation id) {
		return MAP.getOrDefault(id, DEFAULT_TEXTURE_SET);
	}

	@Override
	public void forEach(Consumer<TextureSet> consumer) {
		MAP.values().forEach(consumer);
	}

	public boolean contains(ResourceLocation id) {
		return MAP.containsKey(id);
	}

	void add(TextureSetImpl set) {
		MAP.put(set.id, set);
	}
}
