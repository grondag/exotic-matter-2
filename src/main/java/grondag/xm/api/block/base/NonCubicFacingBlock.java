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

package grondag.xm.api.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.ApiStatus.Experimental;

/**
 * Handles waterlogging.
 */
@Experimental
public class NonCubicFacingBlock extends DirectionalBlock {

	public NonCubicFacingBlock(Properties settings, Direction defaultFace) {
		super(settings);
		registerDefaultState(defaultBlockState().setValue(FACING, defaultFace));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		final FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		return defaultBlockState()
				.setValue(FACING, ctx.getClickedFace().getOpposite())
				.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
		builder.add(BlockStateProperties.WATERLOGGED);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState blockState_1) {
		return true;
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction face, BlockState blockState2, LevelAccessor world, BlockPos pos, BlockPos pos2) {
		if (blockState.getValue(BlockStateProperties.WATERLOGGED)) {
			world.getLiquidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(blockState, face, blockState2, world, pos, pos2);
	}

	@Override
	public FluidState getFluidState(BlockState blockState) {
		return blockState.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}
}
