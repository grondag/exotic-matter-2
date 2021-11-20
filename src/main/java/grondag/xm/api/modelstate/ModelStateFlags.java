/*
 * Copyright © Original Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Additional copyright and licensing notices may apply for content that was
 * included from other projects. For more information, see ATTRIBUTION.md.
 */

package grondag.xm.api.modelstate;

import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public class ModelStateFlags {
	private ModelStateFlags() { }

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
	 * Applies to block-type states. True if is a block type state and requires simple
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
}
