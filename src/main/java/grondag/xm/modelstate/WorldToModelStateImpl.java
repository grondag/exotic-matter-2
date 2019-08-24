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
package grondag.xm.modelstate;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_POS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SIMPLE_JOIN;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.ArrayList;

import org.apiguardian.api.API;

import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.MasonryHelper;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.modelstate.SimpleModelStateUpdate;
import grondag.xm.api.modelstate.WorldToSimpleModelState;
import grondag.xm.api.primitive.simple.CubeWithRotation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
public class WorldToModelStateImpl implements WorldToSimpleModelState {
    private final BlockTest<SimpleModelState> joinTest;
    private final SimpleModelStateUpdate updater;
    private final SimpleModelState defaultState;
    
    private WorldToModelStateImpl(BuilderImpl builder) {
        this.joinTest = builder.joinTest;
        this.defaultState = builder.defaultState;
        if(builder.updaters.isEmpty()) {
            updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {};
        } else if(builder.updaters.size() == 1) {
            updater = builder.updaters.get(0);
        } else {
            final SimpleModelStateUpdate[] funcs = builder.updaters.toArray(new SimpleModelStateUpdate[builder.updaters.size()]);
            updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {
                for(SimpleModelStateUpdate func : funcs) {
                    func.accept(modelState, xmBlockState, world, pos, neighbors, refreshFromWorld);
                }
            };
        }
    };
    
    @Override
    public SimpleModelState.Mutable apply(BlockState blockState, BlockView world, BlockPos pos, boolean refreshFromWorld) {
        SimpleModelState.Mutable modelState = defaultState.mutableCopy();
        if(!modelState.isStatic() && refreshFromWorld) {
            
            BlockNeighbors neighbors = null;
            
            final int stateFlags = modelState.stateFlags();
            if ((stateFlags & STATE_FLAG_NEEDS_POS) == STATE_FLAG_NEEDS_POS) {
                modelState.pos(pos);
            }

            if ((STATE_FLAG_NEEDS_CORNER_JOIN & stateFlags) == STATE_FLAG_NEEDS_CORNER_JOIN) {
                neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, joinTest);
                modelState.cornerJoin(CornerJoinState.fromWorld(neighbors));

            } else if ((STATE_FLAG_NEEDS_SIMPLE_JOIN & stateFlags) == STATE_FLAG_NEEDS_SIMPLE_JOIN) {
                neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, joinTest);
                modelState.simpleJoin(SimpleJoinState.fromWorld(neighbors));
            }

            if ((STATE_FLAG_NEEDS_MASONRY_JOIN & stateFlags) == STATE_FLAG_NEEDS_MASONRY_JOIN) {
                if (neighbors == null) {
                    neighbors = BlockNeighbors.claim(world, pos, ModelStateFunction.STATIC, MasonryHelper.wrap(joinTest));
                } else {
                    neighbors.withTest(MasonryHelper.wrap(joinTest));
                }
                modelState.masonryJoin(SimpleJoinState.fromWorld(neighbors));
            }
            
            updater.accept(modelState, blockState, world, pos, neighbors, refreshFromWorld);

            if (neighbors != null) {
                neighbors.release();
            }
        }      
        return modelState;
    }
    
    private static class BuilderImpl implements WorldToSimpleModelState.Builder {
        private BlockTest<SimpleModelState> joinTest = BlockTest.sameBlock();
        private ArrayList<SimpleModelStateUpdate> updaters = new ArrayList<>();
        private SimpleModelState defaultState = CubeWithRotation.INSTANCE.defaultState();
        
        private BuilderImpl() {}

        @Override
        public Builder withDefaultState(SimpleModelState defaultState) {
            this.defaultState = defaultState == null ? CubeWithRotation.INSTANCE.defaultState() : defaultState;
            return this;
        }
        
        @Override
        public Builder withJoin(BlockTest<SimpleModelState> joinTest) {
            this.joinTest = joinTest == null ? BlockTest.sameBlock() : joinTest;
            return this;
        }
        
        @Override
        public Builder withUpdate(SimpleModelStateUpdate function) {
            if(function != null) {
                updaters.add(function);
            }
            return this;
        }

        @Override
        public Builder clear() {
            joinTest = BlockTest.sameBlock();
            defaultState = CubeWithRotation.INSTANCE.defaultState();
            updaters.clear();
            return this;
        }
        
        @Override
        public
        WorldToSimpleModelState build() {
            return new WorldToModelStateImpl(this);
        }

    }

    public static Builder builder() {
        return new BuilderImpl();
    }
}