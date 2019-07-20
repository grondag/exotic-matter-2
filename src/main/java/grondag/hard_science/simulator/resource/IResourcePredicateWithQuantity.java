package grondag.hard_science.simulator.resource;

public interface IResourcePredicateWithQuantity<V extends StorageType<V>> extends IResourcePredicate<V>
{

    long getQuantity();

    /**
     * returns new value
     */
    public long changeQuantity(long delta);
    
    void setQuantity(long quantity);

//    NBTTagCompound toNBT();

}