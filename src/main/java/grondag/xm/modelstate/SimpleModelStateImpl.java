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
package grondag.xm.modelstate;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

import grondag.xm.api.modelstate.MutableSimpleModelState;
import grondag.xm.api.modelstate.SimpleModelState;

@API(status = INTERNAL)
public class SimpleModelStateImpl extends AbstractPrimitiveModelState<SimpleModelStateImpl, SimpleModelState, MutableSimpleModelState> implements MutableSimpleModelState {
    public static final int MAX_SURFACES = 8;
    
    public static final ModelStateFactoryImpl<SimpleModelStateImpl, SimpleModelState, MutableSimpleModelState> FACTORY = new ModelStateFactoryImpl<>(SimpleModelStateImpl::new);

    @Override
    public final ModelStateFactoryImpl<SimpleModelStateImpl, SimpleModelState, MutableSimpleModelState> factoryImpl() {
        return FACTORY;
    }

    @Override
    protected int maxSurfaces() {
        return MAX_SURFACES;
    }
}
