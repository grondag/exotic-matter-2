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

package grondag.xm.api.item;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmItemAccess;

@Experimental
public class XmItem {
	private XmItem() { }

	/**
	 * @deprecated Use version with world
	 */
	@Deprecated
	public static @Nullable <T extends MutableModelState> T modelState(ItemStack stack) {
		return XmItemAccess.getModelState(null, stack);
	}

	public static @Nullable <T extends MutableModelState> T modelState(Level world, ItemStack stack) {
		return XmItemAccess.getModelState(world, stack);
	}
}
