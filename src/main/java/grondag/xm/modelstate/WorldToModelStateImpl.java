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
import grondag.xm.api.modelstate.primitive.MutablePrimitiveState;
import grondag.xm.api.modelstate.primitive.PrimitiveState;
import grondag.xm.api.modelstate.primitive.WorldToPrimitiveStateMutator;
import grondag.xm.api.modelstate.primitive.WorldToPrimitiveStateMap;
import grondag.xm.api.primitive.simple.CubeWithRotation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
public class WorldToModelStateImpl implements WorldToPrimitiveStateMap {
    private final BlockTest<PrimitiveState> joinTest;
    private final WorldToPrimitiveStateMutator updater;
    private final PrimitiveState defaultState;
    
    private WorldToModelStateImpl(BuilderImpl builder) {
        this.joinTest = builder.joinTest;
        this.defaultState = builder.defaultState;
        if(builder.updaters.isEmpty()) {
            updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {};
        } else if(builder.updaters.size() == 1) {
            updater = builder.updaters.get(0);
        } else {
            final WorldToPrimitiveStateMutator[] funcs = builder.updaters.toArray(new WorldToPrimitiveStateMutator[builder.updaters.size()]);
            updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {
                for(WorldToPrimitiveStateMutator func : funcs) {
                    func.accept(modelState, xmBlockState, world, pos, neighbors, refreshFromWorld);
                }
            };
        }
    };
    
    @Override
    public MutablePrimitiveState apply(BlockState blockState, BlockView world, BlockPos pos, boolean refreshFromWorld) {
        MutablePrimitiveState modelState = defaultState.mutableCopy();
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
    
    private static class BuilderImpl implements WorldToPrimitiveStateMap.Builder {
        private BlockTest<PrimitiveState> joinTest = BlockTest.sameBlock();
        private ArrayList<WorldToPrimitiveStateMutator> updaters = new ArrayList<>();
        private PrimitiveState defaultState = CubeWithRotation.INSTANCE.defaultState();
        
        private BuilderImpl() {}

        @Override
        public Builder withDefaultState(PrimitiveState defaultState) {
            this.defaultState = defaultState == null ? CubeWithRotation.INSTANCE.defaultState() : defaultState;
            return this;
        }
        
        @Override
        public Builder withJoin(BlockTest<PrimitiveState> joinTest) {
            this.joinTest = joinTest == null ? BlockTest.sameBlock() : joinTest;
            return this;
        }
        
        @Override
        public Builder withUpdate(WorldToPrimitiveStateMutator function) {
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
        WorldToPrimitiveStateMap build() {
            return new WorldToModelStateImpl(this);
        }

    }

    public static Builder builder() {
        return new BuilderImpl();
    }
}
