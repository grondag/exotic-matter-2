package grondag.hard_science.gui.control;

import grondag.exotic_matter.model.color.Translucency;
import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.varia.VerticalAlignment;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * TODO: Rewrite to handle alpha in each paint layer now.
 * Then remove translucency enum.
 */
@SideOnly(Side.CLIENT)
public class TranslucencyPicker extends GuiControl<TranslucencyPicker>
{
    private Translucency translucency = null;
    
    private double spacing;
    private double firstBoxLeft;

    @SuppressWarnings("deprecation")
    private final String LABEL = I18n.translateToLocal("label.translucency");
    
    public TranslucencyPicker()
    {
        this.setAspectRatio(1.0 / 7.0);
    }
    
    private int getMouseIndex(int mouseX, int mouseY)
    {
        if(mouseX < this.firstBoxLeft - this.spacing / 2 || mouseX > this.right || mouseY < this.top || mouseY > this.bottom)
            return NO_SELECTION;
        
        return MathHelper.clamp((int) ((mouseX - this.firstBoxLeft + this.spacing) / (this.height + this.spacing)), 0, Translucency.values().length - 1);
    }
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), LABEL, (float)this.left, (float)this.top, 
                (float)(this.width - this.firstBoxLeft), (float)this.height, TEXT_COLOR_LABEL, HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
        
        double x = this.firstBoxLeft;
        
        int mouseIndex = this.getMouseIndex(mouseX, mouseY);
        
        for(Translucency t : Translucency.values())
        {
            GuiUtil.drawRect(x + 2, this.top + 2, x + this.height - 2, this.bottom - 2, 0xFFFFFFFF);
            String label = Integer.toString((int)Math.round((100 * (1 - t.alpha)))) + "%";
            GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), label, x, this.top, this.height, this.height, 0xFF000000, 
                    HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
            int color = t.alphaARGB | 0x00FF0000;
            GuiUtil.drawRect(x + 2, this.top + 2, x + this.height - 2, this.bottom - 2, color);

            if(mouseIndex == t.ordinal())
            {
                GuiUtil.drawBoxWidthHeight(x, this.top, this.height, this.height, 1, GuiControl.BUTTON_COLOR_FOCUS);
            }
            else if(this.translucency == t)
            {
                GuiUtil.drawBoxWidthHeight(x, this.top, this.height, this.height, 1, GuiControl.BUTTON_COLOR_ACTIVE);
            }
            
            x += this.height + this.spacing;
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.spacing = this.height / 3;
        this.firstBoxLeft = this.right - this.height * 5;
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        int mouseIndex = this.getMouseIndex(mouseX, mouseY);
        if(mouseIndex != NO_SELECTION)
        {
            this.setTranslucency(Translucency.values()[mouseIndex]);
            GuiUtil.playPressedSound(mc);
        }
        
    }

    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        this.handleMouseClick(mc, mouseX, mouseY, clickedMouseButton);
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        // ignore
    }

    public Translucency getTranslucency()
    {
        return translucency;
    }

    public void setTranslucency(Translucency translucency)
    {
        this.translucency = translucency;
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }

}
