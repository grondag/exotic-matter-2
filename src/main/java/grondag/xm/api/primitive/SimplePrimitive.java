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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.function.Function;

import org.apiguardian.api.API;

import grondag.xm.api.mesh.XmMesh;
import grondag.xm.api.modelstate.SimpleModelState;
import grondag.xm.api.orientation.OrientationType;
import grondag.xm.api.primitive.surface.XmSurfaceList;
import grondag.xm.primitive.SimplePrimitiveBuilderImpl;

@API(status = EXPERIMENTAL)
public interface SimplePrimitive extends ModelPrimitive<SimpleModelState, SimpleModelState.Mutable>{
    static Builder builder() {
        return SimplePrimitiveBuilderImpl.builder();
    }
    
    public interface Builder {
        
        SimplePrimitive build(String idString);

        Builder surfaceList(XmSurfaceList list);
        
        Builder primitiveBitCount(int bitCount);

        Builder orientationType(OrientationType orientationType);

        Builder polyFactory(Function<SimpleModelState, XmMesh> polyFactory);

    }
}
