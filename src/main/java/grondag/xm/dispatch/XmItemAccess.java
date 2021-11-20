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

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import grondag.xm.api.modelstate.MutableModelState;

@Internal
public interface XmItemAccess {
	@SuppressWarnings("unchecked")
	static <T extends MutableModelState> T getModelState(Level world, ItemStack stack) {
		final BiFunction<ItemStack, Level, MutableModelState> func = ((XmItemAccess) stack.getItem()).xm_modelStateFunc();
		return func == null ? null : (T) func.apply(stack, world);
	}

	BiFunction<ItemStack, Level, MutableModelState> xm_modelStateFunc();

	void xm_modelStateFunc(BiFunction<ItemStack, Level, MutableModelState> func);
}
