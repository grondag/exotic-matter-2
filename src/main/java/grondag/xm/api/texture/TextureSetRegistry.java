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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Consumer;

import org.apiguardian.api.API;

import grondag.xm.Xm;
import grondag.xm.texture.TextureSetRegistryImpl;
import net.minecraft.util.Identifier;

@API(status = EXPERIMENTAL)
public interface TextureSetRegistry {
    public static TextureSetRegistry instance() {
        return TextureSetRegistryImpl.INSTANCE;
    }

    /**
     * Will always be associated with index 0.
     */
    public static final Identifier NONE_ID = new Identifier(Xm.MODID, "none");

    // TODO: make this larger after state refactor
    /**
     * Max number of texture palettes that can be registered, loaded and represented
     * in model state.
     */
    public static final int MAX_TEXTURE_SETS = 4096;

    TextureSet get(Identifier id);

    TextureSet get(int index);

    void forEach(Consumer<TextureSet> consumer);
}
