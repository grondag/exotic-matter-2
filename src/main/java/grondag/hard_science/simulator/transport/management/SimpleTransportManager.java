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
package grondag.hard_science.simulator.transport.management;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.routing.Legs;

/**
 * Transport manager for single carrier
 */
public class SimpleTransportManager<T extends StorageType<T>> extends AbstractTransportManager<T> {
    /**
     * Used to cache legs information if this device is attached to more than one
     * circuit. Otherwise will rely on cache in the single circuit and will always
     * be null.
     */
    protected Legs<T> legs = null;

    public SimpleTransportManager(IDevice owner, T storageType) {
        super(owner, storageType);
    }

    @Override
    public Legs<T> legs(IResource<T> forResource) {
        if (this.circuits.isEmpty())
            return Legs.emptyLegs();

        if (this.circuits.size() == 1)
            return this.circuits.get(0).legs();

        // more than one circuit, so check cache
        if (this.legs == null || !this.legs.isCurrent()) {
            this.legs = new Legs<T>(this.circuits);
        }
        return this.legs;
    }

    @Override
    public void refreshTransport() {
        super.refreshTransport();
        // force rebuild of legs
        this.legs = null;
    }
}
