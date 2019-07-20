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

import grondag.exotic_matter.varia.structures.SimpleUnorderedArrayList;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.endpoint.Port;

public abstract class AbstractTransportManager<T extends StorageType<T>> implements ITransportManager<T>, ITypedStorage<T> {

    protected final IDevice owner;
    protected final T storageType;
    /**
     * Carriers that can be used for transport in/out of this device. SHOULD ONLY BE
     * CHANGED FROM CONNECTION MANAGER THREAD
     */
    protected SimpleUnorderedArrayList<Carrier<T>> circuits = new SimpleUnorderedArrayList<Carrier<T>>();

    protected AbstractTransportManager(IDevice owner, T storageType) {
        super();
        this.owner = owner;
        this.storageType = storageType;
    }

    @Override
    public IDevice device() {
        return this.owner;
    }

    @Override
    public void refreshTransport() {
        assert this.confirmServiceThread() : "Transport logic running outside transport thread";
        this.circuits.clear();

        // on disconnect we'll get a null block manager
        IDeviceBlockManager blockMgr = this.device().blockManager();
        if (blockMgr == null)
            return;

        for (Port<T> port : blockMgr.getAttachedPorts(this.storageType)) {
            switch (port.getMode()) {
            case CARRIER:
            case DIRECT:
                // device is navigable via the external circuit for direct ports
                // and for carrier ports external/internal always the same
                // so can always use external circuit
                this.circuits.addIfNotPresent(port.externalCircuit());
                break;

            // bridge devices never enable transport for this device
            // (purpose is to enable transport for device on other side of bridge)
            // but we want to track them for debug/display purposes
            case BRIDGE:
                this.circuits.addIfNotPresent(port.internalCircuit());
                break;

            case DISCONNECTED:
            case NO_CONNECTION_CHANNEL_MISMATCH:
            case NO_CONNECTION_INCOMPATIBLE:
            case NO_CONNECTION_LEVEL_GAP:
            case NO_CONNECTION_STORAGE_TYPE:
                break;

            default:
                assert false : "missing enum mapping";
                break;

            }
        }
    }

    @Override
    public T storageType() {
        return this.storageType;
    }

    @Override
    public boolean isConnectedTo(Carrier<T> circuit) {
        return this.circuits.contains(circuit);
    }

    @Override
    public boolean hasAnyCircuit() {
        return !this.circuits.isEmpty();
    }

}
