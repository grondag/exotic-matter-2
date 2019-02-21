package grondag.brocade.terrain;

import java.util.function.Predicate;

import grondag.brocade.block.BrocadeBlock;
import grondag.brocade.block.ISuperBlock;
import grondag.brocade.block.ISuperBlockAccess;
import grondag.fermion.world.PackedBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TerrainBlockHelper {
    /**
     * Convenience method to check for flow block.
     */
    public static boolean isFlowBlock(BlockState state) {
        return ((BrocadeBlock)state.getBlock()).brocade_isTerrain(state);
    }
    
    /**
     * Convenience method to check for filler block.
     */
    public static boolean isFlowFiller(BlockState state) {
        return ((BrocadeBlock)state.getBlock()).brocade_isTerrainFiller(state);
    }

    /**
     * Convenience method to check for height block.
     */
    public static boolean isFlowHeight(BlockState state) {
        return ((BrocadeBlock)state.getBlock()).brocade_isTerrainHeight(state);
    }

    public static int getHotness(BlockState state) {
        return ((BrocadeBlock)state.getBlock()).brocade_isHot(state) 
                ? state.get(BrocadeBlock.HEAT).intValue() : 0;
    }

    /**
     * Use for height blocks. Returns a value from 1 to 12 to indicate the center
     * height of this block. Returns zero if not a flow block.
     */
    public static int getFlowHeightFromState(BlockState state) {
        if (isFlowHeight(state)) {
            return Math.max(1, TerrainState.BLOCK_LEVELS_INT - state.get(BrocadeBlock.HEIGHT).intValue());
        } else {
            return 0;
        }
    }

    /**
     * Use for height blocks. Stores a value from 1 to 12 to indicate the center
     * height of this block
     */
    public static BlockState stateWithDiscreteFlowHeight(BlockState state, int value) {
        return state.with(BrocadeBlock.HEIGHT,
                Math.min(TerrainState.BLOCK_LEVELS_INT - 1, Math.max(0, TerrainState.BLOCK_LEVELS_INT - value)));
    }

    public static BlockState stateWithFlowHeight(BlockState state, float value) {
        return stateWithDiscreteFlowHeight(state, (int) Math.round(value * TerrainState.BLOCK_LEVELS_INT));
    }

    /**
     * Shorthand for {@link #adjustFillIfNeeded(World, BlockPos, Predicate)} with no
     * predicate.
     */
    public static BlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, long packedBasePos) {
        return adjustFillIfNeeded(worldObj, packedBasePos, null);
    }

    /**
     * Adds or removes a filler block if needed. Also replaces static filler blocks
     * with dynamic version. Returns the blockstate that was set if it was changed.
     * <p>
     * 
     * Optional predicate can limit what block states are updated. If provided, only
     * block states where predicate test returns true will be changed.
     * 
     */
    public static BlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, final long packedBasePos,
            Predicate<BlockState> filter) {
        final BlockState baseState = worldObj.getBlockState(packedBasePos);
        final Block baseBlock = baseState.getBlock();

        if (TerrainBlockHelper.isFlowHeight(baseState))
            return null;

        // Checks for non-displaceable block
        if (filter != null && !filter.test(baseState))
            return null;

        final int SHOULD_BE_AIR = -1;

        ISuperBlock fillBlock = null;

        BlockState update = null;

        int targetMeta = SHOULD_BE_AIR;

        /**
         * If space is occupied with a non-displaceable block, will be ignored.
         * Otherwise, possible target states: air, fill +1, fill +2
         * 
         * Should be fill +1 if block below is a heightblock and needs a fill >= 1;
         * Should be a fill +2 if block below is not a heightblock and block two below
         * needs a fill = 2; Otherwise should be air.
         */
        final long posBelow = PackedBlockPos.down(packedBasePos, 1);
        final BlockState stateBelow = worldObj.getBlockState(posBelow);

        if (isFlowHeight(stateBelow.getBlock()) && worldObj.terrainState(stateBelow, posBelow).topFillerNeeded() > 0) {
            targetMeta = 0;

            fillBlock = (ISuperBlock) TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateBelow.getBlock());
        } else {
            final long posTwoBelow = PackedBlockPos.down(packedBasePos, 2);
            final BlockState stateTwoBelow = worldObj.getBlockState(posTwoBelow);

            if ((isFlowHeight(stateTwoBelow.getBlock())
                    && worldObj.terrainState(stateTwoBelow, posTwoBelow).topFillerNeeded() == 2)) {
                targetMeta = 1;
                fillBlock = (ISuperBlock) TerrainBlockRegistry.TERRAIN_STATE_REGISTRY
                        .getFillerBlock(stateTwoBelow.getBlock());
            }
        }

        if (isFlowFiller(baseBlock)) {
            if (targetMeta == SHOULD_BE_AIR) {
                update = Blocks.AIR.getDefaultState();
                worldObj.setBlockState(packedBasePos, update);
            } else if (fillBlock != null
                    && (baseState.getValue(ISuperBlock.META) != targetMeta || baseBlock != fillBlock)) {
                update = ((Block) fillBlock).getDefaultState().withProperty(ISuperBlock.META, targetMeta);
                worldObj.setBlockState(packedBasePos, update);
            }
            // confirm filler needed and adjustIfEnabled/remove if needed
        } else if (targetMeta != SHOULD_BE_AIR && fillBlock != null) {
            update = ((Block) fillBlock).getDefaultState().withProperty(ISuperBlock.META, targetMeta);
            worldObj.setBlockState(packedBasePos, update);
        }

        return update;
    }

    /**
     * Returns true of geometry of flow block should be a full cube based on self
     * and neighboring flow blocks. Returns false if otherwise or if is not a flow
     * block.
     */
    public static boolean shouldBeFullCube(BlockState blockState, ISuperBlockAccess blockAccess, BlockPos pos) {
        return blockAccess.terrainState(blockState, pos).isFullCube();
    }

    /**
     * Use for filler blocks. Returns values from +1 to +2.
     */
    public static int getYOffsetFromState(BlockState state) {
        return state.get(BrocadeBlock.HEIGHT).intValue() + 1;
    }

    /**
     * Use for filler blocks. Stores values from -2 to -1 and +1 to +2.
     */
    public static BlockState stateWithYOffset(BlockState state, int value) {
        return state.with(BrocadeBlock.HEIGHT, Math.min(1, Math.max(0, value - 1)));
    }
}
