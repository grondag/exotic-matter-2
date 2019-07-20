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
package grondag.hard_science.machines.impl.production;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.PhotoElectricCell;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;

public class PhotoElectricMachine extends AbstractSimpleMachine {
    public PhotoElectricMachine() {
        super();
    }

    @Override
    protected DeviceEnergyManager createEnergyManager() {
        // Want to use a capacitor so that we don't have energy
        // loss of a battery if the energy can be used immediately.
        PowerContainer output = new PowerContainer(this, ContainerUsage.PUBLIC_BUFFER_OUT);
        output.configure(VolumeUnits.LITER.nL, BatteryChemistry.CAPACITOR);

        return new DeviceEnergyManager(this, new PhotoElectricCell(this), null, output);
    }
}
