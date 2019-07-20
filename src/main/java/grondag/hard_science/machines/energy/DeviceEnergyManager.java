package grondag.hard_science.machines.energy;

import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.IReadWriteNBT;
import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.exotic_matter.simulator.ISimulationTickable;
import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.ComponentRegistry;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.device.IDeviceComponent;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.PowerResource;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.storage.ContainerUsage;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.storage.PowerContainer;
import grondag.hard_science.simulator.storage.PowerStorageManager;
import grondag.hard_science.simulator.storage.StorageManager;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Power supplies have components that occur in the following 
 * combinations:<p>
 * <li>Battery - power storage devices</li>
 * <li>Input Buffer - power consuming devices</li>
 * <li>Generator, Output Buffer - power producing devices</li>
 * <li>Input Buffer, Generator, Output Buffer - power consuming devices
 * that contain an integrated generator</li><p>
 * 
 * Input buffer - necessary if this device consumes
 * power. Device draws power from the buffer during
 * the device tick.  Power is replenished from the
 * local output buffer (if there is one) or from other
 * output buffers or batteries on the power network if
 * the device is connected and local output is unavailable.<p>
 * 
 * Generator - a fuel cell, PE cell, or other component
 * within the device that can generate energy. If present,
 * power supply must have an output buffer to accept the
 * generated energy during the device tick.<p>
 * 
 * Output buffer - necessary if this device houses any
 * kind of generator.  Accepts generated energy during the
 * device tick. Tasks on the power service thread will then
 * redistribute the energy as needed, either locally within
 * the device (if it has an input buffer) or within the power 
 * storage network if the device is connected and demand exists.<p>
 * 
 * Battery - Will only be present if this device <em>is</em> a battery
 * and that is the only function it serves. In that case, the battery
 * will be the only power component in the device/power supply.
 * All transfers in and out will happen on the power service thread.
 * Any device that consumes or generates power will use input and/or
 * output buffer(s) instead of a battery so that device operations 
 * can complete during device tick without waiting for the service thread.<p>
 * 
 * GENERAL NOTES<p>
 * 
 * All stored energy is represented in Joules (aka Watt-seconds).<br>
 * All power input/output represents as Watts.<p>
 * 
 * Why not use IEnergyStorage or similar?<br>
 * 1) Explicitly want to use standard power/energy units. (watts/joules) <br>
 * 2) Want to support larger quantities (long vs int) <br>
 * 3) Want ability to reject partial input/output
 */
public class DeviceEnergyManager implements IReadWriteNBT, IDeviceComponent, ISimulationTickable
{
    private static final String NBT_MACHINE_GENERATOR = NBTDictionary.claim("generator");
    private static final String NBT_ENERGY_OUTPUT_BUFFER = NBTDictionary.claim("energyOutput");
    private static final String NBT_ENERGY_INPUT_BUFFER = NBTDictionary.claim("energyInput");
    
    private final IDevice owner;
    private AbstractGenerator generator;
    
    private PowerContainer outputContainer;
    private PowerContainer inputContainer;
    
    /**
     * If we scheduled something to power service during the 
     * last tick, the future for that task.  We don't want to
     * schedule another task until previous is complete. Retain
     * the reference here so that we can check. Will be null
     * if we have never submitted.
     */
    private Future<?> powerTickFuture = null;
    
    /**
     * Ticks since we last tried to top off power in the input buffer
     * if it isn't urgent that we do so.  Prevents going to the power
     * network every tick for small amounts.
     */
    private byte topOffCounter = 0;
    
    /**
     * Set to true if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    private boolean isFailureCause;
    
    public DeviceEnergyManager(
            IDevice owner, 
            @Nullable AbstractGenerator generator, 
            @Nullable PowerContainer inputContainer,
            @Nullable PowerContainer outputContainer)
    {
        super();
        this.owner = owner;
        this.generator = generator;
        this.inputContainer = inputContainer;
        this.outputContainer = outputContainer;
    }
 
    public boolean isEmpty()
    {
        return this.inputContainer == null && this.outputContainer == null;
    }
    
    @Override
    public IDevice device()
    {
        return this.owner;
    }
    
    @Nullable
    public AbstractGenerator generator() { return this.generator; }
    
    /**
     * Will be a storage if this is a battery-type device. 
     * Will be an output buffer if device has a generator. 
     * Will be null if device only consumes power.
     */
    @Nullable
    public PowerContainer outputContainer() { return this.outputContainer; }
    
    @Nullable
    public PowerContainer inputContainer() { return this.inputContainer; }
    
    /**
     * Recent energy consumption level by device from input buffer.
     */
    public float deviceDrawWatts()
    {
        return this.inputContainer == null 
            ? 0 
            : MachinePower.joulesPerTickToWatts(this.inputContainer.energyOutputLastTickJoules());
    }

    /**
     * Highest possible rate of power draw from this power
     * supply for use within the local device. Based on {@link #maxDeviceDrawPerTick()}
     */
    public float maxDeviceDrawWatts()
    {
        return MachinePower.joulesPerTickToWatts(this.maxDeviceDrawPerTick());
    }

