//TODO: remove
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

import grondag.xm.api.modelstate.PrimitiveModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Convenience methods for XM Blocks and Model States
 */
@SuppressWarnings("rawtypes")
public class XmBlockHelper {
    private  XmBlockHelper() {}
    
    /**
     * Returns species at position if it could join with the given block/modelState
     * Returns -1 if no XM block at position or if join not possible.
     */
    public static int getJoinableSpecies(BlockView world, BlockPos pos, BlockState withBlockState, PrimitiveModelState withModelState) {
        if (withBlockState == null || withModelState == null)
            return -1;

        if (!withModelState.hasSpecies())
            return -1;

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == withBlockState.getBlock()) {
            PrimitiveModelState.Mutable mState = XmBlockState.modelState(state, world, pos, false);
            if (mState == null)
                return -1;

            if (mState.doShapeAndAppearanceMatch(withModelState)) {
                mState.release();
                return withModelState.species();
            }
        }
        return -1;
    }
}
