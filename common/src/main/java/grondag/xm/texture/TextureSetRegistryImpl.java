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

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetRegistry;
import grondag.xm.api.texture.TextureTransform;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TextureSetRegistryImpl implements TextureSetRegistry {
	public static final TextureSetImpl DEFAULT_TEXTURE_SET;

	private static final ResourceKey REGISTRY_KEY = ResourceKey.createRegistryKey(Xm.id("texture_sets"));
	private static final WritableRegistry<TextureSetImpl> REGISTRY;

	public static final TextureSetRegistryImpl INSTANCE = new TextureSetRegistryImpl();

	static {
		REGISTRY = (WritableRegistry<TextureSetImpl>) ((WritableRegistry) Registry.REGISTRY).register(REGISTRY_KEY,
				new DefaultedRegistry(NONE_ID.toString(), REGISTRY_KEY, Lifecycle.stable(), null), Lifecycle.stable()).value();

		DEFAULT_TEXTURE_SET = (TextureSetImpl) TextureSet.builder().displayNameToken("none").baseTextureName("exotic-matter:block/noise_moderate").versionCount(4)
				.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(TextureTransform.ROTATE_RANDOM)
				.renderIntent(TextureRenderIntent.BASE_ONLY).groups(TextureGroup.ALWAYS_HIDDEN).build(TextureSetRegistry.NONE_ID);

		DEFAULT_TEXTURE_SET.use();
	}

	public static TextureSet noTexture() {
		return INSTANCE.get(0);
	}

	@Override
	public TextureSetImpl get(ResourceLocation id) {
		return REGISTRY.get(id);
	}

	@Override
	public TextureSetImpl get(int index) {
		return index < 0 ? DEFAULT_TEXTURE_SET : REGISTRY.byId(index);
	}

	public int indexOf(TextureSetImpl set) {
		return REGISTRY.getId(set);
	}

	@Override
	public void forEach(Consumer<TextureSet> consumer) {
		REGISTRY.forEach(consumer);
	}

	public boolean contains(ResourceLocation id) {
		return REGISTRY != null && REGISTRY.keySet().contains(id);
	}

	void add(TextureSetImpl set) {
		Registry.register(REGISTRY, set.id, set);
	}
}
