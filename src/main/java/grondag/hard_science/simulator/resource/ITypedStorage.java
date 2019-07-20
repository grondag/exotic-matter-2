package grondag.hard_science.simulator.resource;

public interface ITypedStorage<V extends StorageType<V>>
{
    public V storageType();
    
    /**
     * Confirms current thread is the logistics service thread
     * for this object's storage type.  Shorthand for {@link StorageType#service()#confirmServiceThread()}
     */
    public default boolean confirmServiceThread()
    {
        return this.storageType().service().confirmServiceThread();
    }
}