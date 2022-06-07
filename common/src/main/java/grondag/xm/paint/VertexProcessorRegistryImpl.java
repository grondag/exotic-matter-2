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

package grondag.xm.paint;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.resources.ResourceLocation;

import grondag.xm.Xm;
import grondag.xm.api.paint.VertexProcessor;
import grondag.xm.api.paint.VertexProcessorRegistry;

@Internal
public class VertexProcessorRegistryImpl implements VertexProcessorRegistry {
	public static final VertexProcessorRegistry INSTANCE = new VertexProcessorRegistryImpl();

	private final Object2ObjectOpenHashMap<ResourceLocation, VertexProcessor> keyToValueMap = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectOpenHashMap<VertexProcessor, ResourceLocation> valueToKeyMap = new Object2ObjectOpenHashMap<>();

	static {
		INSTANCE.add(NONE_ID, VertexProcessorDefault.INSTANCE);
	}

	@Override
	public VertexProcessor add(ResourceLocation id, VertexProcessor processor) {
		if (keyToValueMap.containsKey(id)) {
			Xm.LOG.warn("Duplicate registration for VertexProcessor ID " + id.toString());
		}

		keyToValueMap.put(id, processor);
		valueToKeyMap.put(processor, id);
		return processor;
	}

	@Override
	public VertexProcessor get(ResourceLocation id) {
		return keyToValueMap.getOrDefault(id, VertexProcessorDefault.INSTANCE);
	}

	@Override
	public ResourceLocation getKey(VertexProcessor value) {
		return valueToKeyMap.getOrDefault(value, NONE_ID);
	}

	@Override
	public boolean containsKey(ResourceLocation id) {
		return keyToValueMap.containsKey(id);
	}
}
