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
import grondag.xm.api.model.ImmutableModelState;
import grondag.xm.api.model.ModelPrimitive;
import grondag.xm.api.model.ModelPrimitiveRegistry;
import grondag.xm.api.model.ModelPrimitiveState;
import grondag.xm.api.model.MutableModelState;
import grondag.xm.api.model.OwnedModelState;
import grondag.xm.model.state.ModelStatesImpl;
import net.minecraft.util.Identifier;

public abstract class AbstractModelPrimitive implements ModelPrimitive {
    private final ImmutableModelState defaultState;

    private final Identifier id;

    /**
     * bits flags used by ModelState to know which optional state elements are
     * needed by this shape
     */
    private final int stateFlags;

    protected AbstractModelPrimitive(Identifier id, int stateFlags) {
        this.stateFlags = stateFlags;
        this.id = id;

        // we handle registration here because model state currently relies on it for
        // serialization
        if (!ModelPrimitiveRegistry.INSTANCE.register(this)) {
            Xm.LOG.warn("[XM2] Unable to register ModelPrimitive " + id.toString());
        }

        OwnedModelState state = ModelStatesImpl.claimSimple(this);
        updateDefaultState(state);
        this.defaultState = state.toImmutable();
        state.release();
    }

    protected AbstractModelPrimitive(String idString, int stateFlags) {
        this(new Identifier(idString), stateFlags);
    }

    @Override
    public ImmutableModelState defaultState() {
        return defaultState;
    }

    @Override
    public int stateFlags(ModelPrimitiveState modelState) {
        return stateFlags;
    }

    @Override
    public Identifier id() {
        return id;
    }

    /**
     * Override if default state should be something other than the, erm... default.
     */
    protected void updateDefaultState(MutableModelState modelState) {
    }
}
