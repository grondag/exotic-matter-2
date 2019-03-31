package grondag.brocade.init;

import grondag.brocade.Brocade;
import grondag.brocade.block.SuperModelTileEntity;
import grondag.brocade.block.SuperTileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModTileEntities {
    public static void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(SuperTileEntity.class, Brocade.INSTANCE.prefixResource("super_tile"));
        GameRegistry.registerTileEntity(SuperModelTileEntity.class,
                Brocade.INSTANCE.prefixResource("super_model_tile"));
    }
}
