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

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_POS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SIMPLE_JOIN;

import grondag.xm.api.block.WorldToModelStateFunction;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.MasonryHelper;
import grondag.xm.api.connect.world.ModelStateFunction;

public interface SimpleModelState extends PrimitiveModelState<SimpleModelState, SimpleModelState.Mutable>  {
    public static interface Mutable extends SimpleModelState, PrimitiveModelState.Mutable<SimpleModelState, SimpleModelState.Mutable> {
        
    }
    
    public static final WorldToModelStateFunction FROM_WORLD = (modelStateIn, xmBlockState, world, pos, refreshFromWorld) -> {
        if(refreshFromWorld) {
            SimpleModelState.Mutable modelState = (SimpleModelState.Mutable) modelStateIn;
            final int stateFlags = modelState.stateFlags();
            if ((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) {
                modelState.pos(pos);
            }

            BlockNeighbors neighbors = null;

            if ((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN) {
                neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, xmBlockState.blockJoinTest());
                modelState.cornerJoin(CornerJoinState.fromWorld(neighbors));

            } else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN) {
                neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, xmBlockState.blockJoinTest());
                modelState.simpleJoin(SimpleJoinState.fromWorld(neighbors));
            }

            if ((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN) {
                @SuppressWarnings("unchecked")
                BlockTest<SimpleModelState> myTest = (BlockTest<SimpleModelState>) xmBlockState.blockJoinTest();
                if (neighbors == null) {
                    neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, MasonryHelper.wrap(myTest));
                } else {
                    neighbors.withTest(MasonryHelper.wrap(myTest));
                }
                modelState.masonryJoin(SimpleJoinState.fromWorld(neighbors));
            }

            if (neighbors != null) {
                neighbors.release();
            }
        }
    };
}
