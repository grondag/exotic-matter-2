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
package grondag.hard_science.simulator.storage;

import com.google.common.eventbus.Subscribe;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.storage.FluidStorageEvent.AfterFluidStorageConnect;
import grondag.hard_science.simulator.storage.FluidStorageEvent.BeforeFluidStorageDisconnect;
import grondag.hard_science.simulator.storage.FluidStorageEvent.FluidCapacityChange;
import grondag.hard_science.simulator.storage.FluidStorageEvent.FluidStoredUpdate;

/**
 * Main purpose is to hold type-specific event handlers.
 */
public class FluidStorageManager extends StorageManager<StorageTypeFluid> {
    public FluidStorageManager() {
        super(StorageType.FLUID);
    }

    @Subscribe
    public void afterStorageConnect(AfterFluidStorageConnect event) {
        this.addStore(event.storage);
    }

    @Subscribe
    public void beforeFluidStorageDisconnect(BeforeFluidStorageDisconnect event) {
        this.removeStore(event.storage);
    }

    @Subscribe
    public void onFluidUpdate(FluidStoredUpdate event) {
        if (event.delta > 0) {
            this.notifyAdded(event.storage, event.resource, event.delta, event.request);
        } else {
            this.notifyTaken(event.storage, event.resource, -event.delta, event.request);
        }
    }

    @Subscribe
    public void onCapacityChange(FluidCapacityChange event) {
        this.notifyCapacityChanged(event.delta);
    }
}
