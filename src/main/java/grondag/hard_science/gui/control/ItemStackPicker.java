package grondag.hard_science.gui.control;

import java.util.List;

import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.varia.VerticalAlignment;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.simulator.resource.ItemResourceDelegate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemStackPicker extends TabBar<ItemResourceDelegate>
{
    protected final FontRenderer fontRenderer;
    
    protected final IClickHandler<ItemResourceDelegate> clickHandler;
    
    public ItemStackPicker(List<ItemResourceDelegate> items, FontRenderer fontRenderer,  
                IClickHandler<ItemResourceDelegate> clickHandler)
    {
        super(items);
        this.fontRenderer = fontRenderer;
        this.clickHandler = clickHandler;
        this.setItemsPerRow(9);
        this.setItemSpacing(2);
        this.setItemSelectionMargin(1);
        this.setSelectionEnabled(false);
        this.setCaptionHeight(fontRenderer.FONT_HEIGHT * 6 / 10 + 4);
    }
    
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        super.drawContent(renderContext, mouseX, mouseY, partialTicks);
    }
   

    private String getQuantityLabel(long qty)
    {
        if(qty < 1000)
        {
            return Long.toString(qty);
        }
        else if(qty < 10000)
        {
            return String.format("%.1fK", (float) qty / 1000);
        }
        else if(qty < 100000)
        {
            return Long.toString(qty / 1000) + "K";
        }
        else
        {
            return "many";
        }
    }

    @Override
    protected void drawItem(ItemResourceDelegate item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks, boolean isHighlighted)
    {
        int x = (int)left;
        int y = (int)top;
        
        ItemStack stack = item.displayStack();
        GlStateManager.enableLighting();
        itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, stack, x, y, "");
        
        // itemRender doesn't clean this up, messes up highlight boxes
        this.drawQuantity(item.getQuantity(), x, y);
     }
    
    protected void drawQuantity(long qty, int left, int top)
    {
        if(qty < 2) return;
        
        String qtyLabel = this.getQuantityLabel(qty);
        
        boolean wasUnicode = this.fontRenderer.getUnicodeFlag();
        if(wasUnicode) this.fontRenderer.setUnicodeFlag(false);
        
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        
        GlStateManager.translate(left, top + 18, 0);
        GlStateManager.scale(0.6, 0.6, 1);
        
        GuiUtil.drawAlignedStringNoShadow(this.fontRenderer, qtyLabel, 0, 0, 16 * 10 / 6 , this.getCaptionHeight() * 10 / 6, 
                0xFFFFFFFF, HorizontalAlignment.CENTER, VerticalAlignment.TOP);

        

//        GlStateManager.enableDepth();
//        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        
        if(wasUnicode) this.fontRenderer.setUnicodeFlag(true);
    }


    @Override
    protected void setupItemRendering()
    {
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);        
    }


    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        super.handleMouseClick(mc, mouseX, mouseY, clickedMouseButton);
        if(this.clickHandler != null && this.currentMouseLocation == MouseLocation.ITEM)
        {
            this.clickHandler.handleMouseClick(mc, clickedMouseButton, this.resourceForClickHandler());
        }
    }


    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        super.handleMouseDrag(mc, mouseX, mouseY, clickedMouseButton);
        if(this.clickHandler != null && this.currentMouseLocation == MouseLocation.ITEM)
        {
            this.clickHandler.handleMouseDrag(mc, clickedMouseButton, this.resourceForClickHandler());
        }
    }
    
    private ItemResourceDelegate resourceForClickHandler()
    {
        ItemResourceDelegate res = this.get(currentMouseIndex);
        return res == null ? ItemResourceDelegate.EMPTY : res;
    }

    @Override
    protected void drawToolTip(ItemResourceDelegate item, IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        renderContext.drawToolTip(item.displayStack(), mouseX, mouseY);
        
    }
}
