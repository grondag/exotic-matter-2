package grondag.xm.api.modelstate.primitive;

import grondag.xm.api.connect.world.BlockNeighbors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.Nullable;

@Experimental
@FunctionalInterface
public interface SimplePrimitiveStateMutator extends PrimitiveStateMutator {
	@Override
	MutablePrimitiveState mutate(MutablePrimitiveState modelState, BlockState blockState);

	@Override
	default void mutate(MutablePrimitiveState modelState, BlockState blockState, @Nullable BlockGetter world, @Nullable BlockPos pos, @Nullable BlockNeighbors neighbors, boolean refreshFromWorld) {
		mutate(modelState, blockState);
	}
}