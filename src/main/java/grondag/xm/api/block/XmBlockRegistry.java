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

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import grondag.xm.api.modelstate.ModelStateFunction;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmRegistryImpl;

@Experimental
public class XmBlockRegistry {
	private XmBlockRegistry() {}

	/**
	 * @deprecated Use version that accepts world for item state
	 */
	@Deprecated
	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap,
					Function<ItemStack, MutableModelState> itemModelFunction)
	{

		XmRegistryImpl.register(block, modelFunctionMap, (s, w) -> itemModelFunction.apply(s));
	}


	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap,
					BiFunction<ItemStack, World, MutableModelState> itemModelFunction)
	{

		XmRegistryImpl.register(block, modelFunctionMap, itemModelFunction);
	}

	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap) {

		XmRegistryImpl.register(block, modelFunctionMap, DEFAULT_ITEM_MODEL_FUNCTION_V2);
	}

	public static <F extends ModelStateFunction<?>> void addBlock(Block block, F modelFunction) {
		addBlockStates(block, (BlockState bs) -> modelFunction);
	}

	/**
	 * @deprecated Use version that accepts world for item state
	 */
	@Deprecated
	public static <F extends ModelStateFunction<?>> void addBlock(Block block, F blockModelFunction, Function<ItemStack, MutableModelState> itemModelFunction) {
		addBlockStates(block, (BlockState bs) -> blockModelFunction, itemModelFunction);
	}

	public static <F extends ModelStateFunction<?>> void addBlock(Block block, F blockModelFunction, BiFunction<ItemStack, World, MutableModelState> itemModelFunction) {
		addBlockStates(block, (BlockState bs) -> blockModelFunction, itemModelFunction);
	}

	/**
	 * Use {@link #DEFAULT_ITEM_MODEL_FUNCTION_V2}
	 */
	@Deprecated
	public static final Function<ItemStack, MutableModelState> DEFAULT_ITEM_MODEL_FUNCTION  = s -> {
		if (s.getItem() instanceof BlockItem) {
			final BlockItem item = (BlockItem) s.getItem();
			final XmBlockState xmState = XmBlockState.get(item);
			if (xmState != null)
				return xmState.defaultModelState();
		}
		return null;
	};

	public static final BiFunction<ItemStack, World, MutableModelState> DEFAULT_ITEM_MODEL_FUNCTION_V2  = (s, w) -> {
		if (s.getItem() instanceof BlockItem) {
			final BlockItem item = (BlockItem) s.getItem();
			final XmBlockState xmState = XmBlockState.get(item);

			if (xmState != null)
				return xmState.defaultModelState();
		}

		return null;
	};
}
