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

import java.util.function.Consumer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Lifecycle;
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

	static {
		REGISTRY = (WritableRegistry<TextureSetImpl>) ((WritableRegistry) Registry.REGISTRY).register(REGISTRY_KEY,
				new DefaultedRegistry(NONE_ID.toString(), REGISTRY_KEY, Lifecycle.stable()), Lifecycle.stable());

		DEFAULT_TEXTURE_SET = (TextureSetImpl) TextureSet.builder().displayNameToken("none").baseTextureName("exotic-matter:block/noise_moderate").versionCount(4)
				.scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSION_X_8).transform(TextureTransform.ROTATE_RANDOM)
				.renderIntent(TextureRenderIntent.BASE_ONLY).groups(TextureGroup.ALWAYS_HIDDEN).build(TextureSetRegistry.NONE_ID);

		DEFAULT_TEXTURE_SET.use();
	}

	void add(TextureSetImpl set) {
		Registry.register(REGISTRY, set.id, set);
	}
}
