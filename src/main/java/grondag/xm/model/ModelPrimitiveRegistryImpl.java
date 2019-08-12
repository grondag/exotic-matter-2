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

import grondag.xm.api.modelstate.PrimitiveModelState;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.model.state.ModelStateTagHelper;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

@SuppressWarnings("rawtypes")
public class ModelPrimitiveRegistryImpl implements ModelPrimitiveRegistry {
    public static ModelPrimitiveRegistryImpl INSTANCE = new ModelPrimitiveRegistryImpl();

    private final Object2ObjectOpenHashMap<String, ModelPrimitive> map = new Object2ObjectOpenHashMap<>();
    private final ObjectArrayList<ModelPrimitive> list = new ObjectArrayList<>();
    private final Object2IntOpenHashMap<String> reverseMap = new Object2IntOpenHashMap<>();

    private ModelPrimitiveRegistryImpl() {
    }

    @Override
    public synchronized <R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> boolean register(ModelPrimitive<R, W> primitive) {
        boolean result = map.putIfAbsent(primitive.id().toString(), primitive) == null;
        if (result) {
            final int index = list.size();
            list.add(primitive);
            reverseMap.put(primitive.id().toString(), index);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> ModelPrimitive<R, W> get(int primitiveIndex) {
        return list.get(primitiveIndex);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>>  ModelPrimitive<R, W> get(String idString) {
        return map.get(idString);
    }

    @Override
    public synchronized void forEach(Consumer<ModelPrimitive> consumer) {
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

    @Override
    public <R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> W fromTag(CompoundTag tag) {
        ModelPrimitive<R, W> shape = get(tag.getString(ModelStateTagHelper.NBT_SHAPE));
        if (shape == null) {
            return null;
        }
        return shape.fromTag(tag);
    }

    @Override
    public <R extends PrimitiveModelState<R, W>, W extends PrimitiveModelState.Mutable<R,W>> W fromBuffer(PacketByteBuf buf) {
        ModelPrimitive<R, W> shape = get(buf.readVarInt());
        if (shape == null) {
            return null;
        }
        return shape.fromBuffer(buf);
    }
    
    public void invalidateCache() {
        list.forEach(p -> p.invalidateCache());
    }
}
