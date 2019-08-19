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
package grondag.xm.relics.placement;

import javax.annotation.Nullable;

import grondag.fermion.world.WorldTaskManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class PlacementResult {
    public static final PlacementResult EMPTY_RESULT_STOP = new PlacementResult(null, PlacementEvent.NO_OPERATION_CONTINUE, null);
    public static final PlacementResult EMPTY_RESULT_CONTINUE = new PlacementResult(null, PlacementEvent.NO_OPERATION_CONTINUE, null);

    private final PlacementEvent event;
    private final BlockPos blockPos;
    private final IPlacementSpec builder;

    /**
     * @param event    Identifies state changes and subsequent event processing that
     *                 should occur with this result.
     * @param blockPos If the event associated with this result requires a BlockPos
     *                 for state changes, the BlockPos that should be used. Null
     *                 otherwise.
     */
    public PlacementResult(@Nullable BlockPos blockPos, PlacementEvent event, @Nullable IPlacementSpec builder) {
        this.blockPos = blockPos;
        this.event = event;
        this.builder = builder;
    }

    public IPlacementSpec builder() {
        return this.builder;
    }

    /**
     * If true, the user input event (mouse click, usually) that caused this result
     * should continue to be processed by other event handlers. True also implies
     * that {@link #apply(ItemStack, EntityPlayer)} will have no effect.
     */
    public boolean shouldInputEventsContinue() {
        return this.event == PlacementEvent.NO_OPERATION_CONTINUE;
    }

    /**
     * True if all block changes in this result are for block removal and there are
     * no block placements.
     */
    public boolean isExcavationOnly() {
        return this.event.isExcavation;
    }

    public void apply(ItemStack stackIn, PlayerEntity player) {
        if (!PlacementItem.isPlacementItem(stackIn))
            return;

        PlacementItem item = (PlacementItem) stackIn.getItem();

        switch (this.event) {

        case START_PLACEMENT_REGION:
            item.fixedRegionStart(stackIn, blockPos, false);
            break;

        case CANCEL_PLACEMENT_REGION:
            item.fixedRegionCancel(stackIn);
            break;

        case PLACE:
        case EXCAVATE:
            if (!player.world.isClient) {
                if (this.builder.validate()) {
                    // Turn off fixed region when completing a successful fixed region
                    // Did this because did not want to require two clicks to place a fixed region
                    // and no point in leaving the region there once placed.
                    if (item.isFixedRegionEnabled(stackIn))
                        item.setFixedRegionEnabled(stackIn, false);

                    if (item.isVirtual(stackIn)) {
                        WorldTaskManager.enqueue(this.builder.worldTask((ServerPlayerEntity) player));
                    } else {
                        // non-virtual placement operations happen immediately
                        // such actions are typically single blocks
                        this.builder.worldTask((ServerPlayerEntity) player).getAsBoolean();
                    }
                }
            }
            break;

        case UNDO_PLACEMENT:
            // TODO
            break;

        case NO_OPERATION_STOP:
        case NO_OPERATION_CONTINUE:
        default:
            // NOOP
            break;
        }
    }
}
