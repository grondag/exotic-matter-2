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

package grondag.xm.api.block;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.modelstate.ModelStateFunction;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmRegistryImpl;

@Experimental
public class XmBlockRegistry {
	private XmBlockRegistry() { }

	/**
	 * @deprecated Use version that accepts world for item state
	 */
	@Deprecated
	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap,
			Function<ItemStack, MutableModelState> itemModelFunction
	) {
		XmRegistryImpl.register(block, modelFunctionMap, (s, w) -> itemModelFunction.apply(s));
	}

	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap,
					BiFunction<ItemStack, Level, MutableModelState> itemModelFunction
	) {
		XmRegistryImpl.register(block, modelFunctionMap, itemModelFunction);
	}

	public static void addBlockStates(
			Block block,
			Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap
	) {
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

	public static <F extends ModelStateFunction<?>> void addBlock(Block block, F blockModelFunction, BiFunction<ItemStack, Level, MutableModelState> itemModelFunction) {
		addBlockStates(block, (BlockState bs) -> blockModelFunction, itemModelFunction);
	}

	/**
	 * Use {@link #DEFAULT_ITEM_MODEL_FUNCTION_V2}.
	 */
	@Deprecated
	public static final Function<ItemStack, MutableModelState> DEFAULT_ITEM_MODEL_FUNCTION = s -> {
		if (s.getItem() instanceof BlockItem) {
			final BlockItem item = (BlockItem) s.getItem();
			final XmBlockState xmState = XmBlockState.get(item);

			if (xmState != null) {
				return xmState.defaultModelState();
			}
		}

		return null;
	};

	public static final BiFunction<ItemStack, Level, MutableModelState> DEFAULT_ITEM_MODEL_FUNCTION_V2 = (s, w) -> {
		if (s.getItem() instanceof BlockItem) {
			final BlockItem item = (BlockItem) s.getItem();
			final XmBlockState xmState = XmBlockState.get(item);

			if (xmState != null) {
				return xmState.defaultModelState();
			}
		}

		return null;
	};
}
