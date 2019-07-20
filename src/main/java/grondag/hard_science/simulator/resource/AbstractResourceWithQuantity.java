package grondag.hard_science.simulator.resource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.NBTDictionary;
import grondag.hard_science.simulator.storage.IResourceContainer;
import grondag.hard_science.simulator.storage.StorageWithResourceAndQuantity;
import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractResourceWithQuantity<V extends StorageType<V>> 
implements ITypedStorage<V>, IResourcePredicateWithQuantity<V>
{
    protected static final String NBT_RESOURCE_QUANTITY = NBTDictionary.claim("resQty");
    
    private IResource<V> resource;
    protected long quantity;
  
    public AbstractResourceWithQuantity(IResource<V> resource, long quantity)
    {
        this.resource = resource;
        this.quantity = quantity;
    }
    
    // needed for IMessage support
    public AbstractResourceWithQuantity()
    {
        this.resource = this.storageType().fromNBT(null);
    }
    
    public AbstractResourceWithQuantity(NBTTagCompound tag)
    {
        this.quantity = tag.getLong(NBT_RESOURCE_QUANTITY);
        this.resource = this.storageType().fromNBT(tag.getCompoundTag(StorageType.NBT_RESOURCE_IDENTITY));
    }
    
    public final ItemResourceDelegate toDelegate(int handle)
    {
        return new ItemResourceDelegate(handle, (ItemResource) this.resource(), this.quantity);
    }
    
    public NBTTagCompound toNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag(StorageType.NBT_RESOURCE_IDENTITY, this.storageType().toNBT(this.resource));
        tag.setLong(NBT_RESOURCE_QUANTITY, this.quantity);
        return tag;
    }

    public IResource<V> resource()
    {
        return this.resource;
    }

    @Override
    public long getQuantity()
    {
        return this.quantity;
    }
    
    public void setQuanity(long quantity)
    {
        this.quantity = quantity;
    }
    
    /**
     * returns new value
     */
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
    
    public boolean isEmpty()
    {
        return this.quantity == 0;
    }

    /**
     * Takes up to limit from this stack and returns how many were actually taken.
     * Intended to be thread-safe.
     */
    public synchronized long takeUpTo(long limit)
    {
        if(limit < 1) return 0;
        
        long taken = Math.min(this.quantity, limit);
        this.quantity -= taken;
        return taken;
    }

    /**
     * Increases quantityStored and returns quantityStored actually added.
     * Intended to be thread-safe.
     */
    public synchronized long add(long howMany)
    {
        if(howMany < 1) return 0;
        
        this.quantity += howMany;
        
        return howMany;
    }

    public StorageWithResourceAndQuantity<V> withStorage(IResourceContainer<V> storage)
    {
        return new StorageWithResourceAndQuantity<V>(storage, this.resource, this.quantity);
    }

    @Override
    public String toString()
    {
        return String.format("%,d x ", this.getQuantity()) + this.resource.toString();
    }
    
    @Override
    public AbstractResourceWithQuantity<V> clone()
    {
        return this.resource.withQuantity(quantity);
    }

    @Override
    public boolean test(@Nullable IResource<V> t)
    {
        return this.resource.test(t);
    }

    @Override
    public V storageType()
    {
        return this.resource.storageType();
    }
}
