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

package grondag.xm.block;

import java.util.function.Function;

import grondag.xm.Xm;
import grondag.xm.api.block.XmBlockRegistry;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class XmBlockRegistryImpl implements XmBlockRegistry {
    private XmBlockRegistryImpl() {
    }

    public static void register(Block block, Function<BlockState, ModelState> defaultStateFunc, WorldToModelStateFunction worldStateFunc,
            BlockTest<?> blockJoinTest) {

        for (BlockState blockState : block.getStateFactory().getStates()) {
            if (XmBlockState.get(blockState) != null) {
                // TODO: localize
                Xm.LOG.warn(String.format("[%s] BlockState %s already associated with an XmBlockState. Skipping.", Xm.MODID, blockState.toString()));
                return;
            }
            XmBlockStateImpl xmState = new XmBlockStateImpl(defaultStateFunc.apply(blockState), worldStateFunc, blockJoinTest, blockState);
            ((XmBlockStateAccess) blockState).xm2_blockState(xmState);
        }
    }

    public static class XmBlockStateImpl implements XmBlockState {
        public final WorldToModelStateFunction worldStateFunc;
        public final BlockTest<?> blockJoinTest;
        public final ModelState defaultModelState;
        public final BlockState blockState;

        private XmBlockStateImpl(ModelState defaultModelState, WorldToModelStateFunction worldStateFunc, BlockTest<?> blockJoinTest,
                BlockState blockState) {

            this.defaultModelState = defaultModelState;
            this.worldStateFunc = worldStateFunc;
            this.blockJoinTest = blockJoinTest;
            this.blockState = blockState;
        }

        @Override
        public BlockTest<?> blockJoinTest() {
            return blockJoinTest;
        }

        @SuppressWarnings("unchecked")
        @Override
        public ModelState defaultModelState() {
            return defaultModelState;
        }

        @SuppressWarnings("unchecked")
        @Override
        public MutableModelState getModelState(BlockView world, BlockPos pos, boolean refreshFromWorld) {
            return worldStateFunc.apply(this, world, pos, refreshFromWorld && !defaultModelState.isStatic());
        }

        @Override
        public BlockState blockState() {
            return blockState;
        }
    }
}