    /**
     * 
     * Effective limit for {@link #provideEnergy(long, boolean, boolean)}.  In joules.
     */
    public long maxDeviceDrawPerTick()
    {
        return this.inputContainer == null 
                ? 0 : inputContainer.maxEnergyOutputJoulesPerTick();  
    }
  
    public long maxStoredEnergyJoules()
    {
        return (this.inputContainer == null ? 0 : this.inputContainer.maxStoredEnergyJoules())
             + (this.outputContainer == null ? 0 : this.outputContainer.maxStoredEnergyJoules());
    }
    
    public long storedEnergyJoules()
    {
        return (this.inputContainer == null ? 0 : this.inputContainer.storedEnergyJoules())
             + (this.outputContainer == null ? 0 : this.outputContainer.storedEnergyJoules());
    }
    
    public float netStorageWatts()
    {
        return (this.inputContainer == null ? 0 : this.inputContainer.netWattsLastTick())
                + (this.outputContainer == null ? 0 : this.outputContainer.netWattsLastTick());

    }
   
    /**
     * Consumes energy from this machine's input container if it has one.
     * Also clears failure flag if a non-simulated attempt is successful.<p>
     * 
     * @param maxOutput
     *            Maximum amount of energy to be extracted, in joules.<br>
     *            Limited by {@link #maxDeviceDrawPerTick()}.
     *            
     * @param allowPartial
     *            If false, no energy will be extracted unless the entire requested amount can be provided.
     *            
     * @param simulate
     *            If true, result will be simulated and no state change occurs.
     *            
     * @return Energy extracted (or that would have been have been extracted, if simulated) in joules.
     */
    public long provideEnergy(long maxOutput, boolean allowPartial, boolean simulate)
    {
        if(maxOutput <= 0 || this.inputContainer == null) return 0; 
        
        long result = this.inputContainer.provideEnergy(maxOutput, allowPartial, simulate);
        
        // forgive self for prior failures if managed to do something this time
        if(result > 0 && !simulate && this.isFailureCause) this.setFailureCause(false);
        
        return result;
    }
    
    /**
     * True if this provider's (low) level has recently caused a 
     * processing failure.  Sent to clients for rendering to inform player
     * but not saved to world because is transient information.
     */
    public boolean isFailureCause()
    {
        return isFailureCause;
    }

    /**
     * See {@link #isFailureCause()}
     */
    public void setFailureCause(boolean isFailureCause)
    {
        this.isFailureCause = isFailureCause;
    }
    
    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        this.generator = tag.hasKey(NBT_MACHINE_GENERATOR) 
                ? (AbstractGenerator)ComponentRegistry.fromNBT(
                        this.device(), tag.getCompoundTag(NBT_MACHINE_GENERATOR))
                : null;
                        
