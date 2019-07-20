package grondag.hard_science.simulator.storage;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import grondag.hard_science.HardScience;
import grondag.hard_science.simulator.device.IDevice;
import grondag.hard_science.simulator.resource.AbstractResourceWithQuantity;
import grondag.hard_science.simulator.resource.ItemResource;
import grondag.hard_science.simulator.resource.StorageType;
import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import grondag.hard_science.simulator.transport.management.LogisticsService;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * /!\ May have problems with IItemHandler if storages are modified concurrently
 * by simulation during world tick.  Tiles from other mods will expect item stack
 * to be unchanged since time they called getStackInSlot(). Will need to detect
 * when IStorage is being accessed this way, limit changes to server thread, etc.
 */
public class ItemContainer extends ResourceContainer<StorageTypeStack> implements IItemHandler
{
    public ItemContainer(IDevice owner, ContainerUsage usage, int maxSlots)
    {
        super(StorageType.ITEM, owner, usage, maxSlots);
    }
    
    /**
    * <i>If we have available capacity, then effectively one more slot available to add another items not already here.</i><br><br>
    * 
    * {@inheritDoc}
    */
   @Override
   public int getSlots()
   {
       try
       {
           return LogisticsService.ITEM_SERVICE.executor.submit( () ->
           {
               return this.availableCapacity() == 0 ? this.slots().size() : this.slots().size() + 1;

           }, true).get();
       }
       catch (Exception e)
       {
           HardScience.INSTANCE.error("Error in item handler", e);
           return 0;
       }
   }

   @Override
   public ItemStack getStackInSlot(int slot)
   {
       try
       {
           return LogisticsService.ITEM_SERVICE.executor.submit( () ->
           {
               List<AbstractResourceWithQuantity<StorageTypeStack>> slots = this.slots();
               
               if(slot < 0 || slot >= slots.size()) return ItemStack.EMPTY;
               AbstractResourceWithQuantity<StorageTypeStack> rwq = slots.get(slot);
               
               ItemStack result = ItemStack.EMPTY;
               if(rwq != null)
               {
                   result = ((ItemResource)rwq.resource()).sampleItemStack();
                   if (result != null) result.setCount((int) Math.min(Integer.MAX_VALUE, rwq.getQuantity()));
               }
               return result;
           }, true).get();
       }
       catch (Exception e)
       {
           HardScience.INSTANCE.error("Error in item handler", e);
           return ItemStack.EMPTY;
       }
   }

   /**
    * <i>Our storage doesn't care about stacking  but need to honor slot that is sent otherwise tend to get strangeness. </i><br><br>
    * 
    * {@inheritDoc}
    */
   @Override
   public ItemStack insertItem(int slot, @Nonnull @Nullable ItemStack stack, boolean simulate)
   {
       if(slot < 0) return stack;
       
       //TODO: should not need to run this on service thread for input buffers
       
       try
       {
           return LogisticsService.ITEM_SERVICE.executor.submit( () ->
           {
               List<AbstractResourceWithQuantity<StorageTypeStack>> slots = this.slots();

               if(slot < slots.size())
               {
                   // reject if trying to put in a mismatched slot - will force it to add to end slot
                   AbstractResourceWithQuantity<StorageTypeStack> rwq = slots.get(slot);
                   if(!((ItemResource)rwq.resource()).isStackEqual(stack)) return stack;
               }
               
               ItemResource stackResource = ItemResource.fromStack(stack);
               
               long added = this.add(stackResource, stack.getCount(), simulate, null);
               
               if(added == 0)
               {
                   return stack;
               }
               else if(added == stack.getCount())
               {
                   return ItemStack.EMPTY;
               }
               else
               {
                   ItemStack result = stack.copy();
                   result.shrink((int)added);
                   return result;
               }
           }, true).get();
       }
       catch (Exception e)
       {
           HardScience.INSTANCE.error("Error in item handler", e);
           return stack;
       }
   }


   @Override
   public ItemStack extractItem(int slot, int amount, boolean simulate)
   {
       //TODO: should not need to run this on service thread for private output buffers

       try
       {
           return LogisticsService.ITEM_SERVICE.executor.submit( () ->
           {
               List<AbstractResourceWithQuantity<StorageTypeStack>> slots = this.slots();

               if(slot < 0 || slot >= slots.size()) return ItemStack.EMPTY;
               
               AbstractResourceWithQuantity<StorageTypeStack> rwq = slots.get(slot);
               
               if(rwq == null || rwq.getQuantity() == 0) return ItemStack.EMPTY;
               
               ItemStack stack = ((ItemResource)rwq.resource()).sampleItemStack();
               
               if(stack == null) return ItemStack.EMPTY;
               
               int limit = Math.min(amount, ((ItemResource)rwq.resource()).sampleItemStack().getMaxStackSize());

               long taken = this.takeUpTo(rwq.resource(), limit, simulate, null);
               
               if(taken == 0) return ItemStack.EMPTY;
               
               stack.setCount((int)taken);
               
               return stack;
           }, true).get();
       }
       catch (Exception e)
       {
           HardScience.INSTANCE.error("Error in item handler", e);
           return ItemStack.EMPTY;
       }
   }

   @Override
   public int getSlotLimit(int slot)
   {
       try
       {
           return LogisticsService.ITEM_SERVICE.executor.submit( () ->
           {
               List<AbstractResourceWithQuantity<StorageTypeStack>> slots = this.slots();

               if(slot < 0 || slot >= slots.size()) return (int) Math.min(Integer.MAX_VALUE, this.availableCapacity());
               
               AbstractResourceWithQuantity<StorageTypeStack> rwq = slots.get(slot);
               
               if(rwq != null)
               {
                   return (int) Math.min(Integer.MAX_VALUE, rwq.getQuantity() + this.availableCapacity());
               }
               return 0;
           }, true).get();
       }
       catch (Exception e)
       {
           HardScience.INSTANCE.error("Error in item handler", e);
           return 0;
       }
   }
}
