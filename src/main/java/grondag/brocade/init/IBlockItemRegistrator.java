package grondag.brocade.init;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;

public interface IBlockItemRegistrator
{
    /**
     * Called at appropriate time by library during startup for all blocks that implement this interface.
     */
    public void registerItems(IForgeRegistry<Item> itemReg);
}
