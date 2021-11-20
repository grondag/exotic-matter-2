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

package grondag.xm.api.texture;

import org.jetbrains.annotations.ApiStatus.Experimental;

@Experimental
public enum TextureGroup {
	STATIC_TILES,
	STATIC_BORDERS,
	STATIC_DETAILS,
	DYNAMIC_TILES,
	DYNAMIC_BORDERS,
	DYNAMIC_DETAILS,
	HIDDEN_TILES,
	HIDDEN_BORDERS,
	HIDDEN_DETAILS,
	ALWAYS_HIDDEN;

	/** Used as a fast way to filter textures from a list. */
	public final int bitFlag;

	TextureGroup() {
		bitFlag = (1 << ordinal());
	}

	public static int makeTextureGroupFlags(TextureGroup... groups) {
		int flags = 0;

		for (final TextureGroup group : groups) {
			flags |= group.bitFlag;
		}

		return flags;
	}

	public static int HIDDEN = HIDDEN_TILES.bitFlag | HIDDEN_BORDERS.bitFlag | HIDDEN_DETAILS.bitFlag | ALWAYS_HIDDEN.bitFlag;
}
