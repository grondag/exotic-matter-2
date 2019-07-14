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

package grondag.xm2.block;

import java.util.function.Function;

import grondag.xm2.Xm;
import grondag.xm2.connect.api.world.BlockTest;
import grondag.xm2.model.impl.state.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class XmBlockRegistryImpl {
	private XmBlockRegistryImpl() {}

	public static void register(
			Block block, 
			Function<BlockState, ModelState> defaultStateFunc, 
			WorldToModelStateFunction worldStateFunc,
			BlockTest blockJoinTest ) {
		
		for(BlockState blockState : block.getStateFactory().getStates()) {
			if(XmBlockState.get(blockState) != null) {
				//TODO: localize
				Xm.LOG.warn(String.format("[%s] BlockState %s already associated with an XmBlockState. Skipping." , Xm.MODID, blockState.toString()));
				return;
			}
			XmBlockStateImpl xmState = new XmBlockStateImpl(
					defaultStateFunc.apply(blockState),
					worldStateFunc,
					blockJoinTest,
					blockState);
			((XmBlockStateAccess)blockState).xm2_blockState(xmState);
		}
	}
	
	public static class XmBlockStateImpl implements XmBlockState {
		public final WorldToModelStateFunction worldStateFunc;
		public final BlockTest blockJoinTest;
		public final ModelState defaultModelState;
		public final BlockState blockState;
		
		private XmBlockStateImpl(
				ModelState defaultModelState,
				WorldToModelStateFunction worldStateFunc,
				BlockTest blockJoinTest,
				BlockState blockState ) {
			
			this.defaultModelState = defaultModelState;
			this.worldStateFunc = worldStateFunc;
			this.blockJoinTest = blockJoinTest;
			this.blockState = blockState;
		}

		@Override
		public BlockTest blockJoinTest() {
			return blockJoinTest;
		}

		@Override
		public ModelState defaultModelState() {
			return defaultModelState;
		}

		@Override
		public ModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorld) {
			return worldStateFunc.apply(this, world, pos, refreshFromWorld);
		}

		@Override
		public BlockState blockState() {
			return blockState;
		}
	}
}
