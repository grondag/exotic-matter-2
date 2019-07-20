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
package grondag.hard_science.machines.impl.processing;

import grondag.hard_science.machines.base.MachineTileEntityTickable;
import net.minecraftforge.items.IItemHandler;

public class MicronizerTileEntity extends MachineTileEntityTickable {

    @Override
    public IItemHandler getItemHandler() {
        if (this.world == null || this.world.isRemote)
            return null;
        if (this.machine() == null)
            return null;
        return this.machine().getBufferManager();
    }

}