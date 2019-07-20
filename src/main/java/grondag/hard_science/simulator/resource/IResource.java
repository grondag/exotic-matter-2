package grondag.hard_science.simulator.resource;

import javax.annotation.Nullable;

/** 
 * A bulkResource is something that can be produced and consumed.
 * Most resources can be stored. (Computation can't.)
 * Resources with a storage type can also have a location.
 * Time is not a bulkResource because it cannot be produced.<p>
 * 
 * Instances must be cached or statically declared so
 * that each unique bulkResource has exactly one instance.  This is
 * because IResource instance is used as a key in an IdentityHashMap
 * within the storage manager, and more generally because
 * equality operations on some resources (ItemStacks) are expensive.<p>
 * 
 * Client-side references should use a delegate class with same interface
 * and any client/server communication should use {@link #handle()}
 * to identify resources involved in a transaction.<p>
 * 
 * Implements Predicate interface as equality test for self.
 * 
 */
public interface IResource<V extends StorageType<V>> extends IResourcePredicate<V>, ITypedStorage<V>
{
    @Override
    public V storageType();
    public String displayName();
    public AbstractResourceWithQuantity<V> withQuantity(long quantity);
    public boolean isResourceEqual(@Nullable IResource<?> other);
    
    @Override
    public default boolean test(@Nullable IResource<V> t)
    {
        return t.equals(this);
    }
    
    @Override
    public default boolean isEqualityPredicate()
    {
        return true;
    }
}
