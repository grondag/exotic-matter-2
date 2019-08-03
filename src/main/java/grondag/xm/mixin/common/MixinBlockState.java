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

package grondag.xm.mixin.common;

import org.spongepowered.asm.mixin.Mixin;

import grondag.xm.block.XmBlockStateAccess;
import grondag.xm.block.XmBlockRegistryImpl.XmBlockStateImpl;
import net.minecraft.block.BlockState;

@Mixin(BlockState.class)
public abstract class MixinBlockState implements XmBlockStateAccess {
    private XmBlockStateImpl xmBlockState;

    @Override
    public void xm2_blockState(XmBlockStateImpl state) {
        xmBlockState = state;
    }

    @Override
    public XmBlockStateImpl xm2_blockState() {
        return xmBlockState;
    }
}
