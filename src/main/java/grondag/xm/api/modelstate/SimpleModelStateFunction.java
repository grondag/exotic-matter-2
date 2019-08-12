package grondag.xm.api.modelstate;

import grondag.xm.api.block.WorldToModelStateFunction;
import grondag.xm.api.connect.world.BlockTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;

@FunctionalInterface
public interface SimpleModelStateFunction extends WorldToModelStateFunction<SimpleModelState.Mutable> {
    
    SimpleModelStateFunction DEFAULT = builder().build();
    
    SimpleModelStateFunction UPDATE_AXIS = (modelState, xmBlockState, world, pos, neighbors, refreshFromWorld) -> {
        final BlockState blockState = xmBlockState.blockState();
        Comparable<?> axis = blockState.getEntries().get(PillarBlock.AXIS);
        if (axis != null) {
            modelState.axis(PillarBlock.AXIS.getValueType().cast(axis));
        }
    };
    
    static Builder builder() {
        return SimpleModelStateFunctionImpl.builder();
    }
    
    public interface Builder {
        Builder withJoin(BlockTest<SimpleModelState> joinTest);
        
        Builder withUpdate(SimpleModelStateFunction function);

        SimpleModelStateFunction build();

        Builder clear();

    }
}
