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
package grondag.xm.modelstate;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.fermion.varia.NBTDictionary;
import net.minecraft.nbt.CompoundTag;

@API(status = INTERNAL)
public abstract class ModelStateTagHelper {
    private ModelStateTagHelper() {
    }

    public static final String NBT_MODEL_BITS = NBTDictionary.claim("modelState");

    public static final String NBT_SHAPE = NBTDictionary.claim("shape");

    /**
     * Stores string containing registry names of textures, vertex processors
     */
    public static final String NBT_LAYERS = NBTDictionary.claim("layers");

    /**
     * Removes model state from the tag if present.
     */
    public static final void clearNBTValues(CompoundTag tag) {
        if (tag == null)
            return;
        tag.remove(NBT_MODEL_BITS);
        tag.remove(NBT_SHAPE);
        tag.remove(NBT_LAYERS);
    }
}
