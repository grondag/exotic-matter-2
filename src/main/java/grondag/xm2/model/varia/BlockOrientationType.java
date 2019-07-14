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

package grondag.xm2.model.varia;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import grondag.xm2.api.model.ModelState;
import grondag.xm2.placement.BlockOrientationHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.EnumProperty;

public enum BlockOrientationType {
    NONE(null, (s, c) -> s, (b, m) -> {
    }),
    AXIS(BlockOrientationHandler.AXIS_PROP, BlockOrientationHandler::axisBlockState,
	    BlockOrientationHandler::axisModelState),
    FACE(BlockOrientationHandler.FACE_PROP, BlockOrientationHandler::faceBlockState,
	    BlockOrientationHandler::faceModelState),
    EDGE(BlockOrientationHandler.EDGE_PROP, BlockOrientationHandler::edgeBlockState,
	    BlockOrientationHandler::edgeModelState),
    CORNER(BlockOrientationHandler.CORNER_PROP, BlockOrientationHandler::cornerBlockState,
	    BlockOrientationHandler::cornerModelState);

    public final @Nullable EnumProperty<?> property;

    public final BiFunction<BlockState, ItemPlacementContext, BlockState> placementFunc;

    /**
     * Updates the model state from block state for orientation.
     */
    public final BiConsumer<BlockState, ModelState> stateFunc;

    private BlockOrientationType(EnumProperty<?> property,
	    BiFunction<BlockState, ItemPlacementContext, BlockState> placementFunc,
	    BiConsumer<BlockState, ModelState> stateFunc) {
	this.property = property;
	this.placementFunc = placementFunc;
	this.stateFunc = stateFunc;
    }
}
