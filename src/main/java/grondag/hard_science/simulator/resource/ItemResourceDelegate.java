package grondag.hard_science.simulator.resource;

import java.io.IOException;
import java.util.Comparator;

import javax.annotation.Nullable;

import grondag.exotic_matter.serialization.IMessagePlus;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

/**
 * Used for client representation of item stacks. Test match
 * with server-side resources using the handle.
 * 
 * Includes a quantityIn directly instead of in a sub class
 * because uses cases for bulkResource delegates typically require it.
 */
public class ItemResourceDelegate implements IMessagePlus
{
    public static final ItemResourceDelegate EMPTY 
    = new ItemResourceDelegate(-1, ItemResource.fromStack(ItemStack.EMPTY), 0);    
    
    private long quantity = 0;
    private int handle;
    
    private ItemStack displayStack;
    
    /** always null on client */
    private ItemResource resource;
    
    public ItemResourceDelegate()
    {
        super();
    }
    
    public ItemResourceDelegate(int handle, ItemResource resource, long quantity)
    {
        this.handle = handle;
        this.quantity = quantity;
        this.resource = resource;
        this.displayStack = resource.sampleItemStack().copy();
    }
    
    @Override
    public String toString()
    {
        return String.format("%,d x ", this.getQuantity()) + this.displayStack().getDisplayName();
    }
    
    /**
     * Delegate equality test is by handle instead of by instance and
     * does include quantityIn.
     */
    @Override
    public boolean equals(@Nullable Object other)
    {
        return this == other
                || (other != null && other.getClass() == this.getClass() 
                && (   ((ItemResourceDelegate)other).handle() == this.handle()
                    && ((ItemResourceDelegate)other).quantity == this.quantity));
    }

    public long getQuantity()
    {
        return quantity;
    }
    
    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }

    public int handle()
    {
        return this.handle;
    }
    
    /**
     * Always null on client.
     */
    public ItemResource resource()
    {
        return this.resource;
    }
    
    @Override
    public int hashCode()
    {
        return this.handle();
    }
    
    @Override
    public void fromBytes(PacketBuffer pBuff)
    {
        this.quantity = pBuff.readLong();
        this.handle = pBuff.readInt();
        try
        {
            this.displayStack = pBuff.readItemStack();
        }
        catch (IOException e)
        {
            this.displayStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(PacketBuffer pBuff)
    {
        pBuff.writeLong(this.quantity);
        pBuff.writeInt(this.handle);
        pBuff.writeItemStack(this.displayStack);
    }

    public ItemStack displayStack()
    {
        return this.displayStack;
    }
    
    /////////////////////////////////////////
    // SORTING UTILITIES
    /////////////////////////////////////////
    
    public static final Comparator<ItemResourceDelegate> SORT_BY_NAME_ASC = new Comparator<ItemResourceDelegate>()
    {
        @Override
        public int compare(@Nullable ItemResourceDelegate o1, @Nullable ItemResourceDelegate o2)
        {
            if(o1 == null)
            {
                if(o2 == null) 
                {
                    return 0;
                }
                return 1;
            }
            else if(o2 == null) 
            {
                return -1;
            }
            
            String s1 = o1.displayStack.getDisplayName();
            String s2 = o2.displayStack.getDisplayName();
            return s1.compareTo(s2);
        }
    };
    
    public static final Comparator<ItemResourceDelegate> SORT_BY_NAME_DESC = new Comparator<ItemResourceDelegate>()
    {
        @Override
        public int compare(@Nullable ItemResourceDelegate o1, @Nullable ItemResourceDelegate o2)
        {
            return SORT_BY_NAME_ASC.compare(o2, o1);
        }
    };
    
    public static final Comparator<ItemResourceDelegate> SORT_BY_QTY_ASC = new Comparator<ItemResourceDelegate>()
    {
        @Override
        public int compare(@Nullable ItemResourceDelegate o1, @Nullable ItemResourceDelegate o2)
        {   
            if(o1 == null)
            {
                if(o2 == null) 
                {
                    return 0;
                }
                return  1;
            }
            else if(o2 == null) 
            {
                return -1;
            }
            int result = Long.compare(o1.quantity, o2.quantity);
            return result == 0 ? SORT_BY_NAME_ASC.compare(o1, o2) : result;
        }
    };
    
    public static final Comparator<ItemResourceDelegate> SORT_BY_QTY_DESC = new Comparator<ItemResourceDelegate>()
    {
        @Override
        public int compare(@Nullable ItemResourceDelegate o1, @Nullable ItemResourceDelegate o2)
        {
            return SORT_BY_QTY_ASC.compare(o2, o1);
        }
    };
    
    public static final int SORT_COUNT = 4;
    public static final String[] SORT_LABELS = {"A-Z", "Z-A", "1-2-3", "3-2-1" };
    @SuppressWarnings("rawtypes")
    public static final Comparator[] SORT = { SORT_BY_NAME_ASC, SORT_BY_NAME_DESC, SORT_BY_QTY_ASC, SORT_BY_QTY_DESC };

}
