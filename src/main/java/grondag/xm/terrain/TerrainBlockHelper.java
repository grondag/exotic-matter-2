/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.xm.terrain;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import grondag.fermion.position.PackedBlockPos;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.terrain.TerrainModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class TerrainBlockHelper {
    /**
     * Convenience method to check for flow block.
     */
    public static boolean isFlowBlock(BlockState state) {
        return terrainType(state) != null;
    }

    private static TerrainType[] HEIGHTS = { TerrainType.HEIGHT_1, TerrainType.HEIGHT_2, TerrainType.HEIGHT_3, TerrainType.HEIGHT_4, TerrainType.HEIGHT_5,
            TerrainType.HEIGHT_6, TerrainType.HEIGHT_7, TerrainType.HEIGHT_8, TerrainType.HEIGHT_9, TerrainType.HEIGHT_10, TerrainType.HEIGHT_11,
            TerrainType.HEIGHT_12 };

    @Nullable
    public static TerrainType terrainType(BlockState state) {
        Comparable<?> val = state.getEntries().get(TerrainBlock.TERRAIN_TYPE);
        return val == null ? null : TerrainBlock.TERRAIN_TYPE.getValueType().cast(val);
    }

    /**
     * Convenience method to check for filler block.
     */
    public static boolean isFlowFiller(BlockState state) {
        TerrainType val = terrainType(state);
        return val != null && val.isFiller;
    }

    /**
     * Convenience method to check for height block.
     */
    public static boolean isFlowHeight(BlockState state) {
        TerrainType val = terrainType(state);
        return val != null && val.isHeight;
    }

    public static int getHotness(BlockState state) {
        Comparable<?> val = state.getEntries().get(TerrainBlock.HEAT);
        return val == null ? 0 : TerrainBlock.HEAT.getValueType().cast(val);
    }

    /**
     * Use for height blocks. Returns a value from 1 to 12 to indicate the center
     * height of this block. Returns zero if not a flow block.
     */
    public static int getFlowHeightFromState(BlockState state) {
        TerrainType val = terrainType(state);
        return val == null ? 0 : val.height;
    }

    /**
     * Use for height blocks. Stores a value from 1 to 12 to indicate the center
     * height of this block
     */
    public static BlockState stateWithDiscreteFlowHeight(BlockState state, int value) {
        return state.with(TerrainBlock.TERRAIN_TYPE, HEIGHTS[MathHelper.clamp(value - 1, 0, 11)]);
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
    public static BlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, final long packedBasePos, Predicate<BlockState> filter) {
        final BlockState baseState = worldObj.getBlockState(packedBasePos);
        final Block baseBlock = baseState.getBlock();

        if (TerrainBlockHelper.isFlowHeight(baseState))
            return null;

        // Checks for non-displaceable block
        if (filter != null && !filter.test(baseState))
            return null;

        final int SHOULD_BE_AIR = -1;

        Block fillBlock = null;

        BlockState update = null;

        int targetFill = SHOULD_BE_AIR;

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

        if (isFlowHeight(stateBelow) && worldObj.terrainState(stateBelow, posBelow).topFillerNeeded() > 0) {
            targetFill = 1;

            fillBlock = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateBelow.getBlock());
        } else {
            final long posTwoBelow = PackedBlockPos.down(packedBasePos, 2);
            final BlockState stateTwoBelow = worldObj.getBlockState(posTwoBelow);

            if ((isFlowHeight(stateTwoBelow) && worldObj.terrainState(stateTwoBelow, posTwoBelow).topFillerNeeded() == 2)) {
                targetFill = 2;
                fillBlock = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateTwoBelow.getBlock());
            }
        }

        if (isFlowFiller(baseState)) {
            if (targetFill == SHOULD_BE_AIR) {
                update = Blocks.AIR.getDefaultState();
                worldObj.setBlockState(packedBasePos, update);
            } else if (fillBlock != null && (getYOffsetFromState(baseState) != targetFill || baseBlock != fillBlock)) {
                update = stateWithYOffset(((Block) fillBlock).getDefaultState(), targetFill);
                worldObj.setBlockState(packedBasePos, update);
            }
            // confirm filler needed and adjustIfEnabled/remove if needed
        } else if (targetFill != SHOULD_BE_AIR && fillBlock != null) {
            update = stateWithYOffset(((Block) fillBlock).getDefaultState(), targetFill);
            worldObj.setBlockState(packedBasePos, update);
        }

        return update;
    }

    /**
     * Returns true of geometry of flow block should be a full cube based on self
     * and neighboring flow blocks. Returns false if otherwise or if is not a flow
     * block.
     */
    public static boolean shouldBeFullCube(BlockState blockState, BlockView blockAccess, BlockPos pos) {
        if (isFlowBlock(blockState)) {
            TerrainModelState.Mutable mState = (TerrainModelState.Mutable)XmBlockState.modelState(blockState, blockAccess, pos, true);
            final boolean result = mState.getTerrainState().isFullCube();
            mState.release();
            return result;
        } else {
            return false;
        }
    }

    public static @Nullable TerrainState terrainState(BlockState blockState, BlockView blockAccess, BlockPos pos) {
        if (isFlowBlock(blockState)) {
            TerrainModelState.Mutable mState = (TerrainModelState.Mutable)XmBlockState.modelState(blockState, blockAccess, pos, true);
            final TerrainState result = mState.getTerrainState();
            mState.release();
            return result;
        } else {
            return null;
        }
    }
    
    /**
     * Use for filler blocks. Returns values from +1 to +2, or zero if not a filler.
     */
    public static int getYOffsetFromState(BlockState state) {
        TerrainType val = terrainType(state);
        return val == null ? 0 : val.fillOffset;
    }

    /**
     * Use for filler blocks. Stores value +1 and +2.
     */
    public static BlockState stateWithYOffset(BlockState state, int value) {
        switch (value) {
        case 1:
            return state.with(TerrainBlock.TERRAIN_TYPE, TerrainType.FILL_UP_ONE);
        case 2:
            return state.with(TerrainBlock.TERRAIN_TYPE, TerrainType.FILL_UP_TWO);
        default:
            return state;
        }
    }
}
