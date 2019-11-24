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
package grondag.xm.api.block;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.ModelStateFunction;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

@API(status = EXPERIMENTAL)
public class XmBlockRegistry {
	private XmBlockRegistry() {}

	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap,
					Function<ItemStack, MutableModelState> itemModelFunction)
	{

		XmRegistryImpl.register(block, modelFunctionMap, itemModelFunction);
	}

	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap) {

		XmRegistryImpl.register(block, modelFunctionMap, DEFAULT_ITEM_MODEL_FUNCTION);
	}

	public static <F extends ModelStateFunction<?>> void addBlock(Block block, F modelFunction) {
		addBlockStates(block, (BlockState bs) -> modelFunction);
	}

	public static <F extends ModelStateFunction<?>> void addBlock(Block block, F blockModelFunction, Function<ItemStack, MutableModelState> itemModelFunction) {
		addBlockStates(block, (BlockState bs) -> blockModelFunction, itemModelFunction);
	}

	public static final Function<ItemStack, MutableModelState> DEFAULT_ITEM_MODEL_FUNCTION  = s -> {
		if (s.getItem() instanceof BlockItem) {
			final BlockItem item = (BlockItem) s.getItem();
			final XmBlockState xmState = XmBlockState.get(item);
			if (xmState != null)
				return xmState.defaultModelState();
		}
		return null;
	};
}
