package grondag.hard_science.simulator.storage;

import grondag.hard_science.simulator.resource.IResource;
import grondag.hard_science.simulator.resource.StorageType;

/**
 * Unique data class for returning store-related inquiry results.
 */
public class StorageWithResourceAndQuantity<T extends StorageType<T>> extends StorageWithQuantity<T>
{
    public final IResource<T> resource;
    
    public StorageWithResourceAndQuantity(IResourceContainer<T> storage, IResource<T> resource, long quantity)
    {
        super(storage, quantity);
        this.resource = resource;
    }
}