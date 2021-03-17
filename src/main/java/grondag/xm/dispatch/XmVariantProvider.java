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

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;

import grondag.xm.api.block.XmBlockState;

@Internal
public class XmVariantProvider implements ModelVariantProvider {
	private final ObjectOpenHashSet<String> targets = new ObjectOpenHashSet<>();

	public XmVariantProvider() {
		targets.clear();

		Registry.BLOCK.forEach(b -> {
			if (XmBlockState.get(b) != null) {
				targets.add(Registry.BLOCK.getId(b).toString());
			}
		});

		Registry.ITEM.forEach(i -> {
			if (((XmItemAccess) i).xm_modelStateFunc() != null) {
				targets.add(Registry.ITEM.getId(i).toString());
			}
		});
	}

	@Override
	public UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
		return targets.contains(modelId.getNamespace() + ":" + modelId.getPath()) ? XmModelProxy.INSTANCE : null;
	}
}
