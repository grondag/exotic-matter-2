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

package grondag.xm.api.model;

import java.util.function.Consumer;

import grondag.xm.model.ModelPrimitiveRegistryImpl;
import net.minecraft.util.Identifier;

public interface ModelPrimitiveRegistry {
    static ModelPrimitiveRegistry INSTANCE = ModelPrimitiveRegistryImpl.INSTANCE;

    boolean register(ModelPrimitive primitive);

    ModelPrimitive get(int primitiveIndex);

    default ModelPrimitive get(Identifier primitiveId) {
        return get(primitiveId.toString());
    }

    ModelPrimitive get(String idString);

    void forEach(Consumer<ModelPrimitive> consumer);

    int count();

    int indexOf(String idString);

    default int indexOf(Identifier primitiveId) {
        return indexOf(primitiveId.toString());
    }

    default int indexOf(ModelPrimitive primitive) {
        return indexOf(primitive.id());
    }
}
