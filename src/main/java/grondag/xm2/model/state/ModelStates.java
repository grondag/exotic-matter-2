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
package grondag.xm2.model.state;

import javax.annotation.Nullable;

import grondag.xm2.api.model.ModelPrimitive;
import grondag.xm2.api.model.OwnedModelState;
import net.minecraft.nbt.CompoundTag;

public abstract class ModelStates {
    private ModelStates() {
    }

    public static final int PRIMITIVE_BIT_COUNT = 6;

    public static OwnedModelState claimSimple(ModelPrimitive primitive) {
        return PrimitiveModelState.claim(primitive);
    }

    public static @Nullable OwnedModelState fromTag(CompoundTag tag) {
        return PrimitiveModelState.fromTag(tag);
    }
}
