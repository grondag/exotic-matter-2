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

import java.util.function.Consumer;

import grondag.xm.api.model.ModelStateFlags;
import grondag.xm.api.texture.TextureRotation;
import grondag.xm.api.texture.TextureSet;
import grondag.xm.api.texture.TextureSetBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

public class TextureSetImpl extends AbstractTextureSet implements TextureSet {
    public static TextureSetBuilder builder() {
        return new TextureSetBuilderImpl();
    }

    public static TextureSetBuilder builder(TextureSet template) {
        TextureSetBuilderImpl result = new TextureSetBuilderImpl();
        result.copyFrom((AbstractTextureSet) template);
        return result;
    }

    public final int index;
    public final Identifier id;
    public final int versionMask;
    public final int stateFlags;
    public final String baseTextureName;
    private boolean used = false;

    TextureSetImpl(Identifier id, AbstractTextureSet template) {
        this.id = id;
        this.baseTextureName = template.rawBaseTextureName;
        this.index = TextureSetRegistryImpl.INSTANCE.claimIndex();
        copyFrom(template);
        this.versionMask = Math.max(0, template.versionCount - 1);
        this.layoutMap = template.layoutMap;

        int flags = template.scale.modelStateFlag | template.layoutMap.layout.modelStateFlag;

        // textures with randomization options also require position information

        if (template.rotation == TextureRotation.ROTATE_RANDOM) {
            flags |= (ModelStateFlags.STATE_FLAG_NEEDS_TEXTURE_ROTATION | ModelStateFlags.STATE_FLAG_NEEDS_POS);
        }

        if (template.versionCount > 1) {
            flags |= ModelStateFlags.STATE_FLAG_NEEDS_POS;
        }

        this.stateFlags = flags;
        
        TextureSetRegistryImpl.INSTANCE.add(this);
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int stateFlags() {
        return stateFlags;
    }

    @Override
    public void prestitch(Consumer<Identifier> stitcher) {
        layoutMap.prestitch(this, stitcher);
    }

    @Override
    public String sampleTextureName() {
        return layoutMap.sampleTextureName(this);
    }

    private Sprite sampleSprite;

    @Override
    public Sprite sampleSprite() {
        Sprite result = sampleSprite;
        if (result == null) {
            result = MinecraftClient.getInstance().getSpriteAtlas().getSprite(sampleTextureName());
            sampleSprite = result;
        }
        return result;
    }

    @Override
    public String textureName(int version) {
        return layoutMap.buildTextureName(this, version & versionMask, 0);
    }

    @Override
    public String textureName(int version, int index) {
        return layoutMap.buildTextureName(this, version & versionMask, index);
    }

    @Override
    public int versionMask() {
        return versionMask;
    }

    @Override
    public String baseTextureName() {
        return baseTextureName;
    }

    @Override
    public void use() {
        used = true;
    }

    @Override
    public boolean used() {
        return used;
    }
}
