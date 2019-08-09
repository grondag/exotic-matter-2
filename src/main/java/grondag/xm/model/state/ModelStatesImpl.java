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
package grondag.xm.model.state;

import javax.annotation.Nullable;

import grondag.xm.api.modelstate.MutableModelState;
import grondag.xm.api.primitive.ModelPrimitive;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;

public abstract class ModelStatesImpl {
    private ModelStatesImpl() {
    }

    public static final int PRIMITIVE_BIT_COUNT = 6;

    public static MutableModelState claimSimple(ModelPrimitive primitive) {
        return PrimitiveModelState.claim(primitive);
    }

    public static @Nullable MutableModelState fromTag(CompoundTag tag) {
        return PrimitiveModelState.fromTag(tag);
    }

    public static @Nullable MutableModelState fromBuffer(PacketByteBuf buf) {
        PrimitiveModelState.Mutable result = PrimitiveModelState.claim();
        result.fromBytes(buf);
        return result;
    }
}
