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
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.Xm;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelStateFunction;
import grondag.xm.api.modelstate.MutableModelState;

@Internal
public class XmRegistryImpl {
	private XmRegistryImpl() {
	}

	public static void register(Block block, Function<BlockState, ? extends ModelStateFunction<?>> modelFunctionMap, BiFunction<ItemStack, Level, MutableModelState> itemModelFunction) {
		for (final BlockState blockState : block.getStateDefinition().getPossibleStates()) {
			if (XmBlockState.get(blockState) != null) {
				// TODO: localize
				Xm.LOG.warn(String.format("[%s] BlockState %s already associated with an XmBlockState. Skipping.", Xm.MODID, blockState.toString()));
				return;
			}

			((XmBlockStateAccess) blockState).xm_modelStateFunc(modelFunctionMap.apply(blockState));

			if (itemModelFunction != null) {
				final Item item = BlockItem.byBlock(block);

				if (item != null) {
					register(item, itemModelFunction);
				}
			}
		}
	}

	public static void register(Item item, BiFunction<ItemStack, Level, MutableModelState> modelFunction) {
		final XmItemAccess access = (XmItemAccess) item;
		final BiFunction<ItemStack, Level, MutableModelState> oldFunc = access.xm_modelStateFunc();

		if (oldFunc != null) {
			if (oldFunc != modelFunction) {
				Xm.LOG.warn(String.format("[%s] Item %s already associated with a model function. Skipping.", Xm.MODID, item.toString()));
				return;
			}
		}

		access.xm_modelStateFunc(modelFunction);
	}
}
