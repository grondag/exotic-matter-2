package grondag.brocade.init;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Implement on items that should be registered with custom model location
 * (item's registry name) and library will handle this automatically on start
 * up.
 */
public interface IItemModelRegistrant {
    public void handleBake(ModelBakeEvent event);

    @SuppressWarnings("null")
    public default void handleRegister(IForgeRegistry<Item> itemReg) {
        Item item = (Item) this;
        ModelBakery.registerItemVariants(item, item.getRegistryName());
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
