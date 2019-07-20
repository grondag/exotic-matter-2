package grondag.hard_science.machines.impl.logistics;

import grondag.hard_science.machines.base.AbstractSimpleMachine;
import grondag.hard_science.machines.energy.BatteryChemistry;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.matter.VolumeUnits;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.PowerContainer;

public class ChemicalBatteryMachine extends AbstractSimpleMachine
{
    
    public ChemicalBatteryMachine()
    {
        super();
    }

    @Override
    protected DeviceEnergyManager createEnergyManager()
    {
        PowerContainer battery = new PowerContainer(this, ContainerUsage.STORAGE);
        battery.configure(VolumeUnits.LITER.nL * 750L, BatteryChemistry.SILICON);
        
        return new DeviceEnergyManager(
                this,
                null,
                null, 
                battery);
    }
    
    @Override
    public boolean hasOnOff()
    {
        return true;
    }

    @Override
    public boolean hasRedstoneControl()
    {
        return true;
    }

    @Override
    public void onDisconnect()
    {
        super.onDisconnect();
    }
}
