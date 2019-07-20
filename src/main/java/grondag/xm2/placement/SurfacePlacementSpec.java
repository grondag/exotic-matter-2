package grondag.xm2.placement;

import java.util.function.BooleanSupplier;

import grondag.fermion.world.IBlockRegion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class SurfacePlacementSpec extends SingleStackPlacementSpec
{
    public SurfacePlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }

    @Override
    protected boolean doValidate()
    {
        // excavation doesn't make sense with this mode
        if(this.isExcavation) return false;

        return World.isValid(pPos.inPos) && player.world.getBlockState(pPos.inPos).getMaterial().isReplaceable();
    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawSelection(Tessellator tessellator, BufferBuilder bufferBuilder)
    {
        // TODO Auto-generated method stub

    }

    @Environment(EnvType.CLIENT)
    @Override
    protected void drawPlacement(Tessellator tessellator, BufferBuilder bufferBuilder, PlacementPreviewRenderMode previewMode)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public BooleanSupplier worldTask(ServerPlayerEntity player)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IBlockRegion region()
    {
        // TODO Auto-generated method stub
        return null;
    }
}