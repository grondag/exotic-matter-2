package grondag.hard_science.gui.control;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemPreview extends GuiControl<ItemPreview>
{
    public ItemStack previewItem;
    
    private double contentLeft;
    private double contentTop;
    private double contentSize;
  
    @Override
    public void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.previewItem != null)
        {
            GuiUtil.renderItemAndEffectIntoGui(renderContext, this.previewItem, this.contentLeft, this.contentTop, this.contentSize);
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.contentSize = Math.min(this.width, this.height);
        this.contentLeft = this.left + (this.width - contentSize) / 2;
        this.contentTop = this.top + (this.height - contentSize) / 2;
    }

    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // nothing privileged
    }
    
    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // nothing privileged
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }
    
}
