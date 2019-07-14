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

package grondag.xm2.api.texture;

import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NEEDS_SPECIES;
import static grondag.xm2.model.state.ModelStateData.STATE_FLAG_NONE;

public enum TextureLayout {
    /**
     * Separate random tiles with naming convention base_j_i where i is 0-7 and j is
     * 0 or more.
     */
    SPLIT_X_8(STATE_FLAG_NONE),

    /**
     * Single square file with optional versions. If more than one version, file
     * names should have a 0-based _x suffix.
     */
    SIMPLE(STATE_FLAG_NONE),

    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the first
     * 13 textures out of every 16 are used for borders.
     */
    BORDER_13(STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SPECIES, 13),

    /**
     * Like BORDER_13 but with an extra texture 14 that to should be rendered if the
     * border is rendered in the solid render layer. It is IMPORTANT that texture 14
     * have a solid alpha channel - otherwise mipmap generation will be borked. The
     * solid face won't be used at all if rendering in a non-solid layer.
     */
    BORDER_14(STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SPECIES, 14),

    /**
     * Separate files with naming convention same as SPLIT_X_8 except only the start
     * 5 textures out of every 8. Files won't exist or will be blank for 5-7.
     */
    MASONRY_5(STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_MASONRY_JOIN | STATE_FLAG_NEEDS_SPECIES, 5),

    /**
     * Animated big textures stored as series of .jpg files
     */
    BIGTEX_ANIMATED(STATE_FLAG_NONE),

    /**
     * Compact border texture on format, typically with multiple variants. Each
     * quadrant of the texture represents one quadrant of a face that can be
     * connected. All are present on same image. Each quadrant must be able to
     * connect with other quadrants in any (connecting) rotation or texture
     * variation.
     * <p>
     * 
     * Follows same naming convention as {@link #SIMPLE}.
     */
    QUADRANT_CONNECTED(STATE_FLAG_NEEDS_CORNER_JOIN | STATE_FLAG_NEEDS_SPECIES);

    private TextureLayout(int stateFlags) {
	this(stateFlags, 1);
    }

    private TextureLayout(int stateFlags, int textureCount) {
	this.modelStateFlag = stateFlags;
	this.textureCount = textureCount;
    }

    /**
     * identifies the world state needed to drive texture random rotation/selection
     */
    public final int modelStateFlag;

    /**
     * Textures per variant in this layout.
     */
    public final int textureCount;
}
