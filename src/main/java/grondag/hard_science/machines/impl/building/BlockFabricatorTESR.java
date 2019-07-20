package grondag.hard_science.machines.impl.building;

import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.machines.base.MachineTESR;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockFabricatorTESR extends MachineTESR
{
    public static final BlockFabricatorTESR INSTANCE = new BlockFabricatorTESR();
    
    @Override
    protected void renderControlFace(Tessellator tessellator, BufferBuilder buffer, MachineTileEntity te, int alpha)
    {
//        BufferManager mbm = te.clientState().bufferManager;
        
//        for(RadialGaugeSpec spec : BlockFabricatorTileEntity.BASIC_BUILDER_GAUGE_SPECS)
//        {
//            MachineControlRenderer.renderGauge(tessellator,buffer, spec, te, mbm.getBuffer(spec.bufferIndex), alpha);
//        }
        
        MachineControlRenderer.renderFabricationProgress(RenderBounds.BOUNDS_PROGRESS, te, alpha);
        
//        MachineControlRenderer.renderCMY(tessellator, buffer, RenderBounds.BOUNDS_BOTTOM_0, 
//                mbm.getBuffer(BlockFabricatorMachine.BUFFER_INDEX_CYAN),
//                mbm.getBuffer(BlockFabricatorMachine.BUFFER_INDEX_MAGENTA), 
//                mbm.getBuffer(BlockFabricatorMachine.BUFFER_INDEX_YELLOW), 
//                alpha);
        
        
//        MachineControlRenderer.renderLinearProgress(tessellator, buffer, new RectRenderBounds(0.2, 0.3, 0.5, 0.08),  ModModels.TEX_LINEAR_POWER_LEVEL,8, 24, true, alpha << 24 | 0xFFFFFF);
        
//        int maxBacklog = te.getMaxBacklog();
//        String msg = Integer.toString(maxBacklog - te.getCurrentBacklog()) + " / " + Integer.toString(maxBacklog);
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RectRenderBounds(0.42, 0.5, 0.2, 0.07), msg, HorizontalAlignment.LEFT, alpha);
        
//        msg = Integer.toString(te.getJobRemainingTicks()) + " / " + Integer.toString(te.getJobDurationTicks());
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RectRenderBounds(0.7, 0.3, 0.2, 0.05), msg, HorizontalAlignment.CENTER, alpha);
        
//        MachineControlRenderer.renderMachineText(tessellator, buffer, new RectRenderBounds(0.7, 0.4, 0.2, 0.10), te.getMachineState().name(), HorizontalAlignment.CENTER, alpha);
    }
 

}
