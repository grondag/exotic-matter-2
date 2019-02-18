package grondag.brocade.block;

import javax.annotation.Nullable;

import grondag.exotic_matter.model.state.ISuperModelState;
import grondag.exotic_matter.terrain.TerrainState;
import grondag.exotic_matter.world.PackedBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

/**
 * Provides convenient access methods and also an opportunity to cache expensive value.
 */
public interface ISuperBlockAccess extends IBlockAccess
{
    public default ISuperModelState getModelState(ISuperBlock block, IBlockState blockState, BlockPos pos, boolean refreshFromWorld)
    {
        return block.getModelStateAssumeStateIsCurrent(blockState, this, pos, refreshFromWorld);
    }
    
    public default ISuperModelState getModelState(ISuperBlock block, BlockPos pos, boolean refreshFromWorld)
    {
        return block.getModelState(this, pos, refreshFromWorld);
    }
    
    public default ISuperModelState computeModelState(ISuperBlock block, IBlockState blockState, BlockPos pos, boolean refreshFromWorld)
    {
        return block.getModelStateAssumeStateIsCurrent(blockState, this, pos, refreshFromWorld);
    }
    
    public default int getFlowHeight(BlockPos pos)
    {
        return TerrainState.getFlowHeight(this, PackedBlockPos.pack(pos));
    }
    
    public default int getFlowHeight(long packedBlockPos)
    {
        return TerrainState.getFlowHeight(this, packedBlockPos);
    }
    
    public IBlockAccess wrapped();
    
    public IBlockState getBlockState(long packedBlockPos);
    
    //TODO: make packed pos the preferred implementation
    public default TerrainState terrainState(IBlockState state, long packedBlockPos)
    {
        return terrainState(state, PackedBlockPos.unpack(packedBlockPos));
    }
    
    public default TerrainState terrainState(IBlockState state, BlockPos pos)
    {
        return TerrainState.terrainState(this, state, pos);
    }
    
    public default TerrainState terrainState(long packedBlockPos)
    {
        return terrainState(getBlockState(packedBlockPos), packedBlockPos);
    }
    
    public default TerrainState terrainState(BlockPos pos)
    {
        return terrainState(getBlockState(pos), pos);
    }
    
    @Override
    @Nullable
    public default TileEntity getTileEntity(BlockPos pos)
    {
        return wrapped().getTileEntity(pos);
    }

    @Override
    public default int getCombinedLight(BlockPos pos, int lightValue)
    {
        return wrapped().getCombinedLight(pos, lightValue);
    }

    @Override
    public default boolean isAirBlock(BlockPos pos)
    {
        IBlockState state = this.getBlockState(pos);
        return state.getBlock().isAir(state, this, pos);
    }

    @Override
    public default Biome getBiome(BlockPos pos)
    {
        return wrapped().getBiome(pos);
    }

    @Override
    public default int getStrongPower(BlockPos pos, EnumFacing direction)
    {
        return wrapped().getStrongPower(pos, direction);
    }

    @Override
    public default WorldType getWorldType()
    {
        return wrapped().getWorldType();
    }

    @Override
    public default boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default)
    {
        return wrapped().isSideSolid(pos, side, _default); 
    }
}
