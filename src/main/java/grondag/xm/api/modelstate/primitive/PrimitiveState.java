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

package grondag.xm.api.modelstate.primitive;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

import grondag.fermion.orientation.api.CubeRotation;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.primitive.simple.Stair;

@Experimental
public interface PrimitiveState extends BaseModelState<PrimitiveState, MutablePrimitiveState> {
	SimplePrimitiveStateMutator AXIS_FROM_BLOCKSTATE = (modelState, blockState) -> {
		final Comparable<?> axis = blockState.getValues().get(RotatedPillarBlock.AXIS);

		if (axis != null) {
			modelState.orientationIndex(RotatedPillarBlock.AXIS.getValueClass().cast(axis).ordinal());
		}

		return modelState;
	};

	SimplePrimitiveStateMutator STAIRS_FROM_BLOCKSTATE = (modelState, blockState) -> {
		final Comparable<?> faceProp = blockState.getValues().get(StairBlock.FACING);
		final Comparable<?> half = blockState.getValues().get(StairBlock.HALF);
		final Comparable<?> shapeProp = blockState.getValues().get(StairBlock.SHAPE);

		if (faceProp != null && half != null && shapeProp != null) {
			final StairsShape shape = StairBlock.SHAPE.getValueClass().cast(shapeProp);
			Direction face = StairBlock.FACING.getValueClass().cast(faceProp);
			final boolean bottom = StairBlock.HALF.getValueClass().cast(half) == Half.BOTTOM;
			final boolean corner = shape != StairsShape.STRAIGHT;
			boolean inside = false;
			boolean left = false;

			switch (shape) {
				case INNER_LEFT:
					left = true;
				case INNER_RIGHT:
					inside = true;
					break;
				case OUTER_LEFT:
					left = true;
					break;
				default:
					break;
			}

			if (corner) {
				if (bottom) {
					if (left) {
						face = face.getCounterClockWise();
					}
				} else {
					if (!left) {
						face = face.getClockWise();
					}
				}
			}

			Stair.setCorner(corner, modelState);
			Stair.setInsideCorner(corner && inside, modelState);
			modelState.orientationIndex(CubeRotation.find(bottom ? Direction.DOWN : Direction.UP, face).ordinal());
		}

		return modelState;
	};
}
