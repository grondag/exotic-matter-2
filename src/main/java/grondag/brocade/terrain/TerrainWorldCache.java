package grondag.brocade.terrain;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Read-only terrain world cache
 */
public class TerrainWorldCache extends TerrainWorldAdapter {
    @Override
    public void setBlockState(long packedBlockPos, BlockState newState) {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }

    @Override
    protected void setBlockState(long packedBlockPos, BlockState newState, boolean callback) {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }

    @Override
    public void setBlockState(BlockPos blockPos, BlockState newState) {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }
}
