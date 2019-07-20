package grondag.hard_science.simulator.resource;


import javax.annotation.Nullable;

import com.google.common.base.Objects;

import grondag.exotic_matter.varia.ItemHelper;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Identifier for resources.
 * Instances with same inputs will have same hashCode and return true for equals().
 */
public class ItemResource extends AbstractResource<StorageType.StorageTypeStack>
{
    private Item item;
    private NBTTagCompound tag;
    private NBTTagCompound caps;
    private int meta;
    private int hash = -1;
    
    // lazy instantiate and cache
    private ItemStack stack;
    
    /** 
     * NEVER USE DIRECTLY EXCEPT FOR UNIT TESTING.
     */
    public ItemResource(Item item, int meta, NBTTagCompound tag, NBTTagCompound caps)
    {
        this.item = item;
        this.meta = meta;
        this.tag = tag;
        this.caps = caps;
        this.stack = null;
    }
    
    public static ItemResource fromItem(Item item)
    {
        return fromStack(item.getDefaultInstance());
    }
    
    public static ItemResource fromStack(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return (ItemResource) StorageType.ITEM.emptyResource;
        
        Item item = stack.getItem();
        int meta = stack.getMetadata();
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound caps = ItemHelper.itemCapsNBT(stack);
        
        return new ItemResource(item, meta, tag, caps);
    }
    
    /**
     * Returns a new stack containing one of this item.
     * Will always be a new instance/copy.     */
    public ItemStack sampleItemStack()
    {
        ItemStack stack = this.stack;
        if(stack == null)
        {
            stack = new ItemStack(this.item, 1, this.meta, this.caps);
            if(this.tag != null) stack.setTagCompound(this.tag);
            this.stack = stack;
        }
        return stack.copy();
    }
    
    public ItemStack stackWithQuantity(int quantity)
    {
        ItemStack result = new ItemStack(this.item, quantity, this.meta, this.caps);
        if(this.tag != null) result.setTagCompound(this.tag);
        return result;
    }
    
    public Item getItem()
    {
        return this.item;
    }

    public boolean hasTagCompound()
    {
        return this.tag != null;
    }
    
    @Nullable
    public NBTTagCompound getTagCompound()
    {
        return this.tag;
    }

    public int getMetadata()
    {
        return this.meta;
    }
   
    @Override
    public StorageTypeStack storageType()
    {
        return StorageType.ITEM;
    }

    public boolean isStackEqual(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return this == StorageType.ITEM.emptyResource;
        
        return stack.getItem() == this.item
            && stack.getMetadata() == this.meta
            && Objects.equal(stack.getTagCompound(), this.tag)
            && Objects.equal(ItemHelper.itemCapsNBT(stack), this.caps);
    }
    
    @Override
    public String displayName()
    {
        return this.stack == null 
                ? this.sampleItemStack().getDisplayName()
                : this.stack.getDisplayName();
    }

    @Override
    public ItemResourceWithQuantity withQuantity(long quantity)
    {
        return new ItemResourceWithQuantity(this, quantity);
    }
    
    @Override
    public String toString()
    {
        return this.displayName();
    }

    @Override
    public int hashCode()
    {
        int h = this.hash;
        if(h == -1)
        {
            h = this.item == null ? 0 : this.item.hashCode();
            
            if(this.tag != null)
            {
                h = h * 7919 + this.tag.hashCode();
            }
            
            if(this.caps != null)
            {
                h = h * 7919 + this.caps.hashCode();
            }
            
            if(this.meta != 0) 
            {
                h = h * 7919 + this.meta;
            }
            
            this.hash = h;
        }
        return h;
    }

  
    @Override
    public boolean isResourceEqual(@Nullable IResource<?> other)
    {
        if(other == this) return true;
        if(other == null) return false;
        if(other instanceof ItemResource)
        {
            ItemResource resource = (ItemResource)other;
            boolean result = resource.item == this.item
                && resource.meta == this.meta
                && Objects.equal(resource.tag, this.tag)
                && Objects.equal(resource.caps, this.caps);
            
            return result;
        }
        return false;
    }
}
