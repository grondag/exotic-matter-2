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
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.routing.Legs;

/**
 * Contains and manages the transport components of a device.
 */
public interface ITransportManager<T extends StorageType<T>> extends IDeviceComponent {

    /**
     * Called after transport ports on this device are attached or detached to
     * notify transport manager to update transport addressability for this device.
     * Call happens via {@link IDevice#refreshTransport(StorageType)}.
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public void refreshTransport();

    /**
     * True if this device is attached to any circuit. If false, implies any
     * off-device transport request will fail.
     */
    public boolean hasAnyCircuit();

    /**
     * All legs accessible from circuits on which this device is connected and which
     * can be used for the given bulkResource. Resource only matters for fluid
     * transport at current time and can be null for those storage types but must be
     * provided for fluid or result will always be empty.
     */
    public Legs<T> legs(IResource<T> forResource);

    /**
     * True if can send/receive on the given circuit. Used to validate transport
     * routes.
     */
    public boolean isConnectedTo(Carrier<T> circuit);
}