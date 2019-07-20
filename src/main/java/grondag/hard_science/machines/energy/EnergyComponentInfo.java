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
package grondag.hard_science.machines.energy;

import grondag.exotic_matter.serialization.IMessagePlus;
import net.minecraft.network.PacketBuffer;

/**
 * Snapshot of static energy component descriptive information for display on
 * client.
 */
public class EnergyComponentInfo implements IMessagePlus {
    /**
     * See {@link IEnergyComponent#maxStoredEnergyJoules()}
     */
    public long maxStoredEnergyJoules() {
        return this.maxStoredEnergyJoules;
    }

    private long maxStoredEnergyJoules;

    /**
     * See {@link IEnergyComponent#maxPowerInputWatts()}
     */
    public float maxPowerInputWatts() {
        return this.maxPowerInputWatts;
    }

    private float maxPowerInputWatts;

    /**
     * See {@link IEnergyComponent#maxPowerOutputWatts()}
     */
    public float maxPowerOutputWatts() {
        return this.maxPowerOutputWatts;
    }

    private float maxPowerOutputWatts;

    public EnergyComponentInfo() {
    }

    public EnergyComponentInfo(IEnergyComponent from) {
        this.maxStoredEnergyJoules = from.maxStoredEnergyJoules();
        this.maxPowerInputWatts = from.maxPowerInputWatts();
        this.maxPowerOutputWatts = from.maxPowerOutputWatts();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.maxStoredEnergyJoules = pBuff.readVarLong();
        this.maxPowerInputWatts = pBuff.readFloat();
        this.maxPowerOutputWatts = pBuff.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeVarLong(this.maxStoredEnergyJoules);
        pBuff.writeFloat(this.maxPowerInputWatts);
        pBuff.writeFloat(this.maxPowerOutputWatts);
    }
}
