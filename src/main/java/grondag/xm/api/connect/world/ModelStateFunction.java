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

package grondag.xm.api.connect.world;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.ModelState;
import grondag.xm.block.XmBlockStateAccess;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Provide an instance to {@link BlockNeighbors} when it is retrieved in order
 * to retrieve values with lazy evaluation and caching. The resulting values
 * will also be provided to {@link BlockTest}.
 */
@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface ModelStateFunction {
    public ModelState get(BlockView world, BlockState blockState, BlockPos pos);
    
    /**
     * Use this as factory for model state block tests that DON'T need to refresh
     * from world.
     */
    static final ModelStateFunction STATIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p, false);
    /**
     * Use this as factory for model state block tests that DO need to refresh from
     * world.
     */
    static final ModelStateFunction DYNAMIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p, true);
}
