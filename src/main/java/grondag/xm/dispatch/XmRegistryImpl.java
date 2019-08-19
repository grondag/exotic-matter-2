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

import java.util.function.Function;

import grondag.xm.Xm;
import grondag.xm.api.block.WorldToModelStateFunction;
import grondag.xm.api.block.XmBlockState;
import grondag.xm.api.modelstate.ModelState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class XmRegistryImpl {
    private XmRegistryImpl() {
    }
    
    public static void register(Block block, Function<BlockState, ? extends WorldToModelStateFunction<?>> modelFunctionMap, Function<ItemStack, ModelState.Mutable> itemModelFunction) {
        for (BlockState blockState : block.getStateFactory().getStates()) {
            if (XmBlockState.get(blockState) != null) {
                // TODO: localize
                Xm.LOG.warn(String.format("[%s] BlockState %s already associated with an XmBlockState. Skipping.", Xm.MODID, blockState.toString()));
                return;
            }
            ((XmBlockStateAccess) blockState).xm_modelStateFunc(modelFunctionMap.apply(blockState));
            
            if(itemModelFunction != null) {
                final Item item = BlockItem.fromBlock(block);
                if(item != null) {
                    register(item, itemModelFunction);
                }
            }
        }
    }
    
    public static void register(Item item, Function<ItemStack, ModelState.Mutable> modelFunction) {
        final XmItemAccess access = (XmItemAccess)item;
        final Function<ItemStack, ModelState.Mutable> oldFunc = access.xm_modelStateFunc();
        if(oldFunc != null) {
            if(oldFunc != modelFunction) {
                Xm.LOG.warn(String.format("[%s] Item %s already associated with a model function. Skipping.", Xm.MODID, item.toString()));
                return;
            }
        }     
        access.xm_modelStateFunc(modelFunction);
    }
}
