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

package grondag.xm2.texture.impl;

import grondag.xm2.texture.api.TextureGroup;
import grondag.xm2.texture.api.TextureLayoutMap;
import grondag.xm2.texture.api.TextureRenderIntent;
import grondag.xm2.texture.api.TextureRotation;
import grondag.xm2.texture.api.TextureScale;

abstract class AbstractTextureSet {
	TextureLayoutMapImpl layoutMap = (TextureLayoutMapImpl) TextureLayoutMap.SINGLE;
    TextureRotation rotation = TextureRotation.ROTATE_NONE;
    TextureScale scale = TextureScale.SINGLE;
    TextureRenderIntent renderIntent = TextureRenderIntent.BASE_ONLY;
    int textureGroupFlags = TextureGroup.ALWAYS_HIDDEN.bitFlag;
    int versionCount = 1;
    boolean renderNoBorderAsTile = false;
    String rawBaseTextureName;
    String displayNameToken;
    
    protected void copyFrom(AbstractTextureSet template) {
        this.layoutMap = template.layoutMap;
        this.rotation = template.rotation;
        this.scale = template.scale;
        this.renderIntent = template.renderIntent;
        this.versionCount = template.versionCount;
        this.rawBaseTextureName = template.rawBaseTextureName;
        this.renderNoBorderAsTile = template.renderNoBorderAsTile;
        this.displayNameToken = template.displayNameToken;
        this.textureGroupFlags = template.textureGroupFlags;
    }
    
    public TextureLayoutMap map() {
        return layoutMap;
    }

    public TextureRotation rotation() {
        return rotation;
    }

    public TextureScale scale() {
        return scale;
    }

    public TextureRenderIntent renderIntent() {
        return renderIntent;
    }

    public int versionCount() {
        return versionCount;
    }
    
    public boolean renderNoBorderAsTile() {
        return renderNoBorderAsTile;
    }

    public String displayName() {
        return displayNameToken;
    }

    public int textureGroupFlags() {
        return textureGroupFlags;
    }
}
