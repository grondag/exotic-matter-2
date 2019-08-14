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

import grondag.xm.api.block.WorldToModelStateFunction;
import grondag.xm.api.connect.world.BlockTest;

@FunctionalInterface
public interface SimpleModelStateFunction extends WorldToModelStateFunction<SimpleModelState.Mutable> {
    static SimpleModelStateFunction ofDefaultState(SimpleModelState defaultState) {
        return builder().withDefaultState(defaultState).build();
    }
    
    static Builder builder() {
        return SimpleModelStateFunctionImpl.builder();
    }
    
    public interface Builder {
        Builder withJoin(BlockTest<SimpleModelState> joinTest);
        
        Builder withUpdate(SimpleModelStateOperation update);

        SimpleModelStateFunction build();

        Builder clear();

        Builder withDefaultState(SimpleModelState defaultState);
    }
}
