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
 * Snapshot of dynamic energy component descriptive information for display on
 * client.
 */
public class EnergyComponentStatus implements IMessagePlus {
    /**
     * See #IEnergyComponent{@link #storedEnergyJoules}
     */
    public long storedEnergyJoules() {
        return this.storedEnergyJoules;
    }

    private long storedEnergyJoules;

    /**
     * See #IEnergyComponent{@link #powerInputWatts}
     */
    public float powerInputWatts() {
        return this.powerInputWatts;
    }

    private float powerInputWatts;

    /**
     * See {@link IEnergyComponent#powerOutputWatts()}
     */
    public float powerOutputWatts() {
        return this.powerOutputWatts;
    }

    private float powerOutputWatts;

    public EnergyComponentStatus() {
    }

    public EnergyComponentStatus(IEnergyComponent from) {
        this.storedEnergyJoules = from.storedEnergyJoules();
        this.powerInputWatts = from.powerInputWatts();
        this.powerOutputWatts = from.powerOutputWatts();
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.storedEnergyJoules = pBuff.readVarLong();
        this.powerInputWatts = pBuff.readFloat();
        this.powerOutputWatts = pBuff.readFloat();
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeVarLong(this.storedEnergyJoules);
        pBuff.writeFloat(this.powerInputWatts);
        pBuff.writeFloat(this.powerOutputWatts);
    }
}
