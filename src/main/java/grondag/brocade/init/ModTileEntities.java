package grondag.brocade.init;

import grondag.exotic_matter.ExoticMatter;
import grondag.exotic_matter.block.SuperModelTileEntity;
import grondag.exotic_matter.block.SuperTileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTileEntities {
    public static void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(SuperTileEntity.class, ExoticMatter.INSTANCE.prefixResource("super_tile"));
        GameRegistry.registerTileEntity(SuperModelTileEntity.class,
                ExoticMatter.INSTANCE.prefixResource("super_model_tile"));
    }
}
