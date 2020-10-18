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

import java.util.Collections;

import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.MutableModelState;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Internal
public class ItemOverrideProxy extends ModelOverrideList {
	static final ItemOverrideProxy INSTANCE = new ItemOverrideProxy();

	private ItemOverrideProxy() {
		super(null, null, null, Collections.emptyList());
	}

	@Override
	public BakedModel apply(BakedModel bakedModel_1, ItemStack stack, @Nullable ClientWorld world, LivingEntity livingEntity_1) {
		final MutableModelState modelState = XmItem.modelState(world, stack);

		if (modelState != null) {
			final BakedModel result = XmDispatcher.INSTANCE.get(modelState).itemProxy();
			modelState.release();
			return result;
		} else {
			return  MinecraftClient.getInstance().getBakedModelManager().getMissingModel();
		}
	}
}
