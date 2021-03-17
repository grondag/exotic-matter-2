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
package grondag.xm.api.texture;

import static grondag.xm.api.modelstate.ModelStateFlags.BLOCK_SPECIES;
import static grondag.xm.api.modelstate.ModelStateFlags.CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.NONE;
import static grondag.xm.api.modelstate.ModelStateFlags.SIMPLE_JOIN;

import org.jetbrains.annotations.ApiStatus.Experimental;

//TODO: docs and remove references to layouts - that's part of layout map and name function now
@Experimental
public enum TextureLayout {
	/**
	 * Separate random tiles with naming convention base_j_i where i is 0-7 and j is
	 * 0 or more.
	 *
	 * Use {@code SIMPLE} instead.  This should be a layout map and will be removed.
	 *
	 * TODO: convert to layout map, remove
	 */
	@Deprecated
	SPLIT_X_8(NONE),

	/**
	 * Single square file with optional versions. If more than one version, file
	 * names should have a 0-based _x suffix.
	 */
	SIMPLE(NONE),

	/**
	 * Separate files with naming convention same as SPLIT_X_8 except only the first
	 * 13 textures out of every 16 are used for borders.
	 */
	BORDER_13(CORNER_JOIN | BLOCK_SPECIES, 13),

	/**
	 * Like BORDER_13 but with an extra texture 14 that to should be rendered if the
	 * border is rendered in the solid render layer. It is IMPORTANT that texture 14
	 * have a solid alpha channel - otherwise mipmap generation will be borked. The
	 * solid face won't be used at all if rendering in a non-solid layer.
	 */
	BORDER_14(CORNER_JOIN | BLOCK_SPECIES, 14),

	/**
	 * Separate files with naming convention same as SPLIT_X_8 except only the start
	 * 5 textures out of every 8. Files won't exist or will be blank for 5-7.
	 */
	MASONRY_5(CORNER_JOIN | MASONRY_JOIN | BLOCK_SPECIES, 5),

	/**
	 * Animated big textures stored as series of .jpg files.
	 *
	 * Holdover from 1.12 - not yet clear if will be implemented or removed.
	 */
	@Deprecated
	BIGTEX_ANIMATED(NONE),

	/**
	 * Compact connected texture format, optionally with multiple variants. Each
	 * quadrant of the texture represents one quadrant of a face that can be
	 * connected. All are present on same image. Each quadrant must be able to
	 * connect with other quadrants in any (connecting) rotation or texture
	 * variation.
	 */
	QUADRANT_ROTATED(CORNER_JOIN | BLOCK_SPECIES),

	/**
	 * Similar to format used by CTM.  Four textures - first is 1x1 fully-enclosed,
	 * second is horizontal borders, third is vertical borders, fourth is corners.
	 * Most faces are rendered as quadrants.
	 */
	QUADRANT_ORIENTED_BORDER(CORNER_JOIN | BLOCK_SPECIES, 4),

	/**
	 * Like {@code QUADRANT_ORIENTED_BORDER} but includes fifth texture for unbordered faces.
	 */
	QUADRANT_ORIENTED_TILE(CORNER_JOIN | BLOCK_SPECIES, 5),

	/**
	 * Compact connected texture format like QUADRANT_ROTATED but
	 * indicates a texture for cables or other shapes that don't
	 * need corner block information and have end caps.
	 */
	QUADRANT_ROTATED_CABLE(SIMPLE_JOIN | BLOCK_SPECIES);

	TextureLayout(int stateFlags) {
		this(stateFlags, 1);
	}

	TextureLayout(int stateFlags, int textureCount) {
		modelStateFlag = stateFlags;
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
