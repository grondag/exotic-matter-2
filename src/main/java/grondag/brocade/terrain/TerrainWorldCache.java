package grondag.brocade.terrain;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Read-only terrain world cache
 */
public class TerrainWorldCache extends TerrainWorldAdapter
{
    @Override
    public void setBlockState(long packedBlockPos, IBlockState newState)
    {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }

    @Override
    protected void setBlockState(long packedBlockPos, IBlockState newState, boolean callback)
    {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }

    @Override
    public void setBlockState(BlockPos blockPos, IBlockState newState)
    {
        throw new UnsupportedOperationException("TerrainWorldCache is read-only");
    }
}
