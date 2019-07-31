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

package grondag.xm2.texture;

import java.util.HashMap;

import org.apache.commons.lang3.ObjectUtils;

import grondag.xm2.api.texture.TextureGroup;
import grondag.xm2.api.texture.TextureLayoutMap;
import grondag.xm2.api.texture.TextureRenderIntent;
import grondag.xm2.api.texture.TextureRotation;
import grondag.xm2.api.texture.TextureScale;
import grondag.xm2.api.texture.TextureSet;
import grondag.xm2.api.texture.TextureSetRegistry;
import net.minecraft.util.Identifier;

public class TextureSetRegistryImpl implements TextureSetRegistry {
    public static final TextureSetRegistryImpl INSTANCE = new TextureSetRegistryImpl();
    public static final TextureSetImpl DEFAULT_TEXTURE_SET;

    public static TextureSet noTexture() {
        return INSTANCE.getByIndex(0);
    }

    private final TextureSetImpl[] array = new TextureSetImpl[MAX_TEXTURE_SETS];
    private final HashMap<Identifier, TextureSetImpl> map = new HashMap<>();
    private int nextIndex = 0;

    synchronized void add(TextureSetImpl newSet) {
        if (array[newSet.index] == null && !map.containsKey(newSet.id)) {
            array[newSet.index] = newSet;
            map.put(newSet.id, newSet);
        }
        ;
    }

    synchronized int claimIndex() {
        return nextIndex++;
    }

    @Override
    public TextureSetImpl getById(Identifier id) {
        return ObjectUtils.defaultIfNull(map.get(id), DEFAULT_TEXTURE_SET);
    }

    @Override
    public TextureSetImpl getByIndex(int index) {
        return index < 0 || index >= nextIndex ? DEFAULT_TEXTURE_SET : array[index];
    }

    @Override
    public int size() {
        return nextIndex;
    }

    static {
        DEFAULT_TEXTURE_SET = (TextureSetImpl) TextureSet.builder().displayNameToken("none").baseTextureName("xm2:blocks/noise_moderate").versionCount(4)
                .scale(TextureScale.SINGLE).layout(TextureLayoutMap.VERSION_X_8).rotation(TextureRotation.ROTATE_RANDOM)
                .renderIntent(TextureRenderIntent.BASE_ONLY).groups(TextureGroup.ALWAYS_HIDDEN).build(TextureSetRegistry.NONE_ID);

        DEFAULT_TEXTURE_SET.use();
    }
}
