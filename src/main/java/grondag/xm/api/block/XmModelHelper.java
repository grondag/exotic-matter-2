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

import java.util.function.BiConsumer;

import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.block.WorldToModelStateFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

@SuppressWarnings("rawtypes")
public interface XmModelHelper {
    static void remodelBlock(
            Block block, 
            ModelPrimitive primitive,
            BiConsumer<BlockState, ModelState.Mutable> stateMapper,
            WorldToModelStateFunction worldStateFunc,
            BlockTest blockJoinTest) {
        
        XmBlockRegistry.register(block, b -> applyMapper(primitive, b, stateMapper), worldStateFunc, blockJoinTest);
    }
    
    
    //TODO: move to impl
    static ModelState applyMapper(
            ModelPrimitive primitive,
            BlockState blockState,
            BiConsumer<BlockState, ModelState.Mutable> stateMapper) {
                final ModelState.Mutable oms = primitive.newState();
                stateMapper.accept(blockState, oms);
                return oms.releaseToImmutable();
    }
            
}
