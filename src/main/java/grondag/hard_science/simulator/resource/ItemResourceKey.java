package grondag.hard_science.simulator.resource;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * For use in hash map.
 * Distinguishes between ItemResources
 * with different NBT 
 */
public class ItemResourceKey //implements IMessagePlus, IReadWriteNBT
{
//    private Item item;
//    private NBTTagCompound tag;
//    private NBTTagCompound caps;
//    private int meta;
    private int hash = -1;
    
    // lazy instantiate and cache
    private ItemStack stack;
    
    ItemResourceKey(ItemStack stack)
    {
        this.stack = stack;

        // needed so hashes match
        if(this.stack != null && !this.stack.isEmpty()) this.stack.setCount(1);
    }

    @Override
    public int hashCode()
    {
        if(this.hash == -1)
        {
            if(this.stack == null || this.stack.isEmpty())
            {
                this.hash = 0;
            }
            else
            {
                this.hash = this.stack.serializeNBT().hashCode();
            }
        }
        return this.hash;
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if(obj == null || !(obj instanceof ItemResourceKey)) return false;
     
        ItemResourceKey otherKey = (ItemResourceKey)obj;
        
        NBTTagCompound thisTag = this.stack.getTagCompound();
        NBTTagCompound otherTag = otherKey.stack.getTagCompound();
        
        if(thisTag == null)
        {
            if(otherTag != null) return false;
        }
        else
        {
            if(!thisTag.equals(otherTag)) return false;
        }
        
        if(!this.stack.areCapsCompatible(otherKey.stack)) return false;
        
        // do these last because should always match for items in same map
        return this.stack.getItem() == otherKey.stack.getItem()
                && this.stack.getMetadata() == otherKey.stack.getMetadata();
    }
    
//    @Override
//    public void serializeNBT(@Nonnull NBTTagCompound nbt)
//    {
//        nbt.setInteger(ModNBTTag.ITEM_RESOURCE_ITEM, Item.getIdFromItem(this.item));
//        nbt.setInteger(ModNBTTag.ITEM_RESOURCE_META, this.meta);
//        if(this.tag != null) nbt.setTag(ModNBTTag.ITEM_RESOURCE_STACK_TAG, this.tag);
//        if(this.caps != null) nbt.setTag(ModNBTTag.ITEM_RESOURCE_STACK_CAPS, this.caps);
//    }
//
//    @Override
//    public void deserializeNBT(@Nonnull NBTTagCompound nbt)
//    {
//        this.item = Item.getItemById(nbt.getInteger(ModNBTTag.ITEM_RESOURCE_ITEM));
//        this.meta = nbt.getInteger(ModNBTTag.ITEM_RESOURCE_META);
//        this.tag = nbt.hasKey(ModNBTTag.ITEM_RESOURCE_STACK_TAG) ? nbt.getCompoundTag(ModNBTTag.ITEM_RESOURCE_STACK_TAG) : null;
//        this.caps = nbt.hasKey(ModNBTTag.ITEM_RESOURCE_STACK_CAPS) ? nbt.getCompoundTag(ModNBTTag.ITEM_RESOURCE_STACK_CAPS) : null;
//        this.hash = -1;
//        this.stack = null;
//    }
    
//    @Override
//    public void fromBytes(PacketBuffer buf)
//    {
//        this.item = Item.getItemById(buf.readInt());
//        this.meta = buf.readInt();
//        try
//        {
//            this.tag = buf.readCompoundTag();
//        }
//        catch (IOException e)
//        {
//            Log.warn("Error reading storage packet");
//            e.printStackTrace();
//            this.tag = null;
//        }
//        try
//        {
//            this.caps = buf.readCompoundTag();
//        }
//        catch (IOException e)
//        {
//            Log.warn("Error reading storage packet");
//            e.printStackTrace();
//            this.caps = null;
//        }
//        this.hash = -1;
//        this.stack = null;
//    }
//
//    @Override
//    public void toBytes(PacketBuffer buf)
//    {
//        buf.writeInt(Item.getIdFromItem(this.item));
//        buf.writeInt(this.meta);
//        buf.writeCompoundTag(this.tag);
//        buf.writeCompoundTag(this.caps);
//    }
}