        if(this.outputContainer != null) this.outputContainer.deserializeNBT(tag.getCompoundTag(NBT_ENERGY_OUTPUT_BUFFER));
        if(this.inputContainer != null) this.inputContainer.deserializeNBT(tag.getCompoundTag(NBT_ENERGY_INPUT_BUFFER));
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        if(this.generator != null) tag.setTag(NBT_MACHINE_GENERATOR, ComponentRegistry.toNBT(this.generator));
        if(this.outputContainer != null) tag.setTag(NBT_ENERGY_OUTPUT_BUFFER, this.outputContainer.serializeNBT());
        if(this.inputContainer != null) tag.setTag(NBT_ENERGY_INPUT_BUFFER, this.inputContainer.serializeNBT());
    }

    @Override
    public boolean doesUpdateOffTick()
    {
        return !this.isEmpty();
    }
   
    @Override
    public void doOffTick()
    {
        // don't do stuff if device is off
        if(!this.isOn()) return;
        
        // try to fill input container if either of these is true:
        // 1) it has room and we haven't topped it off in a while
        // 2) it doesn't have enough power to satisfy max draw next tick
        boolean needsPowerTick = false;
        if(this.inputContainer != null)
        {
            if(this.inputContainer.availableCapacity() > 0)
            {
                if(this.topOffCounter > 10
                   || this.inputContainer.usedCapacity() < this.inputContainer.maxEnergyOutputJoulesPerTick())
                {
                    this.topOffCounter = 0;
                    needsPowerTick = true;
                }
                else this.topOffCounter++;
            }
        }
        // try to generate
        if(this.generator != null && this.outputContainer != null)
        {
            this.generator.generate(this.outputContainer);
               
            needsPowerTick = needsPowerTick || 
                this.outputContainer.usedCapacity() >= 0;
        }
        
        if(powerTickFuture != null)
        {
            if(powerTickFuture.isDone())
            {
                this.powerTickFuture = null;
            }
            else
            {
                assert false : "Power service overload : service tick did not complete before next device tick.";
            }
        }
        
        // if need to transfer in or out of buffers, 
        // schedule task for that purpose
        if(needsPowerTick && powerTickFuture == null)
        {
            this.powerTickFuture = LogisticsService.POWER_SERVICE.executor.submit(() -> 
            {
                this.fillInputIfNeeded();
                this.emptyOutputIfNeeded();
            });
        }
    }

    private void fillInputIfNeeded()
    {
        if(this.inputContainer == null || this.inputContainer.availableCapacity() == 0)
            return;
        
        StorageManager<StorageTypePower> manager = this.getDomain().getCapability(PowerStorageManager.class);
            
        // try local first
        if(this.outputContainer != null)
        {
            long taken = outputContainer.takeUpTo(
                    PowerResource.JOULES, 
                    this.inputContainer.availableCapacity(),
                    false);
            if(taken > 0)
            {
                long accepted = this.inputContainer.add(
                        PowerResource.JOULES, 
                        taken,
                        false);
                
                assert accepted == taken
                        : "Device input buffer failed to accept input";
            }
        }
        
        // if input still not full, try energy network
        if(this.inputContainer.availableCapacity() == 0 
                || !this.powerTransport().hasAnyCircuit()
                || manager.getQuantityAvailable(PowerResource.JOULES) == 0)
            return;
        
        List<IResourceContainer<StorageTypePower>> stores 
            = manager.findSourcesFor(PowerResource.JOULES, this.device());
        
        if(!stores.isEmpty())
        {
            for(IResourceContainer<StorageTypePower> s : stores)
            {
                LogisticsService.POWER_SERVICE.sendResourceNow(
                    PowerResource.JOULES, 
                    this.inputContainer.availableCapacity(), 
                    s.device(), 
                    this.device(), 
                    false, 
                    false, 
                    null);
                
                if(this.inputContainer.availableCapacity() == 0) break;
            }
        }
    }
    
    private void emptyOutputIfNeeded()
    {
        if(this.outputContainer == null 
                || this.outputContainer.containerUsage() != ContainerUsage.PUBLIC_BUFFER_OUT
                || !this.powerTransport().hasAnyCircuit()) 
            return;
        
        long targetAmount = this.outputContainer.usedCapacity();
        
        StorageManager<StorageTypePower> manager = this.getDomain().getCapability(PowerStorageManager.class);
        
        // try to empty output container if it is an output buffer
        // that is close to being full
        // and dedicated storage is available
        List<IResourceContainer<StorageTypePower>> dumps 
            = manager.findSpaceFor(PowerResource.JOULES, this.device());
        
        if(!dumps.isEmpty())
        {
            for(IResourceContainer<StorageTypePower> s : dumps)
            {
                targetAmount -= LogisticsService.POWER_SERVICE.sendResourceNow(
                        PowerResource.JOULES, 
                        targetAmount, 
                        this.device(), 
                        s.device(), 
                        false, 
                        false, 
                        null);
                
                if(targetAmount <= 0) break;
            }
        }
    }
  
    /**
     * Routes to appropriate power container. 
     * See {@link PowerContainer#takeUpTo(IResource, long, boolean, IProcurementRequest)}
     */
    public long takeUpTo(IResource<StorageTypePower> resource, long quantity, boolean simulate, NewProcurementTask<StorageTypePower> request)
    {
        return this.outputContainer == null ? 0 
            : this.outputContainer.takeUpTo(resource, quantity, simulate, request);
    }

    /**
     * Routes to appropriate power container. 
     * See {@link PowerContainer#add(IResource, long, boolean, IProcurementRequest)}
     */
    public long add(IResource<StorageTypePower> resource, long quantity, boolean simulate,  NewProcurementTask<StorageTypePower> request)
    {
        if(this.inputContainer != null)
        {
            return this.inputContainer.add(resource, quantity, simulate, request);
        }
        else if(this.outputContainer != null)
        {
            return this.outputContainer.add(resource, quantity, simulate, request);
        }
        return 0;
    }

//    private String formatedAvailableEnergyJoules;
//    
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String formatedAvailableEnergyJoules()
//    {
//        if(this.formatedAvailableEnergyJoules == null)
//        {
//            this.formatedAvailableEnergyJoules = MachinePower.formatEnergy(this.availableEnergyJoules(), false);
//        }
//        return this.formatedAvailableEnergyJoules;
//    }
//
//    private String formattedAvgNetPowerGainLoss;
//    
//    @Override
//    @SideOnly(Side.CLIENT)
//    public String formattedAvgNetPowerGainLoss()
//    {
//        if(this.formattedAvgNetPowerGainLoss == null)
//        {
//            this.formattedAvgNetPowerGainLoss = MachinePower.formatPower(this.avgNetPowerGainLoss(), true);
//        }
//        return this.formattedAvgNetPowerGainLoss;
//    }

    @Override
    public void onConnect()
    {
        if(this.inputContainer != null) this.inputContainer.onConnect();
        if(this.outputContainer != null) this.outputContainer.onConnect();
    }

    @Override
    public void onDisconnect()
    {
        if(this.inputContainer != null) this.inputContainer.onDisconnect();
        if(this.outputContainer != null) this.outputContainer.onDisconnect();
    }
}
