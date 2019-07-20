package grondag.xm2.block.virtual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;

@Environment(EnvType.CLIENT)
public class VirtualBlockEntityRenderer extends BlockEntityRenderer<VirtualBlockEntityWithRenderer>
{

    public static final VirtualBlockEntityRenderer INSTANCE = new VirtualBlockEntityRenderer();
    
    
    @Override
    public void render(VirtualBlockEntityWithRenderer be, double double_1, double double_2, double double_3, float float_1, int int_1) {
        if(!be.isVirtual() || !((VirtualBlockEntity)be).isVisible()) return;
        
        //TODO: actually render - use 1.12 SuperBlockTESR as starting point
        super.render(be, double_1, double_2, double_3, float_1, int_1);
    }
}
