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
package grondag.xm.texture;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureTransform;

@API(status = INTERNAL)
abstract class AbstractTextureSet {
    TextureLayoutMapImpl layoutMap = (TextureLayoutMapImpl) TextureLayoutMap.SINGLE;
    TextureTransform transform = TextureTransform.IDENTITY;
    TextureScale scale = TextureScale.SINGLE;
    TextureRenderIntent renderIntent = TextureRenderIntent.BASE_ONLY;
    int textureGroupFlags = TextureGroup.ALWAYS_HIDDEN.bitFlag;
    int versionCount = 1;
    boolean renderNoBorderAsTile = false;
    String rawBaseTextureName;
    String displayNameToken;

    protected void copyFrom(AbstractTextureSet template) {
        layoutMap = template.layoutMap;
        transform = template.transform;
        scale = template.scale;
        renderIntent = template.renderIntent;
        versionCount = template.versionCount;
        rawBaseTextureName = template.rawBaseTextureName;
        renderNoBorderAsTile = template.renderNoBorderAsTile;
        displayNameToken = template.displayNameToken;
        textureGroupFlags = template.textureGroupFlags;
    }

    public TextureLayoutMap map() {
        return layoutMap;
    }

    public TextureTransform transform() {
        return transform;
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

    public String displayNameToken() {
        return displayNameToken;
    }

    public int textureGroupFlags() {
        return textureGroupFlags;
    }
}
