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

package grondag.xm.api.primitive.base;

import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.modelstate.PrimitiveModelState.ModelStateFactory;
import grondag.xm.api.modelstate.SimpleModelState.Mutable;
import grondag.xm.api.primitive.SimplePrimitive;
import net.minecraft.util.Identifier;

public abstract class AbstractSimplePrimitive extends AbstractPrimitive<SimpleModelState, SimpleModelState.Mutable> implements SimplePrimitive {
    protected AbstractSimplePrimitive(Identifier id, int stateFlags, ModelStateFactory<SimpleModelState, Mutable> factory) {
        super(id, stateFlags, factory);
    }
    
    protected AbstractSimplePrimitive(String idString, int stateFlags, ModelStateFactory<SimpleModelState, Mutable> factory) {
        super(idString, stateFlags, factory);
    }
}
