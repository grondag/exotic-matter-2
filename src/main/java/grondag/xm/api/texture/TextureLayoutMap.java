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
