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

package grondag.xm.api.block;

import java.util.function.Function;

import grondag.xm.block.XmBlockRegistryImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class XmBlockRegistry {
    private XmBlockRegistry() {}
    
    public static void addBlockStates(
            Block block, 
            Function<BlockState, ? extends WorldToModelStateFunction<?>> modelFunctionMap) {
        
        XmBlockRegistryImpl.register(block, modelFunctionMap);
    }
    
    public static <F extends WorldToModelStateFunction<?>> void addBlock(Block block, F modelFunction) {
        addBlockStates(block, (BlockState bs) -> modelFunction);
    }
}
