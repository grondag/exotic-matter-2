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

import grondag.xm.api.texture.TextureGroup;
import grondag.xm.api.texture.TextureLayoutMap;
import grondag.xm.api.texture.TextureRenderIntent;
import grondag.xm.api.texture.TextureRotation;
import grondag.xm.api.texture.TextureScale;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetBuilder;
import net.minecraft.util.Identifier;

public class TextureSetBuilderImpl extends AbstractTextureSet implements TextureSetBuilder {
    @Override
    public TextureSetBuilder versionCount(int versionCount) {
        this.versionCount = versionCount;
        return this;
    }

    @Override
    public TextureSetBuilder scale(TextureScale scale) {
        this.scale = scale;
        return this;
    }

    @Override
    public TextureSetBuilder layout(TextureLayoutMap layout) {
        this.layoutMap = (TextureLayoutMapImpl) layout;
        return this;
    }

    @Override
    public TextureSetBuilder rotation(TextureRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public TextureSetBuilder renderIntent(TextureRenderIntent renderIntent) {
        this.renderIntent = renderIntent;
        return this;
    }

    @Override
    public TextureSetBuilder groups(TextureGroup... groups) {
        this.textureGroupFlags = TextureGroup.makeTextureGroupFlags(groups);
        return this;
    }

    @Override
    public TextureSetBuilder renderNoBorderAsTile(boolean renderNoBorderAsTile) {
        this.renderNoBorderAsTile = renderNoBorderAsTile;
        return this;
    }

    @Override
    public TextureSetBuilder baseTextureName(String baseTextureName) {
        this.rawBaseTextureName = baseTextureName;
        return this;
    }

    @Override
    public TextureSetBuilder displayNameToken(String displayNameToken) {
        this.displayNameToken = displayNameToken;
        return this;
    }

    @Override
    public TextureSet build(Identifier id) {
        TextureSetImpl result;
        if(TextureSetRegistryImpl.INSTANCE.contains(id)) {
            result = TextureSetRegistryImpl.INSTANCE.get(id);
        } else {
            result = new TextureSetImpl(id, this);
        }
        return result;
    }
}
