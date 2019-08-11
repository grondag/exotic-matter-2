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

package grondag.xm.api.primitive;

import java.util.function.Consumer;

import grondag.xm.model.ModelPrimitiveRegistryImpl;
import grondag.xm.model.state.BaseModelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public interface ModelPrimitiveRegistry {
    static ModelPrimitiveRegistry INSTANCE = ModelPrimitiveRegistryImpl.INSTANCE;

    <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> boolean register(ModelPrimitive<R, W> primitive);

    <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> ModelPrimitive<R, W> get(int primitiveIndex);

    default <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> ModelPrimitive<R, W> get(Identifier primitiveId) {
        return get(primitiveId.toString());
    }

    <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> ModelPrimitive<R, W> get(String idString);

    @SuppressWarnings("rawtypes")
    void forEach(Consumer<ModelPrimitive> consumer);

    int count();

    int indexOf(String idString);

    default int indexOf(Identifier primitiveId) {
        return indexOf(primitiveId.toString());
    }

    default <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> int indexOf(ModelPrimitive<R, W> primitive) {
        return indexOf(primitive.id());
    }
    
    <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> W fromTag(CompoundTag tag);
    
    <R extends BaseModelState<R, W>, W extends BaseModelState.Mutable<R,W>> W fromBuffer(PacketByteBuf buf);
}
