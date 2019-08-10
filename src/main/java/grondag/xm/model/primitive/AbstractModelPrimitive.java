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

package grondag.xm.model.primitive;

import grondag.xm.Xm;
import grondag.xm.api.primitive.ModelPrimitive;
import grondag.xm.api.primitive.ModelPrimitiveRegistry;
import grondag.xm.model.state.AbstractPrimitiveModelState;
import grondag.xm.model.state.AbstractPrimitiveModelState.ModelStateFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

public abstract class AbstractModelPrimitive<T extends AbstractPrimitiveModelState<T>> implements ModelPrimitive<T> {
    private final T defaultState;

    private final ModelStateFactory<T> factory;
    
    private final Identifier id;
    
    /**
     * bits flags used by ModelState to know which optional state elements are
     * needed by this shape
     */
    private final int stateFlags;

    protected AbstractModelPrimitive(Identifier id, int stateFlags, ModelStateFactory<T> factory) {
        this.stateFlags = stateFlags;
        this.id = id;
        this.factory = factory;

        // we handle registration here because model state currently relies on it for
        // serialization
        if (!ModelPrimitiveRegistry.INSTANCE.register(this)) {
            Xm.LOG.warn("[XM2] Unable to register ModelPrimitive " + id.toString());
        }

        T state = factory.claim(this);
        updateDefaultState(state);
        this.defaultState = state.releaseToImmutable();
    }

    protected AbstractModelPrimitive(String idString, int stateFlags, ModelStateFactory<T> factory) {
        this(new Identifier(idString), stateFlags, factory);
    }

    @Override
    public T defaultState() {
        return defaultState;
    }

    @Override
    public int stateFlags(T modelState) {
        return stateFlags;
    }

    @Override
    public Identifier id() {
        return id;
    }

    /**
     * Override if default state should be something other than the, erm... default.
     */
    protected void updateDefaultState(T modelState) {
    }
    
    @Override
    public final T fromBuffer(PacketByteBuf buf) {
        return factory.fromBuffer(this, buf);
    }
    
    @Override
    public final T fromTag(CompoundTag tag) {
        return factory.fromTag(this, tag);
    }
}
