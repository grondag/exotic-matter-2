package grondag.xm.api.modelstate;

import javax.annotation.Nullable;

import grondag.xm.api.connect.world.BlockNeighbors;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@FunctionalInterface
public interface ModelStateOperation<T extends ModelState.Mutable> {
    void accept(T modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld);
    
    default T apply(T modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld) {
        accept(modelState, blockState, world, pos, neighbors, refreshFromWorld);
        return modelState;
    }
    
    default T apply(T modelState, BlockState blockState) {
        accept(modelState, blockState, null, null, null, false);
        return modelState;
    }
}
