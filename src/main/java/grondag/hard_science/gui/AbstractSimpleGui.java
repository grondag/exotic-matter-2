package grondag.hard_science.gui;

import java.io.IOException;

import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.Panel;
import grondag.hard_science.gui.control.machine.MachineControlRenderer;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public abstract class AbstractSimpleGui <T extends MachineTileEntity> extends GuiScreen implements IGuiRenderContext
{

    protected Panel mainPanel;
    protected final T te;
    protected GuiControl<?> hoverControl;
    
    protected int guiLeft;
    protected int guiTop;
    protected int guiWidth;
    protected int guiHeight;
  
    public AbstractSimpleGui(T machineTileEntity) 
    {
        super();
        this.te = machineTileEntity;
    }
    
    @Override
    public void initGui()
    {
        super.initGui();

        guiHeight = height * 4 / 5;
        guiTop = (height - guiHeight) / 2;
        guiWidth = (int) (guiHeight * GuiUtil.GOLDEN_RATIO);
        guiLeft = (width - guiWidth) / 2;
       
        this.mainPanel = this.initGuiContextAndCreateMainPanel(this.te);
    }  
    
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
  
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) 
    {
        // ensure we get updates
        this.te.notifyServerPlayerWatching();
        
        this.hoverControl = null;
        super.drawDefaultBackground();
        
        drawRect(guiLeft, guiTop, guiLeft + guiWidth, guiTop + guiHeight, 0xFFCCCCCC);

        super.drawScreen(mouseX, mouseY, partialTicks);
        
        // Draw controls here because foreground layer is translated to frame of the GUI
        // and our controls are designed to render in frame of the screen.
        // And can't draw after super.drawScreen() because would potentially render on top of things.
        
        MachineControlRenderer.setupMachineRendering();
        this.mainPanel.drawControl(this, mouseX, mouseY, partialTicks);
        MachineControlRenderer.restoreGUIRendering();
        
        if(this.hoverControl != null)
        {
            hoverControl.drawToolTip(this, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int clickedMouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, clickedMouseButton);
        this.mainPanel.mouseClick(mc, mouseX, mouseY, clickedMouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)
    {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        this.mainPanel.mouseDrag(mc, mouseX, mouseY, clickedMouseButton);
    }
    
    @Override
    public Minecraft minecraft()
    {
        return this.mc;
    }

    @Override
    public RenderItem renderItem()
    {
        return this.itemRender;
    }

    @Override
    public GuiScreen screen()
    {
        return this;
    }

    @Override
    public FontRenderer fontRenderer()
    {
        return this.fontRenderer;
    }
    
    @Override
    public void setHoverControl(GuiControl<?> control)
    {
        this.hoverControl = control;
    }

    @Override
    public void drawToolTip(ItemStack hoverStack, int mouseX, int mouseY)
    {
        this.renderToolTip(hoverStack, mouseX, mouseY);
        
    }

    @Override
    public int mainPanelLeft()
    {
        return this.guiLeft + this.guiWidth - GuiControl.CONTROL_EXTERNAL_MARGIN - this.mainPanelSize();
    }

    @Override
    public int mainPanelTop()
    {
        return this.guiTop + GuiControl.CONTROL_EXTERNAL_MARGIN;
    }

    @Override
    public int mainPanelSize()
    {
        return this.guiHeight - GuiControl.CONTROL_EXTERNAL_MARGIN * 2;
    }

}
