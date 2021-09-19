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
package grondag.xm.paint;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus.Internal;

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
