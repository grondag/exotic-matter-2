package grondag.xm2.block;

import grondag.xm2.state.ModelState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Produces model state instance from world state, refreshing if necessary.  Resulting state may or may not be immutable.
 */
@FunctionalInterface
public interface WorldToModelStateFunction {
	ModelState apply(XmBlockState blockState, BlockView world, BlockPos pos, boolean refreshFromWorldIfNeeded);
}
