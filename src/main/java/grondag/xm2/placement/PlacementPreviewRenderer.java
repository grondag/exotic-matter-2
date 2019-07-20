package grondag.xm2.placement;

import grondag.xm2.block.virtual.ExcavationRenderManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

public abstract class PlacementPreviewRenderer {
    private PlacementPreviewRenderer() {}
    
    public static void renderPreview(float tickDelta) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        ItemStack stack = PlacementItem.getHeldPlacementItem(player);
        
        if(player.isSneaking())
        if(stack != null)
        {
            PlacementItem placer = (PlacementItem) stack.getItem();
            PlacementResult result = PlacementHandler.predictPlacementResults(player, stack, placer);
            if(result.builder() != null) result.builder().renderPreview(tickDelta, player);
        }
        
        ExcavationRenderManager.render(tickDelta, player);
    }
}
