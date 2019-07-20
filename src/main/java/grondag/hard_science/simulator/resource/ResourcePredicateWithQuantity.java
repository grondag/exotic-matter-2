package grondag.hard_science.simulator.resource;

import javax.annotation.Nullable;

public class ResourcePredicateWithQuantity<V extends StorageType<V>> implements IResourcePredicateWithQuantity<V>
{
    private final IResourcePredicate<V> predicate;
    
    private long quantity;
    
    @Override
    public long getQuantity()
    {
        return quantity;
    }

    @Override
    public long changeQuantity(long delta)
    {
        this.quantity += delta;
        return this.quantity;
    }
    
    @Override
    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }

    public ResourcePredicateWithQuantity(IResourcePredicate<V> predicate, long quantity)
    {
        this.predicate = predicate;
        this.quantity = quantity;
    }

    @Override
    public boolean test(@Nullable IResource<V> t)
    {
        return this.predicate.test(t);
    }
    
//    @SuppressWarnings("unchecked")
//    public ResourcePredicateWithQuantity(NBTTagCompound tag)
//    {
//        this.predicate = (IResourcePredicate<V>) IResourcePredicate.fromNBT(tag);
//        this.quantity = tag.getLong(ModNBTTag.RESOURCE_QUANTITY);
//    }
    
//    @Override
//    public NBTTagCompound toNBT()
//    {
//        NBTTagCompound result = IResourcePredicate.toNBT(this.predicate);
//        result.setLong(ModNBTTag.RESOURCE_QUANTITY, this.quantity);
//        return result;
//    }

    
}
