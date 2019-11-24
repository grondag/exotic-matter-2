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

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.dispatch.XmBlockStateAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
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
	<T extends MutableModelState> T modelState(@Nullable BlockView world, @Nullable BlockPos pos, boolean refreshFromWorld);

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
		return get(fromBlock.getDefaultState());
	}

	@SuppressWarnings("unchecked")
	static @Nullable <T extends MutableModelState> T modelState(BlockState fromState, @Nullable BlockView blockView, @Nullable BlockPos pos, boolean refresh) {
		final XmBlockState xmState = get(fromState);
		return xmState == null ? null : (T) xmState.modelState(blockView, pos, refresh);
	}
}
