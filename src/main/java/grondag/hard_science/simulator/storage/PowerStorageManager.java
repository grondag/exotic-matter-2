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
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.storage.PowerStorageEvent.AfterPowerStorageConnect;
import grondag.hard_science.simulator.storage.PowerStorageEvent.BeforePowerStorageDisconnect;
import grondag.hard_science.simulator.storage.PowerStorageEvent.PowerCapacityChange;
import grondag.hard_science.simulator.storage.PowerStorageEvent.PowerStoredUpdate;

/**
 * Main purpose is to hold type-specific event handlers.
 */
public class PowerStorageManager extends StorageManager<StorageTypePower> {
    public PowerStorageManager() {
        super(StorageType.POWER);
    }

    @Subscribe
    public void afterStorageConnect(AfterPowerStorageConnect event) {
        this.addStore(event.storage);
    }

    @Subscribe
    public void beforePowerStorageDisconnect(BeforePowerStorageDisconnect event) {
        this.removeStore(event.storage);
    }

    @Subscribe
    public void onPowerUpdate(PowerStoredUpdate event) {
        if (event.delta > 0) {
            this.notifyAdded(event.storage, event.resource, event.delta, event.request);
        } else {
            this.notifyTaken(event.storage, event.resource, -event.delta, event.request);
        }
    }

    @Subscribe
    public void onCapacityChange(PowerCapacityChange event) {
        this.notifyCapacityChanged(event.delta);
    }
}
