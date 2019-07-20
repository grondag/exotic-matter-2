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
package grondag.hard_science.machines.impl.logistics;

import javax.annotation.Nullable;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.matbuffer.BufferManager;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.resource.StorageType;

public class WaterPumpMachine extends AbstractSimpleMachine {
    protected WaterPumpMachine() {
        super();
    }

    @Override
    public boolean hasOnOff() {
        return true;
    }

    @Override
    public boolean hasRedstoneControl() {
        return true;
    }

    @Override
    protected @Nullable BufferManager createBufferManager() {
        return new BufferManager(this, 0L, StorageType.ITEM.MATCH_NONE, 0L, 0L, StorageType.FLUID.MATCH_NONE, VolumeUnits.KILOLITER.nL * 16);
    }
}
