package grondag.xm.api.modelstate;

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_POS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SIMPLE_JOIN;

import java.util.ArrayList;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.BlockTest;
import grondag.xm.api.connect.world.MasonryHelper;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.modelstate.SimpleModelState.Mutable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SimpleModelStateFunctionImpl implements SimpleModelStateFunction {
    private final BlockTest<SimpleModelState> joinTest;
    private final SimpleModelStateFunction updater;
    
    private SimpleModelStateFunctionImpl(BuilderImpl builder) {
        this.joinTest = builder.joinTest;
        if(builder.updaters.isEmpty()) {
            updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {};
        } else if(builder.updaters.size() == 1) {
            updater = builder.updaters.get(0);
        } else {
            final SimpleModelStateFunction[] funcs = builder.updaters.toArray(new SimpleModelStateFunction[builder.updaters.size()]);
            updater = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {
                for(SimpleModelStateFunction func : funcs) {
                    func.accept(modelState, xmBlockState, world, pos, neighbors, refreshFromWorld);
                }
            };
        }
    };
    
    @Override
    public void accept(Mutable modelState, XmBlockState xmBlockState, BlockView world, BlockPos pos, BlockNeighbors neighbors, boolean refreshFromWorld) {
        if(refreshFromWorld) {
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
            
            updater.accept(modelState, xmBlockState, world, pos, neighbors, refreshFromWorld);

            if (neighbors != null) {
                neighbors.release();
            }
        }        
    }
    
    private static class BuilderImpl implements SimpleModelStateFunction.Builder {
        private BlockTest<SimpleModelState> joinTest = BlockTest.sameBlock();
        private ArrayList<SimpleModelStateFunction> updaters = new ArrayList<>();
        
        private BuilderImpl() {}

        @Override
        public Builder withJoin(BlockTest<SimpleModelState> joinTest) {
            this.joinTest = joinTest == null ? BlockTest.sameBlock() : joinTest;
            return this;
        }
        
        @Override
        public Builder withUpdate(SimpleModelStateFunction function) {
            if(function != null) {
                updaters.add(function);
            }
            return this;
        }

        @Override
        public Builder clear() {
            joinTest = BlockTest.sameBlock();
            updaters.clear();
            return this;
        }
        
        @Override
        public
        SimpleModelStateFunction build() {
            return new SimpleModelStateFunctionImpl(this);
        }

    }

    static Builder builder() {
        return new BuilderImpl();
    }
}
