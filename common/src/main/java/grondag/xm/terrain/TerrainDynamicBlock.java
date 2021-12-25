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

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.terrain.TerrainModelState;

@Internal
public class TerrainDynamicBlock extends TerrainBlock {
	public TerrainDynamicBlock(Properties blockSettings, ModelState defaultModelState, boolean isFiller) {
		super(blockSettings, adjustShape(defaultModelState, isFiller));
	}

	// PERF: sucks
	private static TerrainModelState.Mutable adjustShape(ModelState stateIn, boolean isFiller) {
		final TerrainModelState.Mutable result = isFiller ? TerrainSurface.FILLER.newState() : TerrainSurface.HEIGHT.newState();
		result.copyFrom(stateIn);
		result.setStatic(false);
		return result;
	}

	/**
	 * Convert this block to a static version of itself if a static version was
	 * given.
	 */
	public void makeStatic(BlockState state, Level world, BlockPos pos) {
		final TerrainStaticBlock staticVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getStaticBlock(this);

		if (staticVersion == null || state.getBlock() != this) {
			return;
		}

		final MutableModelState myModelState = XmBlockState.modelState(state, world, pos, true);
		myModelState.setStatic(true);
		// TODO: transfer heat block state?
		world.setBlock(pos, staticVersion.defaultBlockState().setValue(TerrainBlock.TERRAIN_TYPE, state.getValue(TerrainBlock.TERRAIN_TYPE)), 7);
		staticVersion.setModelState(world, pos, myModelState);
	}

	// TODO: restore or remove
	//    @Override
	//    public int quantityDropped(ExtendedBlockView world, BlockPos pos, BlockState state) {
	//        double volume = 0;
	//        ISuperModelState modelState = SuperBlockWorldAccess.access(world).computeModelState(this, state, pos, true);
	//        for (BoundingBox box : modelState.getShape().meshFactory().collisionHandler().getCollisionBoxes(modelState)) {
	//            volume += Useful.volumeAABB(box);
	//        }
	//
	//        return (int) Math.min(9, volume * 9);
	//    }

	// TODO: restore or remove
	//    @Override
	//    public boolean isReplaceable(ExtendedBlockView worldIn, BlockPos pos) {
	//        return SuperBlockWorldAccess.access(worldIn).terrainState(pos).isEmpty();
	//    }

	// TODO: restore or remove
	//    @Override
	//    public boolean isAir(BlockState state, ExtendedBlockView world, BlockPos pos) {
	//        return SuperBlockWorldAccess.access(world).terrainState(pos).isEmpty();
	//    }

	@Override
	public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		TerrainDynamicBlock.freezeNeighbors(world, pos, state);
		super.playerWillDestroy(world, pos, state, player);
	}

	/**
	 * Looks for nearby dynamic blocks that might depend on this block for height
	 * state and converts them to static blocks if possible.
	 */
	public static void freezeNeighbors(Level worldIn, BlockPos pos, BlockState state) {
		// only height blocks affect neighbors
		if (!TerrainBlockHelper.isFlowHeight(state)) {
			return;
		}

		BlockState targetState;
		Block targetBlock;

		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				for (int y = -4; y <= 4; y++) {
					//                    if(!(x == 0 && y == 0 && z == 0))
					{
						final BlockPos targetPos = pos.offset(x, y, z);
						targetState = worldIn.getBlockState(targetPos);
						targetBlock = targetState.getBlock();

						if (targetBlock instanceof TerrainDynamicBlock) {
							((TerrainDynamicBlock) targetBlock).makeStatic(targetState, worldIn, targetPos);
						}
					}
				}
			}
		}
	}
}
