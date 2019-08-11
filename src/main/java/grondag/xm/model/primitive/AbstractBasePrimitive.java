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

import grondag.xm.api.primitive.SimplePrimitive;
import grondag.xm.model.state.BaseModelState.ModelStateFactory;
import grondag.xm.model.state.PrimitiveModelState;
import grondag.xm.model.state.PrimitiveModelState.Mutable;
import net.minecraft.util.Identifier;

public abstract class AbstractBasePrimitive extends AbstractModelPrimitive<PrimitiveModelState, PrimitiveModelState.Mutable> implements SimplePrimitive {
    protected AbstractBasePrimitive(Identifier id, int stateFlags, ModelStateFactory<PrimitiveModelState, Mutable> factory) {
        super(id, stateFlags, factory);
    }
    
    protected AbstractBasePrimitive(String idString, int stateFlags, ModelStateFactory<PrimitiveModelState, Mutable> factory) {
        super(idString, stateFlags, factory);
    }
}
