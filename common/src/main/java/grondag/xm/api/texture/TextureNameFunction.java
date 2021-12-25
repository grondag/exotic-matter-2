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

/**
 * Implement to map XM2 texture indexes to custom content texture names.
 */
@FunctionalInterface
@Experimental
public interface TextureNameFunction {
	String apply(String baseName, int versionIndex, int positionIndex);

	TextureNameFunction SINGLE = (baseName, version, index) -> baseName;

	TextureNameFunction VERSIONED = (baseName, version, index) -> baseName + "_" + version;

	TextureNameFunction INDEXED = (baseName, version, index) -> baseName + "_" + index;

	TextureNameFunction VERSIONED_INDEXED = (baseName, version, index) -> {
		return baseName + "_" + version + "_" + index;
	};

	TextureNameFunction VERSION_X_8 = (baseName, version, index) -> gimpNameX8(baseName, version);

	TextureNameFunction INDEX_X_8 = (baseName, version, index) -> gimpNameX8(baseName, index);

	/** 16 because two GIMP output rows per border, w/ 8 textures each. */
	int GIMP_BORDER_SPOTS_PER_VARIANT = 16;

	// Texture sequence expected by index map functions for BORDER_13 and BORDER_14
	int BORDER_SIDES_ALL = 0;
	int BORDER_SIDE_TOP = 1;
	int BORDER_SIDES_TOP_RIGHT = 2;
	int BORDER_SIDES_TOP_BOTTOM = 3;
	int BORDER_SIDES_TOP_LEFT_RIGHT = 4;
	int BORDER_MIXED_TOP_BR = 5;
	int BORDER_MIXED_TOP_BL_BR = 6;
	int BORDER_MIXED_TOP_RIGHT_BL = 7;
	int BORDER_CORNER_TR = 8;
	int BORDER_CORNERS_TL_TR = 9;
	int BORDER_CORNERS_BL_TR = 10;
	int BORDER_CORNERS_BL_TR_BR = 11;
	int BORDER_CORNERS_ALL = 12;
	int BORDER_NONE = 13;

	// Order used by existing XM GIMP-layout border textures
	int[] GIMP_BORDER_SEQUENCE = { 4, 0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

	TextureNameFunction BORDER_X_8 = (baseName, version, index) -> gimpNameX8(baseName, version * GIMP_BORDER_SPOTS_PER_VARIANT + GIMP_BORDER_SEQUENCE[index]);

	/** 8 because one GIMP output rows per border, w/ 8 textures each. */
	int GIMP_MASONRY_SPOTS_PER_VARIANT = 8;

	TextureNameFunction MASONRY_X_8 = (baseName, version, index) -> gimpNameX8(baseName, version * GIMP_MASONRY_SPOTS_PER_VARIANT + index);

	static String gimpNameX8(String baseName, int offset) {
		return baseName + "_" + (offset >> 3) + "_" + (offset & 7);
	}
}
