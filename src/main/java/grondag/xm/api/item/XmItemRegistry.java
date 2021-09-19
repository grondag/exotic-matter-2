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

import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus.Experimental;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmRegistryImpl;

@Experimental
public class XmItemRegistry {
	private XmItemRegistry() {}

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
