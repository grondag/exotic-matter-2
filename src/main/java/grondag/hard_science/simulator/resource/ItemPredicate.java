package grondag.hard_science.simulator.resource;

import javax.annotation.Nullable;

import grondag.hard_science.simulator.resource.StorageType.StorageTypeStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Unique item stack / resource predicate that ignore meta and NBT
 */
public class ItemPredicate implements IResourcePredicate<StorageTypeStack>
{
    private final Item item;
    
    public ItemPredicate(Item item)
    {
        this.item = item;
    }

    public ItemPredicate(ItemStack stack)
    {
        this.item = stack.getItem();
    }

    public ItemPredicate(ItemResource resource)
    {
        this.item = resource.getItem();
    }
    
    @Override
    public boolean test(@Nullable IResource<StorageTypeStack> t)
    {
        return ((ItemResource)t).getItem() == this.item;
    }

    @Override
    public boolean isEqualityPredicate()
    {
        return false;
    }

    @Override
    public Item item()
    {
        return this.item();
    }

    @Override
    public boolean ignoreMeta()
    {
        return true;
    }

    @Override
    public boolean ignoreNBT()
    {
        return true;
    }

    @Override
    public boolean ignoreCaps()
    {
        return true;
    }
    
    
}
