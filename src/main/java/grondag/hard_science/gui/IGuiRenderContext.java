package grondag.hard_science.gui;

import java.util.ArrayList;
import java.util.List;

import grondag.hard_science.gui.control.GuiControl;
import grondag.hard_science.gui.control.Panel;
//import grondag.hard_science.gui.control.machine.AbstractMachineControl;
//import grondag.hard_science.gui.control.machine.MachineName;
//import grondag.hard_science.gui.control.machine.MachineOnOff;
//import grondag.hard_science.gui.control.machine.MachineRedstone;
//import grondag.hard_science.gui.control.machine.MachineSymbol;
//import grondag.hard_science.gui.control.machine.RenderBounds;
//import grondag.hard_science.gui.control.machine.RenderBounds.AbstractRectRenderBounds;
import grondag.hard_science.machines.base.MachineTileEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.ItemStack;

public interface IGuiRenderContext
{
    int CAPACITY_BAR_WIDTH = 4;

    public MinecraftClient minecraft();
    public ItemRenderer renderItem();
    public Screen screen();
    public TextRenderer fontRenderer();
    public void drawToolTip(ItemStack hoverStack, int mouseX, int mouseY);
    
    /** controls that are being hovered over while rendering should call this to
     * receive a callback after all controls have been rendered to draw a tooltip.
     */
    public abstract void setHoverControl(GuiControl<?> control);
    
    /**
     * Draws the given text as a tooltip.
     */
    public default void drawToolTip(String text, int mouseX, int mouseY)
    {
        this.screen().renderTooltip(text, mouseX, mouseY);
    }

    public default void drawToolTip(List<String> textLines, int mouseX, int mouseY)
    {
        this.screen().renderTooltip(textLines, mouseX, mouseY);
    }
    
    public default void drawLocalizedToolTip(String lang_key, int mouseX, int mouseY)
    {
        this.drawToolTip(I18n.translate(lang_key), mouseX, mouseY);
    }
    
    public default void drawLocalizedToolTip(int mouseX, int mouseY, String...lang_keys)
    {
        if(lang_keys.length == 0) return;
        
        ArrayList<String> list = new ArrayList<String>(lang_keys.length);
        
        for(String key : lang_keys)
        {
            list.add(I18n.translate(key));
        }
        this.drawToolTip(list, mouseX, mouseY);
    }
    
    public default void drawLocalizedToolTipBoolean(boolean bool, String true_key, String false_key, int mouseX, int mouseY)
    {
        this.drawToolTip(I18n.translate(bool ? true_key : false_key), mouseX, mouseY);
    }
    
    /** used by {@link #initGuiContext()} for layout.  For containers, set by container layout. For simple gui, is dynamic to screen size. */
    public int mainPanelLeft();
    
    /** used by {@link #initGuiContext()} for layout.  For containers, set by container layout. For simple gui, is dynamic to screen size. */
    public int mainPanelTop();
    
    /** used by {@link #initGuiContext()} for layout.  For containers, set by container layout. For simple gui, is dynamic to screen size. */
    public int mainPanelSize();
    
//    /**
//     * Call from initializer to set up main panel and other shared stuff
//     */
//    public default Panel initGuiContextAndCreateMainPanel(MachineTileEntity tileEntity)
//    {
//        Panel mainPanel = new Panel(true);
//        mainPanel.setLayoutDisabled(true);
//        mainPanel.setLeft(this.mainPanelLeft());
//        mainPanel.setTop(this.mainPanelTop());
//        mainPanel.setSquareSize(this.mainPanelSize());
//        mainPanel.setBackgroundColor(0xFF101010);
//        
//        
//        mainPanel.add(sizeControl(mainPanel, new MachineName(tileEntity, RenderBounds.BOUNDS_NAME), RenderBounds.BOUNDS_NAME));
//        mainPanel.add(sizeControl(mainPanel,  new MachineSymbol(tileEntity, RenderBounds.BOUNDS_SYMBOL), RenderBounds.BOUNDS_SYMBOL));
//
//        
//        if(tileEntity.clientState().hasOnOff) 
//        {
//            mainPanel.add(sizeControl(mainPanel, new MachineOnOff(tileEntity, RenderBounds.BOUNDS_ON_OFF), RenderBounds.BOUNDS_ON_OFF));
//        }
//
//        if(tileEntity.clientState().hasRedstoneControl)
//        {
//            mainPanel.add(sizeControl(mainPanel, new MachineRedstone(tileEntity, RenderBounds.BOUNDS_REDSTONE), RenderBounds.BOUNDS_REDSTONE));
//        }
//        
//        this.addControls(mainPanel, tileEntity);
//        
//        return mainPanel;
//    }
    
//    public default AbstractMachineControl<?, ?> sizeControl(Panel mainPanel, AbstractMachineControl<?, ?> control, AbstractRectRenderBounds bounds)
//    {
//        control.setLeft(mainPanel.getLeft() + mainPanel.getWidth() * ((AbstractRectRenderBounds)bounds).left());
//        control.setTop(mainPanel.getTop() + mainPanel.getHeight() * ((AbstractRectRenderBounds)bounds).top());
//        control.setWidth(mainPanel.getWidth() * ((AbstractRectRenderBounds)bounds).width());
//        control.setHeight(mainPanel.getHeight() * ((AbstractRectRenderBounds)bounds).height());
//        return control;
//    }
    
    public void addControls(Panel mainPanel, MachineTileEntity tileEntity);
}
