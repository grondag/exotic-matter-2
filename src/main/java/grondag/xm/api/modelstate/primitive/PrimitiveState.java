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
import grondag.fermion.orientation.api.CubeRotation;
import grondag.xm.api.modelstate.base.BaseModelState;
import grondag.xm.api.primitive.simple.Stair;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

@Experimental
public interface PrimitiveState extends BaseModelState<PrimitiveState, MutablePrimitiveState>  {
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
						face = face.getCounterClockWise();
					}
				} else {
					if(!left) {
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
