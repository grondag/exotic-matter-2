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

import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_CORNER_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_MASONRY_JOIN;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_POS;
import static grondag.xm.api.modelstate.ModelStateFlags.STATE_FLAG_NEEDS_SIMPLE_JOIN;

import grondag.xm.api.block.WorldToModelStateFunction;
import grondag.xm.api.connect.state.CornerJoinState;
import grondag.xm.api.connect.state.SimpleJoinState;
import grondag.xm.api.connect.world.BlockNeighbors;
import grondag.xm.api.connect.world.ModelStateFunction;
import grondag.xm.api.connect.world.MasonryHelper;
import grondag.xm.api.modelstate.SimpleModelState;

public class SimpleModelStateImpl extends AbstractPrimitiveModelState<SimpleModelStateImpl, SimpleModelState, SimpleModelState.Mutable> implements SimpleModelState.Mutable {
    public static final int MAX_SURFACES = 8;
    
    public static final ModelStateFactoryImpl<SimpleModelStateImpl, SimpleModelState, SimpleModelState.Mutable> FACTORY = new ModelStateFactoryImpl<>(SimpleModelStateImpl::new);

    @Override
    public final ModelStateFactoryImpl<SimpleModelStateImpl, SimpleModelState, SimpleModelState.Mutable> factoryImpl() {
        return FACTORY;
    }

    @Override
    protected int maxSurfaces() {
        return MAX_SURFACES;
    }
}
