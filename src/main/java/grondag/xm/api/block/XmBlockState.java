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

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmBlockStateAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

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
		return ((XmBlockStateAccess)fromState).xm_toXmBlockState();
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
