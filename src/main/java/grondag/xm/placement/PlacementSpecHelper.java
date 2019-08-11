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
package grondag.xm.placement;

import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.block.XmStackHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Knows how to get the appropriate placement builder for a given stack.
 */
public class PlacementSpecHelper {
    /**
     * Instantiates the appropriate placement builder object if the object in the
     * stack is placed in world given current player/world context.<br>
     * <br>
     * 
     * Assumes determination that block should be placed is already made. (Not
     * clicking in mid air without floating selection, for example.)
     */
    public static IPlacementSpec placementBuilder(PlayerEntity player, PlacementPosition pPos, ItemStack stack) {
        ItemStack placedStack = stack.copy();

        BlockOrientationHandler.configureStackForPlacement(placedStack, player, pPos);

        PlacementItem item = PlacementItem.getPlacementItem(stack);

        if (item == null)
            return null;

        // non-virtual items should always be single block placements
        if (!item.isVirtual(placedStack)) {
            return new SingleBlockPlacementSpec(placedStack, player, pPos);
        }

        switch (item.getTargetMode(placedStack)) {
        // meaning of following three types depends...
        // if we are placing individual blocks in a multiblock (cubic) region
        // then the geometry of the region is defined by the builder.
        // if we are placing a CSG multiblock, then the geometry
        // of the placement is part of the GSC shape itself, as configured in model
        // state.
        case COMPLETE_REGION:
        case FILL_REGION:
        case HOLLOW_REGION:
            @SuppressWarnings("rawtypes")
            PrimitiveModelState modelState = XmStackHelper.getStackModelState(placedStack);
            if (modelState != null && modelState.primitive().isMultiBlock()) {
                return new CSGPlacementSpec(placedStack, player, pPos);
            } else {
                if (!item.isFixedRegionEnabled(placedStack) && item.getRegionSize(placedStack, false).equals(new BlockPos(1, 1, 1))) {
                    return new SingleBlockPlacementSpec(placedStack, player, pPos);
                } else {
                    return new CuboidPlacementSpec(placedStack, player, pPos);
                }
            }

        case MATCH_CLICKED_BLOCK:
            return new PredicatePlacementSpec(placedStack, player, pPos);

        case ON_CLICKED_SURFACE:
            return new SurfacePlacementSpec(placedStack, player, pPos);

        case ON_CLICKED_FACE:
        default:
            return new SingleBlockPlacementSpec(placedStack, player, pPos);
        }
    }
}
