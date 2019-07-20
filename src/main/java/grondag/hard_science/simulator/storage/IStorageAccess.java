package grondag.hard_science.simulator.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;

import grondag.hard_science.simulator.demand.IProcurementRequest;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.transport.management.LogisticsService;

/**
 * Shared logic for classes that contain one or more
 * storages (exposed as a list) for searching those 
 * storages.
 */
public interface IStorageAccess<T extends StorageType<T>>
{
    /**
     * Snapshot of stores currently part of this instance.
     */
    public ImmutableList<IResourceContainer<T>> stores();
    
    /**
     * Returns a list of stores of type STORAGE (no output buffers)
     * that could accept the given bulkResource and are reachable from 
     * the given device, in order of preference for input.<p>
     * 
     * Orders results to encourage clustering of like storage.
     * Stores with the largest count of the bulkResource (but still with empty space)
     * come first, followed by stores with available space in descending order.
     */
    public default ImmutableList<IResourceContainer<T>> findSpaceFor(IResource<T> resource, IDevice reachableFrom)
    {
        LogisticsService<T> service = resource.storageType().service();
                
        return this.stores().stream()
            .filter(p -> p.containerUsage() == ContainerUsage.STORAGE 
                    && p.availableCapacityFor(resource) > 0
                    && service.areDevicesConnected(p.device(), reachableFrom, resource))
            .sorted((IResourceContainer<T> a, IResourceContainer<T>b) 
                    -> ComparisonChain.start()
                        .compare(b.getQuantityStored(resource), a.getQuantityStored(resource))
                        .compare(b.availableCapacityFor(resource), a.availableCapacityFor(resource))
                        .result()
                    )
            .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Returns all locations where the bulkResource is stored.
     * Note that the bulkResource may be allocated so the stored
     * quantities may not be available for use, but allocations
     * are not stored by location.<p>
     * 
     * Results are sorted with lowest counts first to encourage
     * emptying of small amounts so that items are clustered.
     */
    public default ImmutableList<IResourceContainer<T>> getLocations(IResource<T> resource)
    {
        return this.stores().stream()
                .filter(p -> p.getQuantityStored(resource) > 0)
                .sorted((IResourceContainer<T> a, IResourceContainer<T>b) 
                        -> Long.compare(a.getQuantityStored(resource), b.getQuantityStored(resource)))
                .collect(ImmutableList.toImmutableList());
    }
    
    /**
     * Aggregate version of {@link IStorage#add(IResource, long, boolean, NewProcurementTask)}
     * Must run on service thread for storage type.
     */
    public default long add(IResource<T> resource, final long howMany, boolean simulate, @Nullable NewProcurementTask<T> request, IDevice reachableFrom)
    {
        assert resource.confirmServiceThread() : "Storage action outside service thread.";
        
        if(howMany <= 0) return 0;
        ImmutableList<IResourceContainer<T>> stores = this.findSpaceFor(resource, reachableFrom);
        if(stores.isEmpty()) return 0;
        if(stores.size() == 1) return stores.get(0).add(resource, howMany, simulate, request);
        
        long demand = howMany;
        long result = 0;
        
        for(IResourceContainer<T> store : stores)
        {
            long added = store.add(resource, demand, simulate, request);
            if(added > 0)
            {
                demand -= added;
                result += added;
                if(demand == 0) break;
            }
        }
        return result;
    }
    
    /**
     * Aggregate version of {@link IStorage#takeUpTo(IResource, long, boolean, IProcurementRequest)}
     */
    public default long takeUpTo(IResource<T> resource, final long howMany, boolean simulate, @Nullable NewProcurementTask<T> request)
    {
        assert resource.confirmServiceThread() : "Storage action outside service thread.";
        
        if(howMany <= 0) return 0;
        ImmutableList<IResourceContainer<T>> stores = this.getLocations(resource);
        if(stores.isEmpty()) return 0;
        if(stores.size() == 1) return stores.get(0).takeUpTo(resource, howMany, simulate, request);
        
        long demand = howMany;
        long result = 0;
        
        for(IResourceContainer<T> store : stores)
        {
            long taken = store.takeUpTo(resource, demand, simulate, request);
            if(taken > 0)
            {
                demand -= taken;
                result += taken;
                if(demand == 0) break;
            }
        }
        return result;
    }
}
