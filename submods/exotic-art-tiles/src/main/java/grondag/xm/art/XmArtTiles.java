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

package grondag.xm.art;

import static grondag.xm.api.texture.TextureRotation.ROTATE_RANDOM;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;

public class XmArtTiles {
    public static final TextureSet TILE_DOTS = TextureSet.builder().displayNameToken("dots").baseTextureName("exotic-art:blocks/dots").versionCount(4)
            .scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSION_X_8).rotation(ROTATE_RANDOM).renderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT)
            .groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS).build("exotic-art:dots");
    
    public static final TextureSet TILE_DOTS_SUBTLE = TextureSet.builder().displayNameToken("dots_subtle").versionCount(4).scale(TextureScale.SINGLE)
            .layout(TextureLayoutMap.VERSION_X_8).baseTextureName("exotic-art:blocks/dots_subtle").rotation(ROTATE_RANDOM)
            .renderIntent(TextureRenderIntent.BASE_OR_OVERLAY_NO_CUTOUT).groups(TextureGroup.STATIC_TILES, TextureGroup.STATIC_DETAILS)
            .build("exotic-art:dots_subtle");

    public static final TextureSet TILE_DOTS_INVERSE = TextureSet.builder(TILE_DOTS_SUBTLE).displayNameToken("dots_inverse")
            .baseTextureName("exotic-art:blocks/dots_inverse").build("exotic-art:dots_inverse");

    public static final TextureSet TILE_DOTS_INVERSE_SUBTLE = TextureSet.builder(TILE_DOTS_SUBTLE).displayNameToken("dots_inverse_subtle")
            .baseTextureName("exotic-art:blocks/dots_inverse_subtle").build("exotic-art:dots_inverse_subtle");
}
