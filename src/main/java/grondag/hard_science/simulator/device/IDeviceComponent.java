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
package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.management.ITransportManager;

public interface IDeviceComponent extends IDomainMember {
    public IDevice device();

    /**
     * Shorthand for {@link #device()#getDomain()}
     */
    @Override
    public default @Nullable IDomain getDomain() {
        return this.device().getDomain();
    }

    /**
     * Implement and call from {@link IDevice#onConnect()} if this component needs
     * to do something when connection happens.
     */
    default void onConnect() {
    }

    /**
     * Implement and call from {@link IDevice#onDisconnect()} if this component
     * needs to do something when disconnect happens.
     */
    default void onDisconnect() {
    }

    /**
     * Shorthand for {@link #device()#isConnected()}
     */
    public default boolean isConnected() {
        return this.device().isConnected();
    }

    /**
     * Shorthand for {@link #device()#setDirty()}
     */
    public default void setDirty() {
        this.device().setDirty();
    }

    /** Shorthand for {@link #device()#isOn()} */
    public default boolean isOn() {
        return this.device().isOn();
    }

    /** Shorthand for {@link #device()#powerTransport() */
    public default ITransportManager<StorageTypePower> powerTransport() {
        return this.device().powerTransport();
    }

    /** Shorthand for {@link #device()#itemTransport() */
    public default ITransportManager<StorageTypeStack> itemTransport() {
        return this.device().itemTransport();
    }

    /** Shorthand for {@link #device()#fluidTransport() */
    public default ITransportManager<StorageTypeFluid> fluidTransport() {
        return this.device().fluidTransport();
    }

}
