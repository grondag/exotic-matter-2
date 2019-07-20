package grondag.hard_science.simulator.storage;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;

import grondag.exotic_matter.simulator.domain.Domain;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.storage.ItemStorageEvent.AfterItemStorageConnect;
import grondag.hard_science.simulator.storage.ItemStorageEvent.BeforeItemStorageDisconnect;
import grondag.hard_science.simulator.storage.ItemStorageEvent.ItemCapacityChange;
import grondag.hard_science.simulator.storage.ItemStorageEvent.ItemStoredUpdate;
import net.minecraft.item.crafting.Ingredient;

/**
 * Main purpose is to hold type-specific event handlers.
 */
public class ItemStorageManager extends StorageManager<StorageTypeStack> 
{
    public ItemStorageManager(Domain domain)
    {
        super(StorageType.ITEM);
    }
    
    @Subscribe
    public void afterStorageConnect(AfterItemStorageConnect event)
    {
        this.addStore(event.storage);
    }
    
    @Subscribe
    public void beforeItemStorageDisconnect(BeforeItemStorageDisconnect event)
    {
        this.removeStore(event.storage);
    }
    
    @Subscribe
    public void onItemUpdate(ItemStoredUpdate event)
    {
        if(event.delta > 0)
        {
            this.notifyAdded(event.storage, event.resource, event.delta, event.request);
        }
        else
        {
            this.notifyTaken(event.storage, event.resource, -event.delta, event.request);
        }
    }
    
    @Subscribe
    public void onCapacityChange(ItemCapacityChange event)
    {
        this.notifyCapacityChanged(event.delta);
    }
    
    /**
     * Like {@link #findEstimatedAvailable(Predicate)} but accepts item
     * ingredient as the predicate.  Can be called from any thread.  
     * Result may not be fully consistent and should not be used 
     * for transport planning.
     */
    public List<AbstractResourceWithQuantity<StorageTypeStack>> findEstimatedAvailable(Ingredient ingredient)
    {
        ImmutableList.Builder<AbstractResourceWithQuantity<StorageTypeStack>> builder = ImmutableList.builder();
        
        for(StorageResourceManager<StorageTypeStack> entry : this.slots)
        {
            if(ingredient.test(((ItemResource)entry.resource).sampleItemStack()) && entry.quantityAvailable() > 0)
            {
                builder.add(entry.resource.withQuantity(entry.quantityAvailable()));
            }
        }
        
        return builder.build();
    }
}
