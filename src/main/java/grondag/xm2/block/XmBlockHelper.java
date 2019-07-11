package grondag.xm2.block;



import grondag.xm2.block.wip.XmBlockStateAccess;
import grondag.xm2.state.ModelState;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Convenience methods for XM Blocks
 */
public class XmBlockHelper {
    
    /**
     * Returns species at position if it could join with the given block/modelState
     * Returns -1 if no XM block at position or if join not possible.
     */
    public static int getJoinableSpecies(BlockView world, BlockPos pos, BlockState withBlockState, ModelState withModelState) {
        if (withBlockState == null || withModelState == null)
            return -1;

        if (!withModelState.hasSpecies())
            return -1;

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == withBlockState.getBlock()) {
            ModelState mState = XmBlockStateAccess.modelState(state, world, pos, false);
            if (mState == null)
                return -1;

            if (mState.doShapeAndAppearanceMatch(withModelState))
                return mState.getSpecies();
        }
        return -1;
    }
}
