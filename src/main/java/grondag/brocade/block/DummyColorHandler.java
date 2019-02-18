package grondag.brocade.block;

import javax.annotation.Nonnull;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DummyColorHandler implements IItemColor
{
    public static final DummyColorHandler INSTANCE = new DummyColorHandler();
    
    @Override
    public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex) {
        return 0xFFFFFFFF;
    }
}