package grondag.xm2.placement;

import grondag.xm2.api.model.ModelState;
import grondag.xm2.block.XmStackHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public abstract class SingleStackPlacementSpec extends AbstractPlacementSpec
{
    /**
     * Stack that should be placed in the world.
     * Populated during {@link #doValidate()}
     * Default is AIR (for excavations) if not set.
     */
    protected ItemStack outputStack = Items.AIR.getStackForRender();
    
    protected SingleStackPlacementSpec(ItemStack placedStack, PlayerEntity player, PlacementPosition pPos)
    {
        super(placedStack, player, pPos);
    }
    
    @Override
    protected ModelState previewModelState()
    {
        return this.outputStack == null ? super.previewModelState() : XmStackHelper.getStackModelState(this.outputStack);
    }
}