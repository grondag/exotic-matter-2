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

package grondag.xm.dispatch;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import io.vram.frex.api.model.provider.ModelProvider;
import io.vram.frex.api.model.provider.SubModelLoader;

import grondag.xm.api.block.XmBlockState;

@Internal
public class XmVariantProvider implements ModelProvider<ModelResourceLocation> {
	private final ObjectOpenHashSet<String> targets = new ObjectOpenHashSet<>();

	public XmVariantProvider() {
		targets.clear();

		BuiltInRegistries.BLOCK.forEach(b -> {
			if (XmBlockState.get(b) != null) {
				targets.add(BuiltInRegistries.BLOCK.getKey(b).toString());
			}
		});

		BuiltInRegistries.ITEM.forEach(i -> {
			if (((XmItemAccess) i).xm_modelStateFunc() != null) {
				targets.add(BuiltInRegistries.ITEM.getKey(i).toString());
			}
		});
	}

	@Override
	public @Nullable UnbakedModel loadModel(ModelResourceLocation path, SubModelLoader subModelLoader) {
		return targets.contains(path.getNamespace() + ":" + path.getPath()) ? XmModelProxy.INSTANCE : null;
	}
}
