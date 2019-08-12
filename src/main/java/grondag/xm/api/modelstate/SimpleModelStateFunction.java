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
