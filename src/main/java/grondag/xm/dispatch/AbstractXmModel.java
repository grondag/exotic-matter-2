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

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;

import grondag.xm.texture.TextureSetHelper;

@API(status = INTERNAL)
public abstract class AbstractXmModel implements BakedModel, FabricBakedModel {
	@Override
	public ModelOverrideList getOverrides() {
		return ItemOverrideProxy.INSTANCE;
	}

	@Override
	public ModelTransformation getTransformation() {
		return ModelHelper.MODEL_TRANSFORM_BLOCK;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isSideLit() {
		return true;
	}

	@Override
	public boolean hasDepth() {
		return true;
	}

	@Override
	public boolean isBuiltin() {
		return false;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public Sprite getSprite() {
		return TextureSetHelper.missingSprite();
	}
}
