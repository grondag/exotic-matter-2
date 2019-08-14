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

package grondag.xm.model.varia;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.placement.BlockOrientationHandler;
import grondag.xm.api.connect.model.BlockEdge;
import grondag.xm.api.connect.model.BlockEdgeSided;
import grondag.xm.api.connect.model.BlockCorner;
import net.minecraft.util.math.Direction;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

@SuppressWarnings("rawtypes")
public enum BlockOrientationType {
    NONE(null, (s, c) -> s, (b, m) -> {}),
    AXIS(Direction.Axis.class, BlockOrientationHandler::axisBlockState, BlockOrientationHandler::axisModelState),
    FACE(Direction.class, BlockOrientationHandler::faceBlockState, BlockOrientationHandler::faceModelState),
    EDGE(BlockEdge.class, BlockOrientationHandler::edgeBlockState, BlockOrientationHandler::edgeModelState),
    EDGE_SIDED(BlockEdgeSided.class, BlockOrientationHandler::edgeBlockState, BlockOrientationHandler::edgeModelState),
    CORNER(BlockCorner.class, BlockOrientationHandler::cornerBlockState, BlockOrientationHandler::cornerModelState);

    public final @Nullable Class<? extends Enum> enumClass;

    public final BiFunction<BlockState, ItemPlacementContext, BlockState> placementFunc;

    /**
     * Updates the model state from block state for orientation.
     */
    public final BiConsumer<BlockState, PrimitiveModelState.Mutable> stateFunc;

    private BlockOrientationType(Class<? extends Enum> enumClass, BiFunction<BlockState, ItemPlacementContext, BlockState> placementFunc,
            BiConsumer<BlockState, PrimitiveModelState.Mutable> stateFunc) {
        this.enumClass = enumClass;
        this.placementFunc = placementFunc;
        this.stateFunc = stateFunc;
    }
}
