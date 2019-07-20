package grondag.hard_science.simulator.storage;

import com.google.common.eventbus.Subscribe;

import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.storage.PowerStorageEvent.AfterPowerStorageConnect;
import grondag.hard_science.simulator.storage.PowerStorageEvent.BeforePowerStorageDisconnect;
import grondag.hard_science.simulator.storage.PowerStorageEvent.PowerCapacityChange;
import grondag.hard_science.simulator.storage.PowerStorageEvent.PowerStoredUpdate;

/**
 * Main purpose is to hold type-specific event handlers.
 */
public class PowerStorageManager extends StorageManager<StorageTypePower>
{
    public PowerStorageManager()
    {
        super(StorageType.POWER);
    }

    @Subscribe
    public void afterStorageConnect(AfterPowerStorageConnect event)
    {
        this.addStore(event.storage);
    }
    
    @Subscribe
    public void beforePowerStorageDisconnect(BeforePowerStorageDisconnect event)
    {
        this.removeStore(event.storage);
    }
    
    @Subscribe
    public void onPowerUpdate(PowerStoredUpdate event)
    {
        if(event.delta > 0)
        {
            this.notifyAdded(event.storage, event.resource, event.delta, event.request);
        }
        else
        {
            this.notifyTaken(event.storage, event.resource, -event.delta, event.request);
        }
    }
    
    @Subscribe
    public void onCapacityChange(PowerCapacityChange event)
    {
        this.notifyCapacityChanged(event.delta);
    }
}
