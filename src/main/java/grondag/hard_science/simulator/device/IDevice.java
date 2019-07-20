package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.ISimulationTickable;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.exotic_matter.simulator.persistence.AssignedNumber;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.fermion.serialization.IReadWriteNBT;
import grondag.fermion.varia.Base32Namer;
import grondag.fermion.varia.Useful;
import grondag.fermion.world.Location.ILocated;
import grondag.hard_science.HsConfig;
import grondag.hard_science.machines.energy.DeviceEnergyManager;
import grondag.hard_science.machines.matbuffer.BufferManager;
import grondag.hard_science.simulator.device.blocks.IDeviceBlockManager;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeFluid;
import grondag.hard_science.simulator.resource.StorageType.StorageTypePower;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.FluidContainer;
import grondag.hard_science.simulator.storage.ItemContainer;
import grondag.hard_science.simulator.transport.management.ITransportManager;

public interface IDevice extends 
    IIdentified, ILocated, IDomainMember, IReadWriteNBT, ISimulationTickable
{
    public default boolean doesPersist() { return true; }
    
    /**
     * Set to true at the end of {@link #onConnect()} and 
     * set to false at the end of {@link #onDisconnect()}.
     */
    public boolean isConnected();
    
    /**
     * Null if this device has no world block delegates.
     */
    @Nullable 
    public default IDeviceBlockManager blockManager() { return null; }

    public default boolean hasBlockManager() { return this.blockManager() != null; }
    
    public default boolean isOn() { return true; }
    
    /**
     * Null if this device has no transport facilities.
     * If null, implies device has no transport nodes.
     */
    @Nullable 
    public default ITransportManager<?> tranportManager(StorageType<?> storageType) { return null; }

    public default  boolean hasTransportManager(StorageType<?> storageType) { return this.tranportManager(storageType) != null; }
    
    @SuppressWarnings("unchecked")
    public default ITransportManager<StorageTypePower> powerTransport() { return (ITransportManager<StorageTypePower>) this.tranportManager(StorageType.POWER); }

    @SuppressWarnings("unchecked")
    public default ITransportManager<StorageTypeStack> itemTransport() { return (ITransportManager<StorageTypeStack>) this.tranportManager(StorageType.ITEM); }

    @SuppressWarnings("unchecked")
    public default ITransportManager<StorageTypeFluid> fluidTransport() { return (ITransportManager<StorageTypeFluid>) this.tranportManager(StorageType.FLUID); }

    /**
     * Signal that device should perform internal
     * initialization and register device blocks
     * via {@link DeviceManager#addDeviceBlock}.
     * Device should already be added to device 
     * manager when this is called.
     * 
     * Called exactly once either by ...<br>
     * 1) Device Manager after all simulation components are deserialized.<br>
     * ...or...<br>
     * 2) Block placement logic after domain /location are set and 
     * and deserialization from stack (if applies) is complete.<p>
     */
    public void onConnect();
    
    public void onDisconnect();
    
    @Override
    public default AssignedNumber idType()
    {
        return AssignedNumber.DEVICE;
    }
    
    /**
     * May want to cache in implementation if frequently used.
     */
    public default String machineName()
    {
        long l = Useful.longHash(this.getLocation().world().getSeed() ^ this.getId());
        return Base32Namer.makeName(l, HsConfig.MACHINES.filterOffensiveMachineNames);
    }
    
    public default void setDirty()
    {
        DeviceManager.instance().setDirty();
    }
    
    /**
     * Called by transport system for this device to handle outbound transport requests.
     * SHOULD ONLY BE CALLED FROM LOGISTICS SERVICE to ensure consistency of results.
     */
    public long onProduce(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request);

    /**
     * Called by transport system for this device to handle inbound transport requests
     * SHOULD ONLY BE CALLED FROM LOGISTICS SERVICE to ensure consistency of results.
     */
    public long onConsume(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request);

    /**
     * Called after ports on this device are attached
     * or detached to notify transport manager to update transport
     * addressability for this device.  Has no effect if this device
     * lacks transport facilities for the given storage type.
     * 
     * SHOULD ONLY BE CALLED FROM CONNECTION MANAGER THREAD.
     */
    public default void refreshTransport(StorageType<?> storageType)
    {
        if(this.hasTransportManager(storageType))
        {
            this.tranportManager(storageType).refreshTransport();
        }
    }

    
    /**
     * Implement if device has fluid storage.  Will be null if not.
     */
    @Nullable 
    public default FluidContainer fluidStorage() {return null;}
    
    /**
     * Convenience for <code>{@link #powerStorage()} != null</code>
     */
    public default boolean hasFluidStorage() { return this.fluidStorage() != null; }

    /**
     * Implement if device has item storage. Will be null if not.
     */
    @Nullable 
    public default ItemContainer itemStorage() {return null;}

    /**
     * Convenience for <code>{@link #itemStorage()} != null</code>
     */
    public default boolean hasItemStorage() { return this.itemStorage() != null; }
    
    public DeviceEnergyManager energyManager();

    /** 
     * If this tile has a material buffer, gives access.  Null if not.
     */
    BufferManager getBufferManager();
    
    /**
     * True if supports channels for item & power transport circuit segregation.
     */
    public default boolean hasChannel() { return this.getChannel() != CHANNEL_UNSUPPORTED; }
    
    public static final int CHANNEL_UNSUPPORTED = -1;
    
    /**
     * Configured transport channel for item & power connection segregation.
     * Will return {@link #CHANNEL_UNSUPPORTED} if not supported.  
     * Is generally the block meta/species.
     * Default implementation is no channel support.
     */
    public default int getChannel() { return CHANNEL_UNSUPPORTED; }
        
}

