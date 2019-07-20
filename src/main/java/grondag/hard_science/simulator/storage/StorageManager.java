package grondag.hard_science.simulator.storage;

import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.magicwerk.brownies.collections.Key1List;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.simulator.domain.IDomain;
import grondag.exotic_matter.simulator.domain.IDomainCapability;
import grondag.exotic_matter.simulator.domain.IDomainMember;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.ITypedStorage;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.nbt.NBTTagCompound;

/**
* Responsibilities:
* <li>Tracking the location of all resources for a storage type within a domain.
* <li>Tracking all empty storage for a storage type within a domain.
* <li>Storing and retrieving items.
* <li>Answering inquiries about storage of a given type based on tracking.
* <li>Notifies listeners when total storage changes</li><p>
*
* Not responsible for optimizing storage.
*/
public class StorageManager<T extends StorageType<T>> 
    implements ITypedStorage<T>, IDomainMember, ISizedContainer, IStorageAccess<T>, IDomainCapability
{
    protected final HashSet<IResourceContainer<T>> stores = new HashSet<IResourceContainer<T>>();
    
    protected IDomain domain;
    protected final T storageType;
       
    /**
     * All unique resources contained in this domain
     */
    protected Key1List<StorageResourceManager<T>, IResource<T>> slots 
        = new Key1List.Builder<StorageResourceManager<T>, IResource<T>>().
              withPrimaryKey1Map(StorageResourceManager::resource).
              build();
    
    protected long capacity = 0;
    protected long used = 0;

    /**
     * Set to true whenever an existing slot becomes empty.  Set false when 
     * {@link #cleanupEmptySlots()} runs without any active listeners.
     */
    protected boolean hasEmptySlots = false;
    
    public StorageManager(T storageType)
    {
        super();
        this.storageType = storageType;
    }

    @Override
    public T storageType()
    {
        return this.storageType;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Implementation in storage manager exploits the StorageResourceManager objects
     * to provide better performance in large storage networks. 
     */
    @Override
    public ImmutableList<IResourceContainer<T>> getLocations(IResource<T> resource)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        
        if(summary == null || summary.quantityStored() == 0) return ImmutableList.of();
    
        return summary.getLocations(resource).stream()
                .sorted((StorageWithQuantity<T> a, StorageWithQuantity<T>b) 
                        -> Long.compare(a.quantity, b.quantity))
                .map(p -> p.storage)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public @Nullable IDomain getDomain()
    {
        return this.domain;
    }

    protected void addStore(IResourceContainer<T> store)
    {
        if(this.confirmServiceThread())
        {
            this.addStoreImpl(store);
        }
        else
        {
            this.storageType.service().executor.execute(() -> 
            {
                this.addStoreImpl(store);
            });
        }
    }
    
    private void addStoreImpl(IResourceContainer<T> store)
    {
        assert !stores.contains(store)
            : "Storage manager received request to add store it already has.";

        this.stores.add(store);
        this.capacity += store.getCapacity();
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyAdded(store, stack.resource(), stack.getQuantity(), null);
        }
    }
    
    protected void removeStore(IResourceContainer<T> store)
    {
        if(this.confirmServiceThread())
        {
            this.removeStoreImpl(store);
        }
        else
        {
            this.storageType.service().executor.execute(() -> 
            {
                this.removeStoreImpl(store);
            });
        }
    }
    
    private void removeStoreImpl(IResourceContainer<T> store)
    {
        assert stores.contains(store)
         : "Storage manager received request to remove store it doesn't have.";
        
        for(AbstractResourceWithQuantity<T> stack : store.find(this.storageType.MATCH_ANY))
        {
            this.notifyTaken(store, stack.resource(), stack.getQuantity(), null);
        }
        
        this.stores.remove(store);
        this.capacity -= store.getCapacity();
    }
    
    public long getQuantityStored(IResource<T> resource)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.quantityStored();
    }
    
    public long getQuantityAvailable(IResource<?> resource)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";
        return getEstimatedAvailable(resource);
    }
    
    /**
     * Like {@link #getQuantityAvailable(IResource)} but does not
     * have to be called on service thread.  Result is not reliable
     * for transport planning purposes.
     */
    public long getEstimatedAvailable(IResource<?> resource)
    {
        @SuppressWarnings("unchecked")
        StorageResourceManager<T> stored = this.slots.getByKey1((IResource<T>) resource);
        return stored == null ? 0 : stored.quantityAvailable();
    }
    
    public long getQuantityAllocated(IResource<T> resource)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.quantityAllocated();
    }
    
    /**
     * Like {@link #findQuantityAvailable(Predicate)} but can be called
     * from any thread.  Result may not be fully consistent and should
     * not be used for transport planning.
     */
    public List<AbstractResourceWithQuantity<T>> findEstimatedAvailable(Predicate<IResource<T>> predicate)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : this.slots)
        {
            if(predicate.test(entry.resource) && entry.quantityAvailable() > 0)
            {
                builder.add(entry.resource.withQuantity(entry.quantityAvailable()));
            }
        }
        
        return builder.build();
    }
    
    public List<AbstractResourceWithQuantity<T>> findQuantityAvailable(Predicate<IResource<T>> predicate)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";
        return this.findEstimatedAvailable(predicate);
    }
    
    public List<AbstractResourceWithQuantity<T>> findQuantityStored(Predicate<IResource<T>> predicate)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        ImmutableList.Builder<AbstractResourceWithQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : this.slots)
        {
            if(predicate.test(entry.resource) && entry.quantityStored() > 0)
            {
                builder.add(entry.resource.withQuantity(entry.quantityStored()));
            }
        }
        
        return builder.build();
    }
    
    /**
     * Returns a list of all stores that have resources matching the predicate with quantityIn included.
     */
    public ImmutableList<StorageWithResourceAndQuantity<T>> findStorageWithQuantity(Predicate<IResource<T>> predicate)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        ImmutableList.Builder<StorageWithResourceAndQuantity<T>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<T> entry : this.slots)
        {
            if(predicate.test(entry.resource))
            {
                entry.addStoragesWithQuantityToBuilder(builder);
            }
        }
        
        return builder.build();
    }
    
    /**
     * Returns a list of stores that have the given resource available
     * and reachable from the given device, in order of preference
     * for extract. (Generally stores that can be emptied are preferred.)<p>
     */
    public ImmutableList<IResourceContainer<T>> findSourcesFor(IResource<T> resource, IDevice reachableFrom)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        
        if(summary == null || summary.quantityStored() == 0) return ImmutableList.of();
    
        LogisticsService<T> service = this.storageType.service();
        
        return summary.getLocations(resource).stream()
                .filter(s -> {return service.areDevicesConnected(s.storage.device(), reachableFrom, resource);})
                .sorted((StorageWithQuantity<T> a, StorageWithQuantity<T>b) 
                        -> ComparisonChain.start()
                        // pull from public output buffers before
                        // pulling from public storage
                        .compare(b.storage.containerUsage().ordinal(), 
                                 a.storage.containerUsage().ordinal())
                        
                        // pull from smaller stores first,
                        // to favor having fewer containers
                        .compare(a.quantity, b.quantity)
                        .result())
                .map(p -> p.storage)
                .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Read-only snapshot of all stores in the domain.
     */
    @Override
    public ImmutableList<IResourceContainer<T>> stores()
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        return ImmutableList.copyOf(this.stores);
    }
    
    @Override
    public long getCapacity()
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        return capacity;
    }

    @Override
    public long usedCapacity()
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        return this.used;
    }
    
    /**
     * Called by storage instances, or by self when a storage is removed.
     * If request is non-null, then the amount taken reduces any allocation to that request.
     */
    public synchronized void notifyTaken(IResourceContainer<T> storage, IResource<T> resource, long taken, @Nullable NewProcurementTask<T> request)
    {
        assert this.confirmServiceThread() : "Storage manager update outside service thread.";
        
        if(taken == 0) return;
        
        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        
        assert summary != null
            : "Storage manager encounted missing resource on resource removal.";
        
        if(summary == null) return;
        
        summary.notifyTaken(storage, taken, request);
        
        // update overall qty
        this.used -= taken;
        
        assert used >= 0
                : "Storage manager encounted negative inventory level.";
        
        if(this.used < 0) used = 0;
        
        if(summary.isEmpty()) 
        {
            this.hasEmptySlots = true;
        }
    }

    /**
     * If request is non-null, then the amount added is immediately allocated to that request.
     */
    public synchronized void notifyAdded(IResourceContainer<T> storage, IResource<T> resource, long added, @Nullable NewProcurementTask<T> request)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        if(added == 0) return;

        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        if(summary == null)
        {
            summary = new StorageResourceManager<T>(resource, storage, added, request);
            this.slots.add(summary);
        }
        else
        {
            summary.notifyAdded(storage, added, request);
        }
        
        // update total quantityStored
        this.used += added;
        
        assert this.used <= this.capacity
            : "Storage manager usage greater than total storage capacity.";
    }
    
    public synchronized void notifyCapacityChanged(long delta)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        if(delta == 0) return;

        this.capacity += delta;
        
        assert this.capacity >= 0
            : "Storage manager encounted negative capacity level.";
    }
    
    /**
     * Sets allocation for the given request to the provided, non-negative value.
     * Returns allocation that was actually set.  Will not set negative allocations
     * and will not set allocation so that total allocated is more the total available.
     * Provides no notification to the request.
     */
    public long setAllocation(
            IResource<T> resource, 
            NewProcurementTask<T> request, 
            long requestedAllocation)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        assert requestedAllocation >= 0
                : "AbstractStorageManager.setAllocation got negative allocation request.";
        
        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.setAllocation(request, requestedAllocation);    
    }
    
    /**
     * Adds delta to the allocation of this resource for the given request.
     * Return value is the quantityIn removed or added, which could be different than
     * amount requested if not enough is available or would reduce allocation below 0.
     * Total quantityIn allocated can be different from return value if request already had an allocation.
     * Provides no notification to the request.
     */
    public long changeAllocation(
            IResource<T> resource,
            long quantityRequested, 
            NewProcurementTask<T> request)
    {       
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> stored = this.slots.getByKey1(resource);
        return stored == null ? 0 : stored.changeAllocation(request, quantityRequested);    
    }
    
    public synchronized void registerResourceListener(IResource<T> resource, IStorageResourceListener<T> listener)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        if(summary == null)
        {
            summary = new StorageResourceManager<T>(resource, null, 0, null);
            this.slots.add(summary);
        }
        summary.registerResourceListener(listener);
    }
    
    public synchronized void unregisterResourceListener(IResource<T> resource, IStorageResourceListener<T> listener)
    {
        assert this.confirmServiceThread() : "Storage manager access outside service thread.";

        StorageResourceManager<T> summary = this.slots.getByKey1(resource);
        if(summary != null)
        {
            summary.unregisterResourceListener(listener);
            if(summary.isEmpty()) this.hasEmptySlots = true;
        }
    }

    @Override
    public void deserializeNBT(@Nullable NBTTagCompound tag)
    {
        // NOOP - storage state is saved with the devices
        // manager state reconstructed a run time
    }

    @Override
    public void serializeNBT(NBTTagCompound tag)
    {
        // NOOP - storage state is saved with the devices
        // manager state reconstructed a run time
    }

    @Override
    public void setDirty()
    {
        // NOOP - storage state is saved with the devices
        // manager state reconstructed a run time        // NOOP - storage state is saved with the devices
    }

    @Override
    public String tagName()
    {
        assert false : "Possible attempt to serialize Storage Manager.  Storage manger does not support serialization.";
        return "";
    }

    @Override
    public void setDomain(IDomain domain)
    {
        this.domain = domain;
        domain.eventBus().register(this);
    }

    @Override
    public boolean isSerializationDisabled()
    {
        return true;
    }

}