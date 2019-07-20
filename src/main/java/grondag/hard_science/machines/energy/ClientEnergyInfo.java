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
public class ClientEnergyInfo implements IMessagePlus {
    private float maxGenerationWatts;
    private float maxDeviceDrawWatts;
    private float maxDischargeWatts;
    private float maxChargeWatts;
    private long maxStoredEnergyJoules;

    // if ever split this into static/dynamic packets
    // dynamic stuff is here
    private boolean isFailureCause;
    private float generationWatts;
    private float deviceDrawWatts;
    private float netStorageWatts;
    private long storedEnergyJoules;

    public ClientEnergyInfo() {
    }

    public ClientEnergyInfo(DeviceEnergyManager powerSupply) {
        if (powerSupply.generator() != null) {
            this.maxGenerationWatts = powerSupply.generator().maxPowerOutputWatts();
        }

        this.maxStoredEnergyJoules = powerSupply.maxStoredEnergyJoules();

        if (powerSupply.inputContainer() != null) {
            this.maxDeviceDrawWatts = powerSupply.inputContainer().maxPowerOutputWatts();
            this.maxChargeWatts = powerSupply.inputContainer().maxPowerInputWatts();
        }
        if (powerSupply.outputContainer() != null) {
            this.maxDischargeWatts = powerSupply.outputContainer().maxPowerOutputWatts();
            this.maxChargeWatts = powerSupply.outputContainer().maxPowerInputWatts();
        }

        // if ever split this into static/dynamic packets
        // dynamic stuff is here
        this.isFailureCause = powerSupply.isFailureCause();
        if (powerSupply.generator() != null) {
            this.generationWatts = powerSupply.generator().powerOutputWatts();
        }
        this.storedEnergyJoules = powerSupply.storedEnergyJoules();
        this.netStorageWatts = powerSupply.netStorageWatts();
        if (powerSupply.inputContainer() != null) {
            this.deviceDrawWatts = powerSupply.inputContainer().powerOutputWatts();
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff) {
        pBuff.writeFloat(this.maxChargeWatts);
        pBuff.writeFloat(this.maxDeviceDrawWatts);
        pBuff.writeFloat(this.maxDischargeWatts);
        pBuff.writeFloat(this.maxGenerationWatts);
        pBuff.writeLong(this.maxStoredEnergyJoules);

        pBuff.writeBoolean(this.isFailureCause);
        pBuff.writeFloat(this.deviceDrawWatts);
        pBuff.writeFloat(this.generationWatts);
        pBuff.writeFloat(this.netStorageWatts);
        pBuff.writeLong(this.storedEnergyJoules);
    }

    @Override
    public void fromBytes(PacketBuffer pBuff) {
        this.maxChargeWatts = pBuff.readFloat();
        this.maxDeviceDrawWatts = pBuff.readFloat();
        this.maxDischargeWatts = pBuff.readFloat();
        this.maxGenerationWatts = pBuff.readFloat();
        this.maxStoredEnergyJoules = pBuff.readLong();

        this.isFailureCause = pBuff.readBoolean();
        this.deviceDrawWatts = pBuff.readFloat();
        this.generationWatts = pBuff.readFloat();
        this.netStorageWatts = pBuff.readFloat();
        this.storedEnergyJoules = pBuff.readLong();
    }

    /**
     * Max rate of discharge for power storage.
     */
    public float maxDischargeWatts() {
        return this.maxDischargeWatts;
    }

    /**
     * Max rate of power production if this device contains a generator component.
     */
    public float maxGenerationWatts() {
        return this.maxGenerationWatts;
    }

    /**
     * Max sustained rate of power consumption by this device
     */
    public float maxDeviceDrawWatts() {
        return this.maxDeviceDrawWatts;
    }

    public boolean hasGenerator() {
        return this.maxGenerationWatts > 0;
    }

    /**
     * Max rate of energy storage charge.
     */
    public float maxChargeWatts() {
        return this.maxChargeWatts;
    }

    /**
     * Max energy storage charge
     */
    public double maxStoredEnergyJoules() {
        return this.maxStoredEnergyJoules;
    }

    public boolean isFailureCause() {
        return this.isFailureCause;
    }

    public long storedEnergyJoules() {
        return this.storedEnergyJoules;
    }

    /**
     * Negative if net discharge from energy storage, positive if net charge.
     */
    public float netStorageWatts() {
        return this.netStorageWatts;
    }

    public float deviceDrawWatts() {
        return this.deviceDrawWatts;
    }

    public float generationWatts() {
        return this.generationWatts;
    }
}
