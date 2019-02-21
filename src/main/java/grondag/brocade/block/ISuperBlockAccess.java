package grondag.brocade.block;


import grondag.brocade.model.state.ISuperModelState;
import grondag.brocade.terrain.TerrainState;
import grondag.fermion.world.PackedBlockPos;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.biome.Biome;

/**
 * Provides convenient access methods and also an opportunity to cache expensive
 * value.
 */
public interface ISuperBlockAccess extends ExtendedBlockView {
    public default ISuperModelState getModelState(ISuperBlock block, BlockState blockState, BlockPos pos,
            boolean refreshFromWorld) {
        return block.getModelStateAssumeStateIsCurrent(blockState, this, pos, refreshFromWorld);
    }

    public default ISuperModelState getModelState(ISuperBlock block, BlockPos pos, boolean refreshFromWorld) {
        return block.getModelState(this, pos, refreshFromWorld);
    }

    public default ISuperModelState computeModelState(ISuperBlock block, BlockState blockState, BlockPos pos,
            boolean refreshFromWorld) {
        return block.getModelStateAssumeStateIsCurrent(blockState, this, pos, refreshFromWorld);
    }

    public default int getFlowHeight(BlockPos pos) {
        return TerrainState.getFlowHeight(this, PackedBlockPos.pack(pos));
    }

    public default int getFlowHeight(long packedBlockPos) {
        return TerrainState.getFlowHeight(this, packedBlockPos);
    }

    public BlockState getBlockState(long packedBlockPos);

    // TODO: make packed pos the preferred implementation
    public default TerrainState terrainState(BlockState state, long packedBlockPos) {
        return terrainState(state, PackedBlockPos.unpack(packedBlockPos));
    }

    public default TerrainState terrainState(BlockState state, BlockPos pos) {
        return TerrainState.terrainState(this, state, pos);
    }

    public default TerrainState terrainState(long packedBlockPos) {
        return terrainState(getBlockState(packedBlockPos), packedBlockPos);
    }

    public default TerrainState terrainState(BlockPos pos) {
        return terrainState(getBlockState(pos), pos);
    }
}
