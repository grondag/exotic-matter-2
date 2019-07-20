package grondag.hard_science.simulator.storage;

import grondag.hard_science.simulator.resource.StorageType;

/**
 * Unique data class for returning store-related inquiry results.
 */
public class StorageWithQuantity<T extends StorageType<T>>
{
    public final IResourceContainer<T> storage;
    public final long quantity;
    
    public StorageWithQuantity(IResourceContainer<T> storage, long quantity)
    {
        this.storage = storage;
        this.quantity = quantity;
    }
}