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

package grondag.xm2.model;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelItemPropertyOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.Sprite;

public abstract class AbstractXmModel implements BakedModel, FabricBakedModel {
    protected ItemProxy itemProxy = null;

    @Override
    public ModelItemPropertyOverrideList getItemPropertyOverrides() {
	ItemProxy result = itemProxy;
	if (result == null) {
	    result = new ItemProxy(this);
	    itemProxy = result;
	}
	return result;
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
    public boolean hasDepthInGui() {
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
	return MissingSprite.getMissingSprite();
    }

}
