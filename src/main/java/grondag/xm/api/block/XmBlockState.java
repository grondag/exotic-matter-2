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

import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmBlockStateAccess;

@Experimental
public interface XmBlockState {
	/**
	 * Minecraft block state associated with this Exotic Matter block state.
	 * Association is always 1:1.
	 */
	BlockState blockState();

	/**
	 * If last parameter is false, does not perform a refresh from world for
	 * world-dependent state attributes. Use this option to prevent infinite
	 * recursion when need to reference some static state ) information in order to
	 * determine dynamic world state. Block tests are main use case for false.
	 */
	<T extends MutableModelState> T modelState(@Nullable BlockGetter world, @Nullable BlockPos pos, boolean refreshFromWorld);

	default <T extends MutableModelState> T defaultModelState() {
		return modelState(null, null, false);
	}

	static @Nullable XmBlockState get(BlockState fromState) {
		return ((XmBlockStateAccess) fromState).xm_toXmBlockState();
	}

	static @Nullable XmBlockState get(BlockItem fromItem) {
		return get(fromItem.getBlock());
	}

	static @Nullable XmBlockState get(Block fromBlock) {
		return get(fromBlock.defaultBlockState());
	}

	@SuppressWarnings("unchecked")
	static @Nullable <T extends MutableModelState> T modelState(BlockState fromState, @Nullable BlockGetter blockView, @Nullable BlockPos pos, boolean refresh) {
		final XmBlockState xmState = get(fromState);
		return xmState == null ? null : (T) xmState.modelState(blockView, pos, refresh);
	}
}
