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

package grondag.xm.mixin.common;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmItemAccess;

@Mixin(Item.class)
public class MixinItem implements XmItemAccess {
	private BiFunction<ItemStack, Level, MutableModelState> modelStateFunc = null;

	@Override
	public void xm_modelStateFunc(BiFunction<ItemStack, Level, MutableModelState> func) {
		modelStateFunc = func;
	}

	@Override
	public BiFunction<ItemStack, Level, MutableModelState> xm_modelStateFunc() {
		return modelStateFunc;
	}
}
