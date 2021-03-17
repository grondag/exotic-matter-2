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
package grondag.xm.api.paint;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.Identifier;

import grondag.xm.Xm;
import grondag.xm.paint.VertexProcessorRegistryImpl;

@Experimental
public interface VertexProcessorRegistry {
	/**
	 * Will always be associated with index 0.
	 */
	Identifier NONE_ID = new Identifier(Xm.MODID, "none");

	VertexProcessorRegistry INSTANCE = VertexProcessorRegistryImpl.INSTANCE;

	VertexProcessor get(Identifier id);

	VertexProcessor get(int index);

	VertexProcessor add(Identifier id, VertexProcessor set);

	Identifier getId(VertexProcessor value);

	boolean containsId(Identifier id);
}
