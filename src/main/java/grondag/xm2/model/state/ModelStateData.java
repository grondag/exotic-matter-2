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

package grondag.xm2.model.state;

import grondag.xm2.api.connect.world.ModelStateFunction;
import grondag.xm2.block.XmBlockStateAccess;

public class ModelStateData {

    /**
     * Use this as factory for model state block tests that DON'T need to refresh
     * from world.
     */
    public static final ModelStateFunction TEST_GETTER_STATIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p,
            false);

    /**
     * Use this as factory for model state block tests that DO need to refresh from
     * world.
     */
    public static final ModelStateFunction TEST_GETTER_DYNAMIC = (w, b, p) -> XmBlockStateAccess.modelState(b, w, p,
            true);

    /**
     * For readability.
     */
    public static final int STATE_FLAG_NONE = 0;

    /*
     * Enables lazy derivation - set after derivation is complete. NB - check logic
     * assumes that ALL bits are zero for simplicity.
     */
    public static final int STATE_FLAG_IS_POPULATED = 1;

    /**
     * Applies to block-type states. True if is a block type state and requires full
     * join state.
     */
    public static final int STATE_FLAG_NEEDS_CORNER_JOIN = STATE_FLAG_IS_POPULATED << 1;

    /**
     * Applies to block-type states. True if is a block type state and requires full
     * join state.
     */
    public static final int STATE_FLAG_NEEDS_SIMPLE_JOIN = STATE_FLAG_NEEDS_CORNER_JOIN << 1;

    /**
     * Applies to block-type states. True if is a block type state and requires
     * masonry join info.
     */
    public static final int STATE_FLAG_NEEDS_MASONRY_JOIN = STATE_FLAG_NEEDS_SIMPLE_JOIN << 1;

    /**
     * True if position (big-tex) world state is needed. Applies for block and flow
     * state formats.
     */
    public static final int STATE_FLAG_NEEDS_POS = STATE_FLAG_NEEDS_MASONRY_JOIN << 1;

    public static final int STATE_FLAG_NEEDS_SPECIES = STATE_FLAG_NEEDS_POS << 1;

    public static final int STATE_FLAG_HAS_AXIS = STATE_FLAG_NEEDS_SPECIES << 1;

    public static final int STATE_FLAG_NEEDS_TEXTURE_ROTATION = STATE_FLAG_HAS_AXIS << 1;

    public static final int STATE_FLAG_HAS_AXIS_ORIENTATION = STATE_FLAG_NEEDS_TEXTURE_ROTATION << 1;

    /**
     * Set if shape can be rotated around an axis. Only applies to block models;
     * multiblock models manage this situationally.
     */
    public static final int STATE_FLAG_HAS_AXIS_ROTATION = STATE_FLAG_HAS_AXIS_ORIENTATION << 1;

    // hide constructor
    private ModelStateData() {
        super();
    }
}
