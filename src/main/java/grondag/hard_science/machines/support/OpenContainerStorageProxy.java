package grondag.hard_science.machines.support;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

import grondag.exotic_matter.concurrency.ConcurrentForwardingList;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Maintains a view of an IStorage on client for currently open container.
 */
@SideOnly(Side.CLIENT)
public class OpenContainerStorageProxy
{    
    public static OpenContainerStorageProxy ITEM_PROXY = new OpenContainerStorageProxy();
    
    private OpenContainerStorageProxy() {};

    private Int2ObjectOpenHashMap<ItemResourceDelegate> MAP = new Int2ObjectOpenHashMap<ItemResourceDelegate>();
    
    public final ConcurrentForwardingList<ItemResourceDelegate> LIST = new ConcurrentForwardingList<ItemResourceDelegate>(Collections.emptyList());
    
    private boolean isDirty = false;
    
    private int sortIndex = 0;
    
    private long capacity;
    private long usedCapacity;
    
    /**
     * Incorporates changes and updates sort order.
     * Returns true if a refresh was performed.
     */

    public boolean refreshListIfNeeded()
    {
        if(!this.isDirty) return false;
        
        @SuppressWarnings("unchecked")
        Comparator<ItemResourceDelegate> sort = ItemResourceDelegate.SORT[this.sortIndex];
        
        LIST.setDelegate(ImmutableList.copyOf(MAP.values().stream().sorted(sort).collect(Collectors.toList())));
        
        this.isDirty = false;
        
        return true;
    }
    
    public void handleStorageRefresh(List<ItemResourceDelegate> update, long capacity, boolean isFullRefresh)
    {
        this.capacity = capacity;
        if(isFullRefresh)
        {
            this.handleFullRefresh(update);
        }
        else if(!update.isEmpty())
        {
            for(ItemResourceDelegate d : update)
            {
                this.handleDelegateUpdate(d);
            }
        }
        this.isDirty = true;
        
    }

    private void handleFullRefresh(List<ItemResourceDelegate> update)
    {
        this.MAP.clear();
        this.usedCapacity = 0;
        for(ItemResourceDelegate item : update )
        {
            this.MAP.put(item.handle(), item);
            this.usedCapacity += item.getQuantity();
        }
    }
    
    private void handleDelegateUpdate(ItemResourceDelegate update)
    {
        ItemResourceDelegate prior = this.MAP.get(update.handle());
        if(prior != null) this.usedCapacity -= prior.getQuantity();

        if(update.getQuantity() == 0)
        {
            this.MAP.remove(update.handle());
        }
        else
        {
            this.MAP.put(update.handle(), update);
            this.usedCapacity += update.getQuantity();
        }
    }

    public void handleStorageDisconnect()
    {
        this.MAP.clear();
        this.LIST.setDelegate(Collections.emptyList());
        this.isDirty = false;
    }

//    public boolean isClosed()
//    {
//        return false;
//    }

    public int getSortIndex()
    {
        return this.sortIndex;
    }

    public void setSortIndex(int sortIndex)
    {
        this.sortIndex = sortIndex;
        this.isDirty = true;
    }

    public long capacity()
    {
        return capacity;
    }

    public long usedCapacity()
    {
        return usedCapacity;
    }
    
    public int fillPercentage()
    {
        return this.capacity == 0 ? 0 : (int) (this.usedCapacity * 100 / this.capacity);
    }
}
