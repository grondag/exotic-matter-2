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
package grondag.xm.api.texture;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.util.Identifier;

import grondag.xm.Xm;
import grondag.xm.texture.TextureSetRegistryImpl;

@Experimental
public interface TextureSetRegistry {
	static TextureSetRegistry instance() {
		return TextureSetRegistryImpl.INSTANCE;
	}

	/**
	 * Will always be associated with index 0.
	 */
	Identifier NONE_ID = new Identifier(Xm.MODID, "none");

	TextureSet get(Identifier id);

	TextureSet get(int index);

	void forEach(Consumer<TextureSet> consumer);
}
