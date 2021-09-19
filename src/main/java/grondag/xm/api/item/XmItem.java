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
package grondag.xm.api.item;

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmItemAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Experimental
public class XmItem {
	private XmItem() {}

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
