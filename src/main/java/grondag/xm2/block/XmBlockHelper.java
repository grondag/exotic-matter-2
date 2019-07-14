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

import grondag.xm2.api.model.MutableModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Convenience methods for XM Blocks
 */
public class XmBlockHelper {

    /**
     * Returns species at position if it could join with the given block/modelState
     * Returns -1 if no XM block at position or if join not possible.
     */
    // TODO: remove if not used
    public static int getJoinableSpecies(BlockView world, BlockPos pos, BlockState withBlockState,
	    MutableModelState withModelState) {
	if (withBlockState == null || withModelState == null)
	    return -1;

	if (!withModelState.hasSpecies())
	    return -1;

	BlockState state = world.getBlockState(pos);
	if (state.getBlock() == withBlockState.getBlock()) {
	    MutableModelState mState = XmBlockStateAccess.modelState(state, world, pos, false);
	    if (mState == null)
		return -1;

	    if (mState.doShapeAndAppearanceMatch(withModelState))
		return mState.worldState().species();
	}
	return -1;
    }
}
