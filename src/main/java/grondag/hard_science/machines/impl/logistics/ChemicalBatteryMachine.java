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
package grondag.hard_science.machines.impl.logistics;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;

public class ChemicalBatteryMachine extends AbstractSimpleMachine {

    public ChemicalBatteryMachine() {
        super();
    }

    @Override
    protected DeviceEnergyManager createEnergyManager() {
        PowerContainer battery = new PowerContainer(this, ContainerUsage.STORAGE);
        battery.configure(VolumeUnits.LITER.nL * 750L, BatteryChemistry.SILICON);

        return new DeviceEnergyManager(this, null, null, battery);
    }

    @Override
    public boolean hasOnOff() {
        return true;
    }

    @Override
    public boolean hasRedstoneControl() {
        return true;
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
    }
}