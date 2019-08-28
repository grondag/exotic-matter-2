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
package grondag.xm.api.modelstate;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL)
public class ModelStateFlags {
    private ModelStateFlags() {}
    /**
     * For readability.
     */
    public static final int NONE = 0;

    /*
     * Enables lazy derivation - set after derivation is complete. NB - check logic
     * assumes that ALL bits are zero for simplicity.
     */
    public static final int IS_POPULATED = 1;

    /**
     * Applies to block-type states. True if is a block type state and requires full
     * join state.
     */
    public static final int CORNER_JOIN = IS_POPULATED << 1;

    /**
     * Applies to block-type states. True if is a block type state and requires full
     * join state.
     */
    public static final int SIMPLE_JOIN = CORNER_JOIN << 1;

    /**
     * Applies to block-type states. True if is a block type state and requires
     * masonry join info.
     */
    public static final int MASONRY_JOIN = SIMPLE_JOIN << 1;

    /**
     * True if position (big-tex) world state is needed. Applies for block and flow
     * state formats.
     */
    public static final int POSITION = MASONRY_JOIN << 1;

    public static final int BLOCK_SPECIES = POSITION << 1;

    public static final int TEXTURE_ROTATION = BLOCK_SPECIES << 1;

}
