package grondag.hard_science.simulator.device;

import javax.annotation.Nullable;

import grondag.exotic_matter.simulator.domain.DomainManager;
import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.persistence.IIdentified;
import grondag.fermion.serialization.NBTDictionary;
import grondag.fermion.world.Location;
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
import grondag.hard_science.simulator.transport.management.ITransportManager;
import net.minecraft.nbt.CompoundTag;

public abstract class AbstractDevice implements IDevice
{
    private static final String NBT_DOMAIN_ID = NBTDictionary.claim("devDomID");
    private static final String NBT_CHANNEL= NBTDictionary.claim("devChan");
    
    private int id;
    private @Nullable Location location;
    
    /**
     * True if {@link #onConnect()} has run and {@link #onDisconnect()} has not.
     */
    private boolean isConnected = false;
    
    private int channel = 0;
    
    private int domainID = IIdentified.UNASSIGNED_ID;
    
    /** do not access directly - lazy lookup after deserialization */
    private @Nullable IDomain domain = null;
    
    /**
     * Initialized during just before super connect, set null after super disconnect
     */
    protected @Nullable IDeviceBlockManager blockManager = null; 
    
    protected final @Nullable ITransportManager<StorageTypeStack> itemTransportManager;
    protected final @Nullable ITransportManager<StorageTypeFluid> fluidTransportManager;
    protected final @Nullable ITransportManager<StorageTypePower> powerTransportManager;
    
    
    /** Levels of materials stored in this machine.  Is persisted to NBT. 
     * Null (default) means disabled. 
     */
    private final BufferManager bufferManager;
    
    /**
     * Energy manager for this device.
     */
    protected DeviceEnergyManager energyManager;
    
    protected AbstractDevice()
    {
        this.itemTransportManager = this.createItemTransportManager();
        this.fluidTransportManager = this.createFluidTransportManager();
        this.powerTransportManager = this.createPowerTransportManager();
        this.bufferManager = this.createBufferManager();
        this.energyManager = this.createEnergyManager();
    }
    
    /**
     * Override to enable item transport
     */
    protected @Nullable ITransportManager<StorageTypeStack> createItemTransportManager()
    {
        return null;
    }

    /**
     * Override to enable fluid transport
     */
    protected @Nullable ITransportManager<StorageTypeFluid> createFluidTransportManager()
    {
        return null;
    }
    
    /**
     * Override to enable power transport
     */
    protected @Nullable ITransportManager<StorageTypePower> createPowerTransportManager()
    {
        return null;
    }
    
    /**
     * If this machine has a material buffer, used to create a new instance.
     * May be used on client to create client-side delegate.
     */
    protected BufferManager createBufferManager()
    {
        return new BufferManager(
                this, 
                0L, 
                StorageType.ITEM.MATCH_NONE, 
                0L, 
                0L, 
                StorageType.FLUID.MATCH_NONE, 
                0L);
    }
    

    @Override
    public final BufferManager getBufferManager()
    {
        return this.bufferManager;
    }
    
    /** 
     * If this machine has an energy manager provider, gives access.  Null if not.
     */
    @Override
    public final DeviceEnergyManager energyManager()
    {
        return this.energyManager;
    }
    
    /**
     * Override to implement block manager functionality
     */
    protected @Nullable IDeviceBlockManager createBlockManager()
    {
        return null;
    }
    
    /**
     * Override to enable energy consumption / production.
     */
    protected DeviceEnergyManager createEnergyManager()
    {
        return new DeviceEnergyManager(this, null, null, null);
    }
    
    
    @Override
    public final @Nullable IDeviceBlockManager blockManager()
    {
        return this.blockManager;
    }
    
    @Override
    public @Nullable ITransportManager<?> tranportManager(StorageType<?> storageType)
    {
        switch(storageType.enumType)
        {
        case ITEM:
            return this.itemTransportManager;
            
        case POWER:
            return this.powerTransportManager;
        
        case FLUID:
            return this.fluidTransportManager;

        case PRIVATE:
            assert false : "Unsupported private storage type reference";

        default:
            return null;
        }
    }

    @Override
    public final boolean isConnected()
    {
        return this.isConnected;
    }
    
    @Override
    public int getIdRaw()
    {
        return this.id;
    }

    @Override
    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public @Nullable Location getLocation()
    {
        return this.location;
    }

    @Override
    public void setLocation(@Nullable Location loc)
    {
        this.location = loc;
    }

    @Nullable
    @Override
    public IDomain getDomain()
    {
        if(this.domain == null)
        {
            if(this.domainID != IIdentified.UNASSIGNED_ID)
                this.domain = DomainManager.instance().getDomain(this.domainID);
        }
        return this.domain;
    }
    
    /**
     * MUST NOT BE CALLED WHILE CONNECTED.
     * Storages, connections, etc. all rely on domain remaining
     * static while connected to transport network. If domain
     * is to be changed, disconnect, make the change, and reconnect.
     */
    public void setDomain(@Nullable IDomain domain)
    {
        if(this.isConnected) 
            throw new UnsupportedOperationException("Attempt to change device domain while connected.");
        
        this.domainID = domain == null ? IIdentified.UNASSIGNED_ID : domain.getId();
        this.domain = domain;
        this.setDirty();
    }

    
    @Override
    public void deserializeNBT(CompoundTag tag)
    {
        // would cause problems with devices that have already posted events to a domain
        assert this.domain == null
                : "Non-null domain during device deserialization.";

        if(tag != null)
        {
            this.deserializeID(tag);
            this.domainID = tag.getInt(NBT_DOMAIN_ID);
        }
         
        this.location = Location.fromNBT(tag);
        this.channel = tag.getInt(NBT_CHANNEL);
        if(this.bufferManager != null) this.bufferManager.deserializeNBT(tag);
        this.energyManager.deserializeNBT(tag);

    }

