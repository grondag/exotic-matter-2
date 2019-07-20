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
package grondag.hard_science.simulator.transport.endpoint;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.carrier.Carrier;
import grondag.hard_science.simulator.transport.carrier.CarrierLevel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DirectPortState<T extends StorageType<T>> extends Port<T> {
    /**
     * Physical device on which this port is present.
     */
    private final IDevice device;

    public DirectPortState(IDevice device, T storageType, PortConnector connector, CarrierLevel level, BlockPos pos, EnumFacing face) {
        super(storageType, PortFunction.DIRECT, connector, level, pos, face);
        this.device = device;
    }

    public DirectPortState(IDevice device, IPortDescription<T> spec, BlockPos pos, EnumFacing face) {
        super(spec.storageType(), PortFunction.DIRECT, spec.connector(), spec.level(), pos, face);
        this.device = device;
        this.setChannel(spec.getChannel());
    }

    @Override
    public @Nullable Carrier<T> internalCircuit() {
        return null;
    }

    @Override
    public IDevice device() {
        return this.device;
    }

    @Override
    public CarrierLevel level() {
        return this.externalLevel();
    }
}
