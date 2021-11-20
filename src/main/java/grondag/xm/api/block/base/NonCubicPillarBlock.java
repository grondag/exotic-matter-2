/*
 * Copyright © Original Authors
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
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.block.base;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Handles waterlogging.
 */
@Experimental
public class NonCubicPillarBlock extends RotatedPillarBlock {
	public NonCubicPillarBlock(Properties settings) {
		super(settings);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		final FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		return defaultBlockState()
				.setValue(AXIS, ctx.getClickedFace().getAxis())
				.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
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
