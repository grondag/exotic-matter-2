package grondag.hard_science.machines.impl.logistics;

import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ChemicalBatteryTESR extends MachineTESR
{
    public static final ChemicalBatteryTESR INSTANCE = new ChemicalBatteryTESR();
    
    @Override
    protected void renderControlFace(Tessellator tessellator, BufferBuilder buffer, MachineTileEntity te, int alpha)
    {
        
    }
 

}
