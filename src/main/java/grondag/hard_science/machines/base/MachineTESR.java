package grondag.hard_science.machines.base;

import javax.annotation.Nonnull;

import grondag.exotic_matter.block.SuperBlockTESR;
import grondag.exotic_matter.block.SuperTileEntity;
import grondag.exotic_matter.font.FontHolder;
import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.world.Rotation;
import grondag.hard_science.Configurator;
import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.gui.control.machine.RenderBounds;
import grondag.hard_science.init.ModModels;
import grondag.hard_science.machines.energy.ClientEnergyInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MachineTESR extends SuperBlockTESR
{
    public static MachineTESR INSTANCE = new MachineTESR();
    
    @Override
    public void render(@Nonnull SuperTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        /**
         * To see the control face, player has to be in front of it.
         */
        switch(te.getCachedModelState().getAxisRotation())
        {
        case ROTATE_NONE:
            if(z <= 0) 
                return;
            break;
            
        case ROTATE_90:
            if(x >= 0) 
                return;
            break;
            
        case ROTATE_180:
            if(z >= 0) 
                return;
            break;
            
        case ROTATE_270:
            if(x <= 0) 
                return;
            break;
            
        default:
            return;
        }

        MachineTileEntity mte = (MachineTileEntity)te;
        
        // fade in controls as player approaches - over a 4-block distance
        int displayAlpha = (int)(alpha * (MathHelper.clamp((1 - (Math.sqrt(mte.getLastDistanceSquared()) - Configurator.MACHINES.machineMaxRenderDistance) / 4) * 255, 0, 255)));

        if(displayAlpha <= 1) return;
       
        int white = (displayAlpha << 24) | 0xFFFFFF;

        // TE will send keepalive packets to server to get updated machine status for rendering
        //FIXME - don't call if not displaying anything than can change
        mte.notifyServerPlayerWatching();
        
        GlStateManager.pushMatrix();

        // the .5 is to move origin to block center so that we can rotate to correct facing
        GlStateManager.translate(x + .5f, y + .5f, z + .5f);
        GlStateManager.rotate(te.getCachedModelState().getAxisRotation().degreesInverse, 0, 1, 0);
        // move origin back to upper left corner to match GUI semantics
        GlStateManager.translate(0.5f, 0.5f, -.5f);
        GlStateManager.scale(-1.0f, -1.0f, 1.0f);

        MachineControlRenderer.setupMachineRendering();
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
  
        MachineControlRenderer.renderSpriteInBounds(tessellator, buffer, RenderBounds.BOUNDS_SYMBOL, mte.getSymbolSprite(), (displayAlpha << 24) | 0xFFFFFF, Rotation.ROTATE_NONE);
        MachineControlRenderer.renderMachineText(tessellator, buffer, FontHolder.FONT_RENDERER_LARGE, RenderBounds.BOUNDS_NAME, mte.clientState().machineName, HorizontalAlignment.CENTER, white);

        if(mte.clientState().hasOnOff)
        {
            MachineControlRenderer.renderBinarySprite(tessellator, buffer, 
                RenderBounds.BOUNDS_ON_OFF, ModModels.TEX_MACHINE_ON_OFF, 
                mte.clientState().isOn(), white);
        }
        
        if(mte.clientState().hasRedstoneControl)
        {
            MachineControlRenderer.renderRedstoneControl(mte, tessellator, buffer, RenderBounds.BOUNDS_REDSTONE, displayAlpha);
        }
        
        ClientEnergyInfo mpi = mte.clientState().powerSupplyInfo;
        if(mpi != null)
        {
            MachineControlRenderer.renderPower(tessellator, buffer, RenderBounds.BOUNDS_POWER_0, mpi, displayAlpha);
            MachineControlRenderer.renderBattery(tessellator, buffer, RenderBounds.BOUNDS_POWER_1, mpi, displayAlpha);
            if(mpi.hasGenerator())
            {
                MachineControlRenderer.renderGenerator(tessellator, buffer, RenderBounds.BOUNDS_POWER_2, mpi, displayAlpha);
            }
        }
        renderControlFace(tessellator, buffer, mte, displayAlpha);
        
        MachineControlRenderer.restoreWorldRendering();
        GlStateManager.popMatrix();

        
    }
    
    /**
     * Override if device needs to render anything special.
     * Default is NOOP.
     */
    protected void renderControlFace(Tessellator tessellator, BufferBuilder buffer, MachineTileEntity te, int alpha)
    {
        
    }
   
  
}
