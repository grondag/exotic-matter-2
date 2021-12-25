/*
 * This file is part of Exotic Matter and is licensed to the project under
 * terms that are compatible with the GNU Lesser General Public License.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership and licensing.
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
 */

package grondag.xm.api.texture;

import org.jetbrains.annotations.ApiStatus.Experimental;

import grondag.xm.texture.TextureLayoutMapImpl;

@Experimental
public interface TextureLayoutMap {
	static TextureLayoutMap create(TextureLayout layout, TextureNameFunction nameFunc) {
		return TextureLayoutMapImpl.create(layout, nameFunc);
	}

	TextureLayoutMap SINGLE = create(TextureLayout.SIMPLE, TextureNameFunction.SINGLE);

	TextureLayoutMap VERSIONED = create(TextureLayout.SIMPLE, TextureNameFunction.VERSIONED);

	TextureLayoutMap VERSION_X_8 = create(TextureLayout.SPLIT_X_8, TextureNameFunction.VERSION_X_8);

	TextureLayoutMap BORDER_13 = create(TextureLayout.BORDER_13, TextureNameFunction.BORDER_X_8);

	TextureLayoutMap BORDER_14 = create(TextureLayout.BORDER_14, TextureNameFunction.BORDER_X_8);

	TextureLayoutMap MASONRY_5 = create(TextureLayout.MASONRY_5, TextureNameFunction.MASONRY_X_8);

	TextureLayoutMap BIGTEX_ANIMATED = create(TextureLayout.BIGTEX_ANIMATED, TextureNameFunction.SINGLE);

	TextureLayoutMap QUADRANT_ROTATED_CABLE = create(TextureLayout.QUADRANT_ROTATED_CABLE, TextureNameFunction.SINGLE);

	TextureLayoutMap QUADRANT_ROTATED_SINGLE = create(TextureLayout.QUADRANT_ROTATED, TextureNameFunction.SINGLE);

	TextureLayoutMap QUADRANT_ROTATED_VERSIONED = create(TextureLayout.QUADRANT_ROTATED, TextureNameFunction.VERSIONED);

	TextureLayoutMap QUADRANT_ORIENTED_BORDER_SINGLE = create(TextureLayout.QUADRANT_ORIENTED_BORDER, TextureNameFunction.INDEXED);

	TextureLayoutMap QUADRANT_ORIENTED_BORDER_VERSIONED = create(TextureLayout.QUADRANT_ORIENTED_BORDER, TextureNameFunction.VERSIONED_INDEXED);

	TextureLayoutMap QUADRANT_ORIENTED_TILE_SINGLE = create(TextureLayout.QUADRANT_ORIENTED_TILE, TextureNameFunction.INDEXED);

	TextureLayoutMap QUADRANT_ORIENTED_TILE_VERSIONED = create(TextureLayout.QUADRANT_ORIENTED_TILE, TextureNameFunction.VERSIONED_INDEXED);

	TextureLayout layout();
}
