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

package grondag.xm.block;

import javax.annotation.Nullable;

import grondag.xm.api.block.WorldToModelStateFunction;
import grondag.xm.api.block.XmBlockState;

public interface XmBlockStateAccess {
    void xm2_worldFunc(WorldToModelStateFunction<?> func);

    WorldToModelStateFunction<?> xm2_worldFunc();
    
    @Nullable XmBlockState xm2_toXmBlockState();
}
