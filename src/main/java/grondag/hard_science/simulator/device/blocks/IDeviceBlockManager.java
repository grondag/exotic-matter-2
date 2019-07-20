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
package grondag.hard_science.simulator.device.blocks;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.endpoint.Port;

/**
 * Manages the device block delegates for a device.
 * 
 * Hierarchical structure Device DeviceBlock(s) Connector Instance : Connection
 * Instance Port Instance : Transport Node
 * 
 */
public interface IDeviceBlockManager {
    /**
     * All device blocks for this device.
     */
    public Collection<IDeviceBlock> blocks();

    /**
     * Will be called by owning device when added to world. Should register all
     * device blocks with DeviceWorldManager. Happens before transport manager
     * connect.
     */
    public void connect();

    /**
     * Will be called by owning device when removed from world. Should unregister
     * all device blocks with DeviceWorldManager.
     */
    public void disconnect();

    /**
     * Get all currently attached ports on this device with the given StorageType,
     * irrespective of channel
     *
     * @param storageType Matches ports of this type.
     */
    public <T extends StorageType<T>> List<Port<T>> getAttachedPorts(T storageType);

}
