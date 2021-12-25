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

package grondag.xm.api.item;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmRegistryImpl;

@Experimental
public class XmItemRegistry {
	private XmItemRegistry() { }

	/**
	 * @deprecated Use version that accepts world for item state
	 */
	@Deprecated
	public static void addItem(Item item, Function<ItemStack, MutableModelState> modelFunction) {
		XmRegistryImpl.register(item, (s, w) -> modelFunction.apply(s));
	}

	public static void addItem(Item item, BiFunction<ItemStack, Level, MutableModelState> modelFunction) {
		XmRegistryImpl.register(item, modelFunction);
	}
}
