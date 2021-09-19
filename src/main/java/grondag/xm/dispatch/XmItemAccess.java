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

import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.xm.api.modelstate.MutableModelState;

@Internal
public interface XmItemAccess {
	@SuppressWarnings("unchecked")
	static <T extends MutableModelState> T getModelState(Level world, ItemStack stack) {
		final BiFunction<ItemStack, Level, MutableModelState> func = ((XmItemAccess)stack.getItem()).xm_modelStateFunc();
		return func == null ? null : (T) func.apply(stack, world);
	}

	BiFunction<ItemStack, Level, MutableModelState> xm_modelStateFunc();

	void xm_modelStateFunc(BiFunction<ItemStack, Level, MutableModelState> func);
}
