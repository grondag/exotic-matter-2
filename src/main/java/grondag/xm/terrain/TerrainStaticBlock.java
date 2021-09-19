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

import org.jetbrains.annotations.ApiStatus.Internal;
import grondag.xm.api.modelstate.ModelState;
import grondag.xm.api.modelstate.MutableModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Internal
public class TerrainStaticBlock extends Block implements IHotBlock {

	public TerrainStaticBlock(Properties blockSettings, ModelState defaultModelState, BlockEntityType<?> blockEntityType, boolean isFiller) {
		super(blockSettings); //, adjustShape(defaultModelState, isFiller), blockEntityType);
	}

	// PERF: sucks
	@SuppressWarnings("unused")
	private static MutableModelState adjustShape(ModelState stateIn, boolean isFiller) {
		final MutableModelState result = isFiller ? TerrainSurface.FILLER.newState() : TerrainSurface.HEIGHT.newState();
		result.copyFrom(stateIn);
		result.setStatic(true);
		return result;
	}

	//  TODO: remove or restore
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

	/**
	 * Prevent neighboring dynamic blocks from updating geometry by making them
	 * static.
	 */
	@Override
	public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		TerrainDynamicBlock.freezeNeighbors(world, pos, state);
		super.playerWillDestroy(world, pos, state, player);
	}

	/**
	 * Prevent neighboring dynamic blocks from updating geometry by making them
	 * static.
	 */
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		TerrainDynamicBlock.freezeNeighbors(worldIn, pos, state);
	}

	/**
	 * Convert this block to a dynamic version of itself if one is known.
	 */
	public void makeDynamic(BlockState state, Level world, BlockPos pos) {
		final BlockState newState = dynamicState(state, world, pos);
		if (newState != state) {
			world.setBlock(pos, newState, 3);
		}
	}

	/**
	 * Returns dynamic version of self if one is known. Otherwise returns self.
	 */
	public BlockState dynamicState(BlockState state, BlockGetter world, BlockPos pos) {
		final Block dynamicVersion = TerrainBlockRegistry.TERRAIN_STATE_REGISTRY.getDynamicBlock(this);
		if (dynamicVersion == null || state.getBlock() != this)
			return state;
		// TODO: transfer heat block state?
		return dynamicVersion.defaultBlockState().setValue(TerrainBlock.TERRAIN_TYPE, state.getValue(TerrainBlock.TERRAIN_TYPE));
	}

	public void setModelState(Level world, BlockPos pos, MutableModelState myModelState) {
		// TODO Auto-generated method stub

	}
}
