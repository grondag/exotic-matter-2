package grondag.hard_science.machines.impl.production;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.energy.PhotoElectricCell;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;

public class PhotoElectricMachine extends AbstractSimpleMachine
{
    public PhotoElectricMachine()
    {
        super();
    }
    
    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        // Want to use a capacitor so that we don't have energy
        // loss of a battery if the energy can be used immediately.
        PowerContainer output = new PowerContainer(this, ContainerUsage.PUBLIC_BUFFER_OUT);
        output.configure(VolumeUnits.LITER.nL, BatteryChemistry.CAPACITOR);
        
        return new DeviceEnergyManager(
                this,
                new PhotoElectricCell(this), 
                null,
                output);
    }
}
