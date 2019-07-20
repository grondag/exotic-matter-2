package grondag.hard_science.gui.control;

import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.varia.VerticalAlignment;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Toggle extends GuiControl<Toggle>
{

    protected boolean isOn = false;
    protected String label  = "unlabedl toggle";
    
    protected int targetAreaTop;
    protected int targetAreaBottom;
    protected int labelWidth;
    protected int labelHeight;
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        float boxRight = (float) (this.left + this.labelHeight);
        
        GuiUtil.drawBoxRightBottom(this.left, this.targetAreaTop, boxRight, this.targetAreaBottom, 1, this.isMouseOver(mouseX, mouseY) ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_ACTIVE);
        
        if(this.isOn)
        {
            GuiUtil.drawRect(this.left + 2, this.targetAreaTop + 2, boxRight - 2, this.targetAreaBottom - 2, BUTTON_COLOR_ACTIVE);
        }
        
        GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), this.label, boxRight + CONTROL_INTERNAL_MARGIN, this.targetAreaTop, 
                this.labelWidth, this.labelHeight, TEXT_COLOR_LABEL, HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        int fontHeight = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        this.targetAreaTop = (int) Math.max(this.top, this.top + (this.height - fontHeight) / 2);
        this.targetAreaBottom = (int) Math.min(this.bottom, this.targetAreaTop + fontHeight);
        this.labelHeight = fontHeight;
        this.labelWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.label);
    }

    @Override
    protected boolean isMouseOver(int mouseX, int mouseY)
    {
        return !(mouseX < this.left || mouseX > this.left + this.labelHeight + CONTROL_INTERNAL_MARGIN + this.labelWidth
                || mouseY < this.targetAreaTop || mouseY > this.targetAreaBottom);
    }
    
    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.isMouseOver(mouseX, mouseY))
        {
            this.isOn = !this.isOn;
            GuiUtil.playPressedSound(mc);
        }
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        // ignore
        
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }

    public boolean isOn()
    {
        return isOn;
    }

    public Toggle setOn(boolean isOn)
    {
        this.isOn = isOn;
        return this;
    }

    public String getLabel()
    {
        return label;
    }

    public Toggle setLabel(String label)
    {
        this.label = label;
        this.isDirty = true;
        return this;
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }

}
