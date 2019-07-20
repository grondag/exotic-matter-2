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
package grondag.hard_science.machines.support;

import grondag.exotic_matter.serialization.IMessagePlus;
import grondag.hard_science.machines.base.MachineTileEntity;

/**
 * For dispatching machine status updates to client for use in GUI or HUD. Is
 * based on tile entity, not simulation, because player can only be looking at
 * machine that are loaded and thus have an active tile entity. This allows
 * support for machines that aren't backed by the simulation.
 */
public interface IMachineStatusListener<T extends IMessagePlus> {

    /**
     * Sends new status information. This replaces prior status. Machine update
     * packets are small so we don't bother with deltas.
     */
    void handleStatusUpdate(MachineTileEntity sender, T update);

    /**
     * Will be called if the machine is destroyed or goes offline.
     */
    public void handleMachineDisconnect(MachineTileEntity sender);

    /**
     * Used by machine to remove orphaned/dead listeners.
     */
    public boolean isClosed();

}
