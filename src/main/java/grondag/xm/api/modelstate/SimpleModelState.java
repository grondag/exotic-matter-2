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

package grondag.xm.api.modelstate;

import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;

public interface SimpleModelState extends PrimitiveModelState<SimpleModelState, SimpleModelState.Mutable>  {
    public static interface Mutable extends SimpleModelState, PrimitiveModelState.Mutable<SimpleModelState, SimpleModelState.Mutable> {
        
    }
    
    ModelStateMap.Modifier<BlockState, Mutable> AXIS_FROM_BLOCKSTATE = (modelState, blockState) -> {
        Comparable<?> axis = blockState.getEntries().get(PillarBlock.AXIS);
        if (axis != null) {
            modelState.axis(PillarBlock.AXIS.getValueType().cast(axis));
        }
        return modelState;
    };
}
