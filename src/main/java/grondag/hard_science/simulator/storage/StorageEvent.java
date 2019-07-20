package grondag.hard_science.simulator.storage;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.fobs.NewProcurementTask;
import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

public class StorageEvent
{
    protected static abstract class ResourceUpdate<T extends StorageType<T>>
    {
        public final IResourceContainer<T> storage;
        public final IResource<T> resource;
        public final long delta;
        public final NewProcurementTask<T> request;
        
        protected ResourceUpdate(
                IResourceContainer<T> storage, 
                IResource<T> resource, 
                long delta,
                @Nullable NewProcurementTask<T> request)
        {
            this.storage = storage;
            this.resource = resource;
            this.delta = delta;
            this.request = request;
        }
    }
    
    protected static abstract class StorageNotification<T extends StorageType<T>>
    {
        public final IResourceContainer<T> storage;
        
        protected StorageNotification(IResourceContainer<T> storage)
        {
            this.storage = storage;
        }
    }
    
    protected static abstract class CapacityChange<T extends StorageType<T>>
    {
        public final IResourceContainer<T> storage;
        public final long delta;
        
        protected CapacityChange(IResourceContainer<T> storage, long newCapacity)
        {
            this.storage = storage;
            this.delta = newCapacity;
        }
    }
}
