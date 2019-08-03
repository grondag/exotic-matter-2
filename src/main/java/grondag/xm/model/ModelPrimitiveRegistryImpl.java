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

package grondag.xm.model;

import java.util.function.Consumer;

import grondag.xm.api.model.ModelPrimitive;
import grondag.xm.api.model.ModelPrimitiveRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ModelPrimitiveRegistryImpl implements ModelPrimitiveRegistry {
    public static ModelPrimitiveRegistryImpl INSTANCE = new ModelPrimitiveRegistryImpl();

    private final Object2ObjectOpenHashMap<String, ModelPrimitive> map = new Object2ObjectOpenHashMap<>();
    private final ObjectArrayList<ModelPrimitive> list = new ObjectArrayList<>();
    private final Object2IntOpenHashMap<String> reverseMap = new Object2IntOpenHashMap<>();

    private ModelPrimitiveRegistryImpl() {
    }

    @Override
    public synchronized boolean register(ModelPrimitive primitive) {
        boolean result = map.putIfAbsent(primitive.id().toString(), primitive) == null;
        if (result) {
            final int index = list.size();
            list.add(primitive);
            reverseMap.put(primitive.id().toString(), index);
        }
        return result;
    }

    @Override
    public ModelPrimitive get(int primitiveIndex) {
        return list.get(primitiveIndex);
    }

    @Override
    public ModelPrimitive get(String idString) {
        return map.get(idString);
    }

    @Override
    public void forEach(Consumer<ModelPrimitive> consumer) {
        list.forEach(consumer);
    }

    @Override
    public int count() {
        return list.size();
    }

    @Override
    public int indexOf(String idString) {
        return reverseMap.getInt(idString);
    }
}