    @Override
    public void serializeNBT(CompoundTag tag)
    {
        this.serializeID(tag);
        Location.saveToNBT(location, tag);
        tag.putInt(NBT_DOMAIN_ID, this.domainID);
        tag.putInt(NBT_CHANNEL, this.channel);
        if(this.bufferManager != null) this.bufferManager.serializeNBT(tag);
        this.energyManager.serializeNBT(tag);

    }
    
    @Override
    public void onConnect()
    {
        this.blockManager = this.createBlockManager();
        this.blockManager.connect();
        this.energyManager.onConnect();
        this.bufferManager.onConnect();
        this.isConnected = true;
    }
    
    @Override
    public void onDisconnect()
    {
        if(this.blockManager != null)
        {
            this.blockManager.disconnect();
            this.blockManager = null;
        }
        this.energyManager.onDisconnect();
        this.bufferManager.onDisconnect();
        this.isConnected = false;
    }

    @Override
    public int getChannel()
    {
        return channel;
    }

    public void setChannel(int channel)
    {
        this.channel = channel;
    }
    
    /**
     * Override if your device handles transport requests. 
     * Base implementation handles service check.<p>
     * 
     * See {@link #onProduce(IResource, long, boolean, NewProcurementTask)}
     */
    @SuppressWarnings({ "unchecked", "null" })
    protected long onProduceImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request)
    { 
        switch(resource.storageType().enumType)
        {
        case FLUID:
            if(this.hasFluidStorage()) 
                return this.fluidStorage().takeUpTo((IResource<StorageTypeFluid>)resource, quantity, simulate, (NewProcurementTask<StorageTypeFluid>)request);
            else
            {
                FluidContainer output 
                    = this.getBufferManager().fluidOutput();
                if(output != null)
                    return output.takeUpTo((IResource<StorageTypeFluid>)resource, quantity, simulate, (NewProcurementTask<StorageTypeFluid>)request);
                }
            break;
            
        case ITEM:
            if(this.hasItemStorage()) 
                return this.itemStorage().takeUpTo((IResource<StorageTypeStack>)resource, quantity, simulate, (NewProcurementTask<StorageTypeStack>)request);
            else if(this.getBufferManager().itemOutput() != null)
                return this.getBufferManager().itemOutput().takeUpTo((IResource<StorageTypeStack>)resource, quantity, simulate, (NewProcurementTask<StorageTypeStack>)request);
            break;
            
        case POWER:
            return this.energyManager().takeUpTo((IResource<StorageTypePower>)resource, quantity, simulate, (NewProcurementTask<StorageTypePower>)request);

        case PRIVATE:
            assert false : "Unsupported private storage type reference";

        default:
            assert false : "Unhandled enum mapping";
        }
        return 0;
    }
    
    @Override
    public final long onProduce(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request)
    {
        assert resource.confirmServiceThread() 
            : "Transport logic running outside logistics service"; 
        return this.onProduceImpl(resource, quantity, simulate, request);
    }

    /**
     * Override if your device handles transport requests. 
     * Base implementation handles service check.<p>
     * 
     * See {@link #onConsume(IResource, long, boolean, NewProcurementTask)}
     */
    @SuppressWarnings({ "unchecked", "null" })
    protected long onConsumeImpl(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request)
    {
        switch(resource.storageType().enumType)
        {
        case FLUID:
            if(this.hasFluidStorage()) 
                return this.fluidStorage().add((IResource<StorageTypeFluid>)resource, quantity, simulate, (NewProcurementTask<StorageTypeFluid>)request);
            else
            {
                FluidContainer input 
                    = this.getBufferManager().fluidInput();
                if(input != null)
                    return input.add((IResource<StorageTypeFluid>)resource, quantity, simulate, (NewProcurementTask<StorageTypeFluid>)request);
            }            
        break;
            
        case ITEM:
            if(this.hasItemStorage()) 
                return this.itemStorage().add((IResource<StorageTypeStack>)resource, quantity, simulate, (NewProcurementTask<StorageTypeStack>)request);
            else if(this.getBufferManager().itemInput() != null)
                return this.getBufferManager().itemInput().add((IResource<StorageTypeStack>)resource, quantity, simulate, (NewProcurementTask<StorageTypeStack>)request);
            break;
            
        case POWER:
            return this.energyManager().add((IResource<StorageTypePower>)resource, quantity, simulate, (NewProcurementTask<StorageTypePower>)request);

        case PRIVATE:
            assert false : "Unsupported private storage type reference";
        
        default:
            assert false : "Unhandled enum mapping";
        }
        return 0;
    }
    
    @Override
    public final long onConsume(IResource<?> resource, long quantity, boolean simulate, @Nullable NewProcurementTask<?> request)
    {
        assert resource.confirmServiceThread() 
            : "Transport logic running outside logistics service"; 
        return this.onConsumeImpl(resource, quantity, simulate, request);
    }

    @Override
    public boolean doesUpdateOffTick()
    {
        return this.energyManager.doesUpdateOffTick() 
            || this.bufferManager.doesUpdateOffTick();
    }

    @Override
    public void doOffTick()
    {
        IDevice.super.doOffTick();
        if(this.energyManager.doesUpdateOffTick()) this.energyManager.doOffTick();
        if(this.bufferManager.doesUpdateOffTick()) this.bufferManager.doOffTick();
    }
}
