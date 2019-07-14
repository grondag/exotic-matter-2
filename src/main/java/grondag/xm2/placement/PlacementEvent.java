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

package grondag.xm2.placement;

public enum PlacementEvent {
    /**
     * Nothing happens, and click processing should continue.
     */
    NO_OPERATION_CONTINUE(false, false, false),
    /**
     * Nothing happens, and we eat the click event.
     */
    NO_OPERATION_STOP(false, false, false),

    /**
     * Place blocks, eat the event. If fixed region in progress, completes fixed
     * region. Cancel any previous operations in progress.
     */
    PLACE(true, false, false),

    /*
     * Start a new placement region selection operation. Cancel any previous
     * operations in progress. BlockPos is the start position that should be part of
     * the region. Eat the event.
     */
    START_PLACEMENT_REGION(false, false, false),

    /*
     * Cancel placement region selection operation in progress. Eat the event.
     */
    CANCEL_PLACEMENT_REGION(false, false, false),

    /*
     * Remove blocks. Eat the event. If fixed region in progress, completes region.
     * Block position identifies the block that was clicked.
     */
    EXCAVATE(false, true, false),

    /**
     * Undo block placement. Block position identifies the block that was clicked.
     * Cancel any previous operations in progress. Eat the event.
     */
    UNDO_PLACEMENT(false, false, false);

    public final boolean isExcavation;
    public final boolean isPlacement;
    public final boolean isSetRegion;

    private PlacementEvent(Boolean isPlacement, boolean isExcavation, boolean isSetRegion) {
	this.isPlacement = isPlacement;
	this.isExcavation = isExcavation;
	this.isSetRegion = isSetRegion;
    }
}
