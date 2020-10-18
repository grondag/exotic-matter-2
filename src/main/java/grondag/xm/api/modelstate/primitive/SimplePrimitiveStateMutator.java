package grondag.xm.api.modelstate.primitive;

import grondag.xm.api.connect.world.BlockNeighbors;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Experimental
@FunctionalInterface
public interface SimplePrimitiveStateMutator extends PrimitiveStateMutator {
	@Override
	MutablePrimitiveState mutate(MutablePrimitiveState modelState, BlockState blockState);

	@Override
	default void mutate(MutablePrimitiveState modelState, BlockState blockState, @Nullable BlockView world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld) {
		mutate(modelState, blockState);
	}
}