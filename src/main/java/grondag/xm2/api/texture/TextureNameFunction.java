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

/**
 * Implement to map XM2 texture indexes to custom content texture names
 */
@FunctionalInterface
public interface TextureNameFunction {
    String apply(String baseName, int versionIndex, int positionIndex);

    TextureNameFunction SINGLE = (baseName, version, index) -> baseName;

    TextureNameFunction VERSIONED = (baseName, version, index) -> baseName + "_" + version;

    TextureNameFunction VERSION_X_8 = (baseName, version, index) -> gimpNameX8(baseName, version);

    TextureNameFunction INDEX_X_8 = (baseName, version, index) -> gimpNameX8(baseName, index);

    /** 16 because two GIMP output rows per border, w/ 8 textures each */
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

    /** 8 because one GIMP output rows per border, w/ 8 textures each */
    int GIMP_MASONRY_SPOTS_PER_VARIANT = 8;

    TextureNameFunction MASONRY_X_8 = (baseName, version, index) -> gimpNameX8(baseName, version * GIMP_MASONRY_SPOTS_PER_VARIANT + index);

    static String gimpNameX8(String baseName, int offset) {
        return baseName + "_" + (offset >> 3) + "_" + (offset & 7);
    }
}
