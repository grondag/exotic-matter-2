package grondag.brocade.model.state;

import javax.annotation.Nullable;

import grondag.exotic_matter.block.ISuperBlock;
import grondag.exotic_matter.block.SuperBlockWorldAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Convenience methods for SuperBlock and subclasses
 */
public class SuperBlockHelper {
    /**
     * returns null if not a superblock at the position
     */
    public static ISuperModelState getModelStateIfAvailable(IBlockAccess world, BlockPos pos,
            boolean refreshFromWorldIfNeeded) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ISuperBlock) {
            return SuperBlockWorldAccess.access(world).getModelState((ISuperBlock) state.getBlock(), state, pos,
                    refreshFromWorldIfNeeded);
        }
        return null;
    }

    /**
     * Returns species at position if it could join with the given block/modelState
     * Returns -1 if no superblock at position or if join not possible.
     */
    public static int getJoinableSpecies(IBlockAccess world, BlockPos pos, @Nullable IBlockState withBlockState,
            @Nullable ISuperModelState withModelState) {
        if (withBlockState == null || withModelState == null)
            return -1;

        if (!withModelState.hasSpecies())
            return -1;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == withBlockState.getBlock()) {
            ISuperModelState mState = getModelStateIfAvailable(world, pos, false);
            if (mState == null)
                return -1;

            if (mState.doShapeAndAppearanceMatch(withModelState))
                return mState.getSpecies();
        }
        return -1;
    }
}
