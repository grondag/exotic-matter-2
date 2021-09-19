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
package grondag.xm.dispatch;

import org.jetbrains.annotations.ApiStatus.Internal;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import grondag.xm.texture.TextureSetHelper;

@Internal
public abstract class AbstractXmModel implements BakedModel, FabricBakedModel {
	@Override
	public ItemOverrides getOverrides() {
		return ItemOverrideProxy.INSTANCE;
	}

	@Override
	public ItemTransforms getTransforms() {
		return ModelHelper.MODEL_TRANSFORM_BLOCK;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean usesBlockLight() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return TextureSetHelper.missingSprite();
	}
}
