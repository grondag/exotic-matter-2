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

package grondag.xm.paint;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.paint.VertexProcessor;
import grondag.xm.api.paint.VertexProcessorRegistry;

@Internal
@SuppressWarnings({ "unchecked", "rawtypes" })
public class VertexProcessorRegistryImpl extends DefaultedRegistry<VertexProcessor> implements VertexProcessorRegistry {
	public static final VertexProcessor DEFAULT_VERTEX_PROCESSOR = VertexProcessorDefault.INSTANCE;

	private static final ResourceKey REGISTRY_KEY = ResourceKey.createRegistryKey(Xm.id("vertex_proc"));
	public static final VertexProcessorRegistryImpl INSTANCE;

	static {
		INSTANCE = (VertexProcessorRegistryImpl) ((WritableRegistry) Registry.REGISTRY).register(REGISTRY_KEY,
				new VertexProcessorRegistryImpl(NONE_ID.toString()), Lifecycle.stable());
	}

	VertexProcessorRegistryImpl(String defaultIdString) {
		super(defaultIdString, REGISTRY_KEY, Lifecycle.experimental());
	}

	@Override
	public VertexProcessor add(ResourceLocation id, VertexProcessor processor) {
		return Registry.register(this, id, processor);
	}

	@Override
	public VertexProcessor get(int index) {
		return byId(index);
	}
}
