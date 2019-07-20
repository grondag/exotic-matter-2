package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.hard_science.machines.support.ThroughputRegulator;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ResourceContainer<T extends StorageType<T>> implements IResourceContainer<T>
{
    protected final IDevice owner;
    private final ContainerUsage usage;
    protected long capacity = 2000;
    protected long used = 0;
    private final T storageType;
    
    /**
     * If usage is {@link ContainerUsage#PUBLIC_BUFFER_OUT} will
     * be a private output buffer that is co-serialized. 
     * Otherwise always null.
     * 
     * For output buffers, calls to {@link #addLocally(IResource, long, boolean, boolean, NewProcurementTask)}
     * can happen outside the service thread, and contents
     * will be automatically moved to the main buffer on that thread.<p>
     * 
     * Size of the internal buffer is set equal to capacity
     * of the public output buffer when contents are moved.
     */
    protected final ResourceContainer<T> localSlots;
    
    /**
     * If non-null, restricts what may be placed in this container.
     */
    protected Predicate<IResource<T>> predicate; 

    /**
     * Make this something other than the dummy regulator during
     * constructor if you want limits or accounting.
     */
    @SuppressWarnings("unchecked")
    protected ThroughputRegulator<T> regulator = ThroughputRegulator.DUMMY;
    
    /**
     * All unique resources contained in this container
     */
    protected final IResourceSlots<T> slots;
    
    /**
     * Max number of unique resources in this container.
     */
    protected final int maxSlotCount;
    
    public ResourceContainer(T storageType, IDevice owner, ContainerUsage usage, int maxSlots)
    {
        this.storageType = storageType;
        this.owner = owner;
        this.usage = usage;
        this.maxSlotCount = maxSlots;
        this.slots = maxSlots == 1 
                ? new ResourceSlotsSingle<T>()
                : new ResourceSlotsMulti<T>();
                
        this.localSlots = usage == ContainerUsage.PUBLIC_BUFFER_OUT 
                ? new ResourceContainer<T>(storageType, owner, ContainerUsage.PRIVATE_BUFFER_OUT, maxSlots)
                : null;
    }
    
    @Override
    public T storageType()
    {
        return this.storageType;
    }

    private static final String NBT_STORAGE_CAPACITY = NBTDictionary.claim("storeSize");
    private static final String NBT_STORAGE_CONTENTS = NBTDictionary.claim("storeContent");
    private static final String NBT_PRIVATE_BUFFER = NBTDictionary.claim("storBuff");

    @Override
    public void serializeNBT(NBTTagCompound nbt)
    {
        nbt.setLong(NBT_STORAGE_CAPACITY, this.capacity);
        if(!this.slots.isEmpty())
        {
            NBTTagList nbtContents = new NBTTagList();
            
            for(AbstractResourceWithQuantity<T> rwq : this.slots)
            {
                nbtContents.appendTag(rwq.toNBT());
            }
            nbt.setTag(NBT_STORAGE_CONTENTS, nbtContents);
        }
        
        // serialize sub-buffer for output buffers
        if(this.localSlots != null && !this.localSlots.isEmpty())
        {
            nbt.setTag(NBT_PRIVATE_BUFFER, this.localSlots.serializeNBT());
        }
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound nbt)
    {
        this.capacity = nbt.getLong(NBT_STORAGE_CAPACITY);
        this.slots.clear();
        this.used = 0;

        NBTTagList nbtContents = nbt.getTagList(NBT_STORAGE_CONTENTS, 10);
        if( nbtContents != null && !nbtContents.hasNoTags())
        {
            for (int i = 0; i < nbtContents.tagCount(); ++i)
            {
                NBTTagCompound subTag = nbtContents.getCompoundTagAt(i);
                if(subTag != null)
                {
                    AbstractResourceWithQuantity<T> rwq = this.storageType().fromNBTWithQty(subTag);
                    this.add(rwq, false, null);
                }
            }   
        }
        
        // load sub-buffer for output buffers
        if(localSlots != null && nbt.hasKey(NBT_PRIVATE_BUFFER))
        {
            this.localSlots.deserializeNBT(nbt.getCompoundTag(NBT_PRIVATE_BUFFER));
            // if we crashed or ended before local buffer 
            // got moved to public buffer, move the contents
            // now.  If there isn't enough space, they will simply
            // stay until another attempt is made to output something.
            if(this.localSlots.isEmpty()) 
                this.refreshLocalCapacity();
            else
                this.exportLocalBuffer();
        }
    }

    @Override
    public IDevice device()
    {
        return this.owner;
    }

    @Override
    public ContainerUsage containerUsage()
    {
        return this.usage;
    }
    
    @Override
    public ThroughputRegulator<T> getRegulator()
    {
        return this.regulator;
    }
    
    @Override
    public void setRegulator(ThroughputRegulator<T> regulator)
    {
        this.regulator = regulator;
    }
    
    @Override
    public void setContentPredicate(Predicate<IResource<T>> predicate)
    {
        if(this.predicate != predicate && predicate != null)
        {
            if(this.used == 0)
            {
                this.predicate = predicate;
                
                // For output buffers, give private buffer same restrictions
                if(this.localSlots != null)
                {
                    synchronized(this.localSlots)
                    {
                        this.localSlots.setContentPredicate(predicate);
                    }
                }
            }
            else assert false: "Attempt to configure non-empty bulkResource container.";
        }
    }
    
    @Override
    public Predicate<IResource<T>> getContentPredicate()
    {
        return this.predicate;
    }
    
    @Override
    public long getCapacity()
    {
        return capacity;
    }

    @Override
    public void setCapacity(long capacity)
    {
        if(this.containerUsage().isListed)
        {
            long oldCapacity = this.getCapacity();
            this.capacity = capacity;
            long delta = this.getCapacity() - oldCapacity;
            if(delta != 0 && this.isConnected() && this.getDomain() != null)
            {
                assert this.confirmServiceThread() : "storage operation outside service thread";
                this.storageType().eventFactory().postCapacityChange(this, delta);
            }
            // only does something for output buffers
            this.refreshLocalCapacity();
        }
        else
        {
            synchronized(this)
            {
                this.capacity = capacity;
            }
        }
    }
    
    /**
     * For output buffers, ensures private buffer has available 
     * capacity equal to available capacity of public container.
     */
    private void refreshLocalCapacity()
    {
        if(this.localSlots == null) return;
        
        synchronized(this.localSlots)
        {
            this.localSlots.setCapacity(this.localSlots.usedCapacity() + this.availableCapacity());
        }
        
    }
    
    @Override
    public long usedCapacity()
    {
        return this.used;
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> find(@Nonnull Predicate<IResource<T>> predicate)
    {
        if(this.slots.isEmpty()) return ImmutableList.of();
        
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(AbstractResourceWithQuantity<T> rwq : this.slots)
        {
            if(predicate.test(rwq.resource()))
            {
                builder.add(rwq.clone());
            }
        }
        
        return builder.build();
    }
    
    @Override
    public long getQuantityStored(@Nonnull IResource<T> resource)
    {
        return this.slots.getQuantity(resource);
    }

    @Override
    public long add(@Nonnull IResource<T> resource, long howMany, boolean simulate, boolean allowPartial, @Nullable NewProcurementTask<T> request)
    {
        long added;
        
        if(this.containerUsage().isListed)
        {
            added = this.addImpl(resource, howMany, simulate, allowPartial, request);
            if(added != 0 && this.isConnected() && this.getDomain() != null)
            {
                assert this.confirmServiceThread() : "storage operation outside service thread";
                if(!simulate) this.storageType().eventFactory().postStoredUpdate(this, resource, added, request);
            }
        }
        else
        {
            synchronized(this)
            {
                added = this.addImpl(resource, howMany, simulate, allowPartial, request);
            }
        }
        return added;
    }
    
    private long addImpl(
            IResource<T> resource, 
            long howMany, 
            boolean simulate, 
            boolean allowPartial, 
            @Nullable NewProcurementTask<T> request)
    {
        if(howMany < 1 || !this.isResourceAllowed(resource)) return 0;
        
        // if we are already at max slot count, prevent adding another slot
        if(    this.slots.size() >= this.maxSlotCount 
            && this.slots.getQuantity(resource) == 0) return 0;
        
        long added = this.regulator.limitInput(howMany, simulate, allowPartial);
        
        added = Math.min(howMany, this.availableCapacity());
        
        if(added < 1 || (!allowPartial && added != howMany)) return 0;
        
        if(!simulate)
        {
            this.slots.changeQuantity(resource, added);
            this.used += added;
            this.setDirty();
        }
        
        return added;
    }

    @Override
    public long takeUpTo(@Nonnull IResource<T> resource, long limit, boolean simulate, boolean allowPartial, @Nullable NewProcurementTask<T> request)
    {
        long taken;
        
        if(this.containerUsage().isListed)
        {
            taken = this.takeUpToImpl(resource, limit, simulate, allowPartial, request);
            if(this.isConnected() && this.getDomain() != null)
            {
                assert this.confirmServiceThread() : "storage operation outside service thread";
                if(!simulate) this.storageType().eventFactory().postStoredUpdate(this, resource, -taken, request);
            }
        }
        else
        {
            synchronized(this)
            {
                taken = this.takeUpToImpl(resource, limit, simulate, allowPartial, request);
            }
        }
        return taken;
    }
    
    private long takeUpToImpl(
            IResource<T> resource, 
            long limit, 
            boolean simulate, 
            boolean allowPartial, 
            NewProcurementTask<T> request)
    {
        if(limit < 1) return 0;
        
        long current = this.slots.getQuantity(resource);
        
        if(current <= 0) return 0;
        
        long taken = this.regulator.limitOutput(limit, simulate, allowPartial);
        
        taken = Math.min(limit, current);
        
        if(!allowPartial && taken != limit) return 0;
        
        if(taken > 0 && !simulate)
        {
            this.slots.changeQuantity(resource, -taken);
            this.used -= taken;
            this.setDirty();
        }
        
        return taken;   
    }
    
    @Override
    public List<AbstractResourceWithQuantity<T>> slots()
    {
        return this.slots.isEmpty()
                ? ImmutableList.of()
                : ImmutableList.copyOf(this.slots);
    }
    
    @Override
    public void onConnect()
    {
        IResourceContainer.super.onConnect();
        if(this.containerUsage().isListed)
        {
            assert this.getDomain() != null : "Null domain on storage connect";
            this.storageType().eventFactory().postAfterStorageConnect(this);
        }
    }
    
    @Override
    public void onDisconnect()
    {
        if(this.containerUsage().isListed)
        {
            assert this.getDomain() != null : "Null domain on storage disconnect";
            this.storageType().eventFactory().postBeforeStorageDisconnect(this);
        }
        IResourceContainer.super.onDisconnect();
    }
    
    /**
     * Can be called from device outside service thread to store content. 
     * Contents will be automatically be moved to public buffer on service queue.<p>
     * 
     * Contents in private buffer are serialized and if game crashes before
     * they move to public buffer, will be added to public buffer at time
     * of deserialization, if they fit within capacity.
     */
    public long addLocally(IResource<T> resource, long howMany, boolean simulate, boolean allowPartial, NewProcurementTask<T> request)
    {
        synchronized(this.localSlots)
        {
            final long result = localSlots.add(resource, howMany, simulate, allowPartial, request);
            
            this.storageType().service().executor.execute(() -> 
            {
                exportLocalBuffer();
            });
            
            return result;
        }
    }

    public long addLocally(IResource<T> resource, long howMany, boolean simulate)
    {
        return this.addLocally(resource, howMany, simulate, true, null);
    }
    
    /**
     * Tries to move contents of private local buffer to
     * public buffer. Called on service thread after every 
     * attempt to add to the local buffer, and called
     * directly after deserialization if there is something to move.
     */
    private void exportLocalBuffer()
    {
        synchronized(this.localSlots)
        {
            for(AbstractResourceWithQuantity<T> l : this.localSlots.find(this.storageType().MATCH_ANY))
            {
                long added = this.add(l, false, null);
                long taken = this.localSlots.takeUpTo(l.resource(), added, false);
                assert added == taken : "Item count mismatch during private buffer transfer.";
            }
            this.refreshLocalCapacity();
        }
    }
}