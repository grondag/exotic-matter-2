/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.dispatch;

import java.util.Collections;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import grondag.xm.api.item.XmItem;
import grondag.xm.api.modelstate.MutableModelState;

@Internal
public class ItemOverrideProxy extends ItemOverrides {
	static final ItemOverrideProxy INSTANCE = new ItemOverrideProxy();

	private ItemOverrideProxy() {
		super(null, null, null, Collections.emptyList());
	}

	@Override
	public BakedModel resolve(BakedModel bakedModel_1, ItemStack stack, @Nullable ClientLevel world, LivingEntity livingEntity_1, int seed) {
		final MutableModelState modelState = XmItem.modelState(world, stack);

		if (modelState != null) {
			final BakedModel result = XmDispatcher.INSTANCE.get(modelState).itemProxy();
			modelState.release();
			return result;
		} else {
			return Minecraft.getInstance().getModelManager().getMissingModel();
		}
	}
}
