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

import javax.annotation.Nullable;

import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.modelstate.ModelState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Produces model state instance from world state, refreshing if necessary.
 */
@FunctionalInterface
@SuppressWarnings("unchecked")
public interface WorldToModelStateFunction<T extends ModelState.Mutable> {
    void accept(T modelState, XmBlockState xmBlockState, BlockView world, BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld);
    
    default T apply(XmBlockState xmBlockState, BlockView world, BlockPos pos, boolean refreshFromWorld) {
        final T result = (T) xmBlockState.defaultModelState().mutableCopy();
        accept(result, xmBlockState, world, pos, null, refreshFromWorld);
        return result;
    }
}
