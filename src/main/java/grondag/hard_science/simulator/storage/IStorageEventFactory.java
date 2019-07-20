package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public interface IStorageEventFactory<T extends StorageType<T>>
{
    public void postBeforeStorageDisconnect(IResourceContainer<T> storage);
    
    public void postAfterStorageConnect(IResourceContainer<T> storage);
    
    public void postStoredUpdate(
            IResourceContainer<T> storage, 
            IResource<T> resource, 
            long delta,
            @Nullable NewProcurementTask<T> request);
    
    public void postAvailableUpdate(
            IResourceContainer<T> storage, 
            IResource<T> resource, 
            long delta,
            @Nullable NewProcurementTask<T> request);
    
    public void postCapacityChange(IResourceContainer<T> storage, long delta);
}
