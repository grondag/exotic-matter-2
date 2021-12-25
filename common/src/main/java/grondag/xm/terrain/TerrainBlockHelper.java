/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package grondag.xm.terrain;

import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.terrain.TerrainModelState;

@Internal
public class TerrainBlockHelper {
	/**
	 * Convenience method to check for flow block.
	 */
	public static boolean isFlowBlock(BlockState state) {
		return terrainType(state) != null;
	}

	private static TerrainType[] HEIGHTS = {
		TerrainType.HEIGHT_1, TerrainType.HEIGHT_2, TerrainType.HEIGHT_3, TerrainType.HEIGHT_4,
		TerrainType.HEIGHT_5, TerrainType.HEIGHT_6, TerrainType.HEIGHT_7, TerrainType.HEIGHT_8,
		TerrainType.HEIGHT_9, TerrainType.HEIGHT_10, TerrainType.HEIGHT_11, TerrainType.HEIGHT_12
	};

	@Nullable
	public static TerrainType terrainType(BlockState state) {
		final Comparable<?> val = state.getValues().get(TerrainBlock.TERRAIN_TYPE);
		return val == null ? null : TerrainBlock.TERRAIN_TYPE.getValueClass().cast(val);
	}

	/**
	 * Convenience method to check for filler block.
	 */
	public static boolean isFlowFiller(BlockState state) {
		final TerrainType val = terrainType(state);
		return val != null && val.isFiller;
	}

	/**
	 * Convenience method to check for height block.
	 */
	public static boolean isFlowHeight(BlockState state) {
		final TerrainType val = terrainType(state);
		return val != null && val.isHeight;
	}

	public static int getHotness(BlockState state) {
		final Comparable<?> val = state.getValues().get(TerrainBlock.HEAT);
		return val == null ? 0 : TerrainBlock.HEAT.getValueClass().cast(val);
	}

	/**
	 * Use for height blocks. Returns a value from 1 to 12 to indicate the center
	 * height of this block. Returns zero if not a flow block.
	 */
	public static int getFlowHeightFromState(BlockState state) {
		final TerrainType val = terrainType(state);
		return val == null ? 0 : val.height;
	}

	/**
	 * Use for height blocks. Stores a value from 1 to 12 to indicate the center
	 * height of this block
	 */
	public static BlockState stateWithDiscreteFlowHeight(BlockState state, int value) {
		return state.setValue(TerrainBlock.TERRAIN_TYPE, HEIGHTS[Mth.clamp(value - 1, 0, 11)]);
	}

	public static BlockState stateWithFlowHeight(BlockState state, float value) {
		return stateWithDiscreteFlowHeight(state, Math.round(value * TerrainState.BLOCK_LEVELS_INT));
	}

	/**
	 * Shorthand for {@link #adjustFillIfNeeded(Level, BlockPos, Predicate)} with no
	 * predicate.
	 */
	public static BlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, long packedBasePos) {
		return adjustFillIfNeeded(worldObj, packedBasePos, null);
	}

	/**
	 * Adds or removes a filler block if needed. Also replaces static filler blocks
	 * with dynamic version. Returns the blockstate that was set if it was changed.
	 *
	 * <p>Optional predicate can limit what block states are updated. If provided, only
	 * block states where predicate test returns true will be changed.
	 *
	 */
	public static BlockState adjustFillIfNeeded(TerrainWorldAdapter worldObj, final long packedBasePos, Predicate<BlockState> filter) {
		final BlockState baseState = worldObj.getBlockState(packedBasePos);
		final Block baseBlock = baseState.getBlock();

		if (TerrainBlockHelper.isFlowHeight(baseState)) {
			return null;
		}

		// Checks for non-displaceable block
		if (filter != null && !filter.test(baseState)) {
			return null;
		}

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
		final long posBelow = BlockPos.offset(packedBasePos, 0, -1, 0);
		final BlockState stateBelow = worldObj.getBlockState(posBelow);

		if (isFlowHeight(stateBelow) && worldObj.terrainState(stateBelow, posBelow).topFillerNeeded() > 0) {
			targetFill = 1;

			fillBlock = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateBelow.getBlock());
		} else {
			final long posTwoBelow = BlockPos.offset(packedBasePos, 0, -2, 0);
			final BlockState stateTwoBelow = worldObj.getBlockState(posTwoBelow);

			if ((isFlowHeight(stateTwoBelow) && worldObj.terrainState(stateTwoBelow, posTwoBelow).topFillerNeeded() == 2)) {
				targetFill = 2;
				fillBlock = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getFillerBlock(stateTwoBelow.getBlock());
			}
		}

		if (isFlowFiller(baseState)) {
			if (targetFill == SHOULD_BE_AIR) {
				update = Blocks.AIR.defaultBlockState();
				worldObj.setBlockState(packedBasePos, update);
			} else if (fillBlock != null && (getYOffsetFromState(baseState) != targetFill || baseBlock != fillBlock)) {
				update = stateWithYOffset(fillBlock.defaultBlockState(), targetFill);
				worldObj.setBlockState(packedBasePos, update);
			}
		} else if (targetFill != SHOULD_BE_AIR && fillBlock != null) {
			// confirm filler needed and adjustIfEnabled/remove if needed
			update = stateWithYOffset(fillBlock.defaultBlockState(), targetFill);
			worldObj.setBlockState(packedBasePos, update);
		}

		return update;
	}

	/**
	 * Returns true of geometry of flow block should be a full cube based on self
	 * and neighboring flow blocks. Returns false if otherwise or if is not a flow
	 * block.
	 */
	public static boolean shouldBeFullCube(BlockState blockState, BlockGetter blockAccess, BlockPos pos) {
		if (isFlowBlock(blockState)) {
			final TerrainModelState.Mutable mState = (TerrainModelState.Mutable) XmBlockState.modelState(blockState, blockAccess, pos, true);
			final boolean result = mState.getTerrainState().isFullCube();
			mState.release();
			return result;
		} else {
			return false;
		}
	}

	public static @Nullable TerrainState terrainState(BlockState blockState, BlockGetter blockAccess, BlockPos pos) {
		if (isFlowBlock(blockState)) {
			final TerrainModelState.Mutable mState = (TerrainModelState.Mutable) XmBlockState.modelState(blockState, blockAccess, pos, true);
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
		final TerrainType val = terrainType(state);
		return val == null ? 0 : val.fillOffset;
	}

	/**
	 * Use for filler blocks. Stores value +1 and +2.
	 */
	public static BlockState stateWithYOffset(BlockState state, int value) {
		switch (value) {
			case 1:
				return state.setValue(TerrainBlock.TERRAIN_TYPE, TerrainType.FILL_UP_ONE);
			case 2:
				return state.setValue(TerrainBlock.TERRAIN_TYPE, TerrainType.FILL_UP_TWO);
			default:
				return state;
		}
	}
}
