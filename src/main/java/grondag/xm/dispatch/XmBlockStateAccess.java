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
package grondag.xm.dispatch;

import static org.apiguardian.api.API.Status.INTERNAL;

import javax.annotation.Nullable;

import org.apiguardian.api.API;

import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.WorldToModelStateMap;

@API(status = INTERNAL)
public interface XmBlockStateAccess {
    void xm_modelStateFunc(WorldToModelStateMap<?> func);

    WorldToModelStateMap<?> xm_modelStateFunc();
    
    @Nullable XmBlockState xm_toXmBlockState();
}
