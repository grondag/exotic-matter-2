package grondag.brocade.init;

import grondag.brocade.Brocade;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BrocadeBlocks {
    public static final Block TEST_BLOCK = new Block(FabricBlockSettings
            .of(Material.STONE).strength(1, 1).build());
    
    public static final void init() {
        Brocade.LOG.debug("Registering Brocade Blocks");
        register(TEST_BLOCK, "test");
    }
    
    private static void register(Block block, String name) {
        Identifier id = new Identifier("brocade", name);
        Registry.BLOCK.add(id, block);
        Registry.ITEM.add(id, new BlockItem(block, new Item.Settings()
                .maxCount(64)
                .group(ItemGroup.BUILDING_BLOCKS)));
    }
}
