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
package grondag.xm.api.modelstate.primitive;

import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.block.PillarBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.util.math.Direction;

import grondag.fermion.orientation.api.CubeRotation;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.primitive.simple.Stair;

@Experimental
public interface PrimitiveState extends BaseModelState<PrimitiveState, MutablePrimitiveState>  {
	SimplePrimitiveStateMutator AXIS_FROM_BLOCKSTATE = (modelState, blockState) -> {
		final Comparable<?> axis = blockState.getEntries().get(PillarBlock.AXIS);
		if (axis != null) {
			modelState.orientationIndex(PillarBlock.AXIS.getType().cast(axis).ordinal());
		}
		return modelState;
	};

	SimplePrimitiveStateMutator STAIRS_FROM_BLOCKSTATE = (modelState, blockState) -> {
		final Comparable<?> faceProp = blockState.getEntries().get(StairsBlock.FACING);
		final Comparable<?> half = blockState.getEntries().get(StairsBlock.HALF);
		final Comparable<?> shapeProp = blockState.getEntries().get(StairsBlock.SHAPE);

		if (faceProp != null && half != null && shapeProp != null) {
			final StairShape shape = StairsBlock.SHAPE.getType().cast(shapeProp);
			Direction face = StairsBlock.FACING.getType().cast(faceProp);
			final boolean bottom = StairsBlock.HALF.getType().cast(half) == BlockHalf.BOTTOM;
			final boolean corner = shape != StairShape.STRAIGHT;
			boolean inside = false;
			boolean left = false;

			switch(shape) {
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

			if(corner) {
				if(bottom) {
					if(left) {
						face = face.rotateYCounterclockwise();
					}
				} else {
					if(!left) {
						face = face.rotateYClockwise();
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
