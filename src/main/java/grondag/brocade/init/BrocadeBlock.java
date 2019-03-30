package grondag.brocade.init;

import grondag.brocade.Brocade;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.block.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BrocadeBlock {
    public static final Block TEST_BLOCK = new Block(FabricBlockSettings
            .of(Material.STONE).strength(1, 1).build());
    
    public static final void init() {
        Brocade.INSTANCE.debug("Registering Brocade Blocks");
        register(TEST_BLOCK, "test");
    }
    
    private static void register(Block block, String name) {
        Identifier id = new Identifier("brocade", name);
        Registry.BLOCK.add(id, block);
        Registry.ITEM.add(id, new BlockItem(block, new Item.Settings()
                .stackSize(64)
                .itemGroup(ItemGroup.BUILDING_BLOCKS)));
    }
}
