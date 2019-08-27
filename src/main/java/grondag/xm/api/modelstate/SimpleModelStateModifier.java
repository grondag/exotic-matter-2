package grondag.xm.api.modelstate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.connect.world.BlockNeighbors;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@API(status = EXPERIMENTAL)
@FunctionalInterface
public interface SimpleModelStateModifier extends ModelStateModifier<BlockState, MutableSimpleModelState>, SimpleModelStateUpdate {
    @Override
    MutableSimpleModelState apply(MutableSimpleModelState modelState, BlockState blockState);
    
    @Override
    default void accept(MutableSimpleModelState modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld) {
        apply(modelState, blockState);
    }
}