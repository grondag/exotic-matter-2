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

import javax.annotation.Nullable;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.block.XmBlockRegistryImpl.XmBlockStateImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface XmBlockStateAccess {

    void xm2_blockState(XmBlockStateImpl state);

    XmBlockStateImpl xm2_blockState();

    static @Nullable XmBlockStateImpl get(BlockState fromState) {
        return ((XmBlockStateAccess) fromState).xm2_blockState();
    }

    static @Nullable XmBlockStateImpl get(BlockItem fromItem) {
        return get(fromItem.getBlock());
    }

    static @Nullable XmBlockStateImpl get(Block fromBlock) {
        return get(fromBlock.getDefaultState());
    }

    @SuppressWarnings("unchecked")
    static @Nullable <T extends MutableModelState> T modelState(BlockState fromState, BlockView blockView, BlockPos pos, boolean refresh) {
        final XmBlockStateImpl xmState = get(fromState);
        return xmState == null ? null : (T) xmState.getModelState(blockView, pos, refresh);
    }
}
