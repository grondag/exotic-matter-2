package grondag.hard_science.gui.control;

import grondag.exotic_matter.varia.HorizontalAlignment;
import grondag.exotic_matter.varia.VerticalAlignment;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.Layout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Slider extends GuiControl<Slider>
{
    public static final int TAB_MARGIN = 2;
    public static final int TAB_WIDTH = 8;
    public static final int ITEM_SPACING = 4;
    
    protected int size;
    protected String label;
    
    /** in range 0-1, how much of pixelWidth to allow for label */
    protected double labelWidthFactor = 0;
    
    /** actual pixelWidth of the label area */
    protected double labelWidth = 0;
    
    /** point to the right of label area */
    protected double labelRight;
    
    /** in range 0-1,, how much pixelWidth to allow for drawing selected option */
    protected double choiceWidthFactor = 0;
    
    /** actual pixelWidth of the selected option area */
    protected double choiceWidth = 0;
    
    /** size of each tab box, 0 if one continuous bar */
    protected double tabSize;
    
    /** pixelWidth of area between arrows */
    protected double scrollWidth;
    
    /** x point right of choice, left of arrows, tabs. Same as labelRight if no choice display. */
    protected double choiceRight;
    
    protected int selectedTabIndex;

    protected static enum MouseLocation
    {
        NONE,
        CHOICE,
        LEFT_ARROW,
        RIGHT_ARROW,
        TAB
    }
    
    private MouseLocation currentMouseLocation;
    private int currentMouseIndex;
    
    /**
     * Size refers to the number of choices in the slider.
     * Minecraft reference is needed to set height to font height.
     * labelWidth is in range 0-1 and allows for alignment of stacked controls.
     */
    public Slider(Minecraft mc, int size, String label, double labelWidthFactor)
    {
        this.size = size;
        this.label = label;
        this.labelWidthFactor = labelWidthFactor;
        this.setHeight(Math.max(TAB_WIDTH, mc.fontRenderer.FONT_HEIGHT + CONTROL_INTERNAL_MARGIN));
        this.setVerticalLayout(Layout.FIXED);
    }
    
    public void setSize(int size)
    {
        this.size = size;
    }
    
    protected void drawChoice(Minecraft mc, RenderItem itemRender, float partialTicks)
    {
        // not drawn in base implementation
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(size == 0) return;
        
        updateMouseLocation(mouseX, mouseY);
        
        // draw label if there is one
        if(this.label != null && this.labelWidth > 0)
        {
            GuiUtil.drawAlignedStringNoShadow(renderContext.fontRenderer(), this.label, this.left, this.top, 
                    this.labelWidth, this.height, TEXT_COLOR_LABEL, HorizontalAlignment.LEFT, VerticalAlignment.MIDDLE);
        }
        
        if(this.choiceWidthFactor > 0)
        {
            this.drawChoice(renderContext.minecraft(), renderContext.renderItem(), partialTicks);
        }
        
        // skip drawing tabs if there is only one
        if(this.size <= 1) return;
        
        // if tabs are too small, just do a continuous bar
        double tabStartX = this.choiceRight + TAB_WIDTH + ITEM_SPACING;
        double tabTop = this.top + (this.height - TAB_WIDTH) / 2;
        double tabBottom = tabTop + TAB_WIDTH;
        if(this.tabSize == 0.0)
        {            
            GuiUtil.drawRect(tabStartX, tabTop, tabStartX + this.scrollWidth, tabBottom, BUTTON_COLOR_INACTIVE);
     
            // box pixelWidth is same as tab height, so need to have it be half that extra to the right so that we keep our margins with the arrows
            double selectionCenterX = tabStartX + TAB_WIDTH / 2.0 + (this.scrollWidth - TAB_WIDTH) * (double) this.selectedTabIndex / (this.size - 1);
            
            GuiUtil.drawRect(selectionCenterX -  TAB_WIDTH / 2.0, tabTop, selectionCenterX -  TAB_WIDTH / 2.0, tabBottom, BUTTON_COLOR_ACTIVE);
        }
        else
        {
            int highlightIndex = this.currentMouseLocation == MouseLocation.TAB ? this.currentMouseIndex : -1;
            
            for(int i = 0; i < this.size; i++)
            {
                GuiUtil.drawRect(tabStartX, tabTop, tabStartX + this.tabSize, tabBottom,
                        i == highlightIndex ? BUTTON_COLOR_FOCUS : i == this.selectedTabIndex ? BUTTON_COLOR_ACTIVE : BUTTON_COLOR_INACTIVE);
                tabStartX += (this.tabSize + TAB_MARGIN);
            }
        }
        
        double arrowCenterY = tabTop + TAB_WIDTH / 2.0;

        GuiUtil.drawQuad(this.choiceRight, arrowCenterY, this.choiceRight + TAB_WIDTH, tabBottom,
                this.choiceRight + TAB_WIDTH, tabTop, this.choiceRight, arrowCenterY, 
                this.currentMouseLocation == MouseLocation.LEFT_ARROW ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_INACTIVE);

        GuiUtil.drawQuad(this.right, arrowCenterY, this.right - TAB_WIDTH, tabTop,
                this.right - TAB_WIDTH, tabBottom, this.right, arrowCenterY,
                this.currentMouseLocation == MouseLocation.RIGHT_ARROW ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_INACTIVE);
        
    }
    
    private void updateMouseLocation(int mouseX, int mouseY)
    {
        if(this.size == 0) return;
        
        if(mouseX < this.choiceRight || mouseX > this.right || mouseY < this.top || mouseY > this.top + TAB_WIDTH)
        {
            this.currentMouseLocation = MouseLocation.NONE;
        }
        else if(mouseX <= this.choiceRight + TAB_WIDTH + ITEM_SPACING / 2.0)
        {
            this.currentMouseLocation = MouseLocation.LEFT_ARROW;
        }
        else if(mouseX >= this.right - TAB_WIDTH - ITEM_SPACING / 2.0)
        {
            this.currentMouseLocation = MouseLocation.RIGHT_ARROW;
        }
        else
        {
            this.currentMouseLocation = MouseLocation.TAB;
            this.currentMouseIndex = MathHelper.clamp((int) ((mouseX - this.choiceRight - TAB_WIDTH - ITEM_SPACING / 2) / (this.scrollWidth) * this.size), 0, this.size - 1);
        }
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.size != 0)
        {
            this.labelWidth = this.width * this.labelWidthFactor;
            this.choiceWidth = this.width * this.choiceWidthFactor;
            this.labelRight = this.left + this.labelWidth;
            this.choiceRight = this.labelRight + this.choiceWidth + CONTROL_INTERNAL_MARGIN;
            this.scrollWidth = this.width - this.labelWidth - this.choiceWidth - CONTROL_INTERNAL_MARGIN - (TAB_WIDTH + ITEM_SPACING) * 2;
            this.tabSize = this.size <= 0 ? 0 : (this.scrollWidth - (TAB_MARGIN * (this.size - 1))) / size;
            if(tabSize < TAB_MARGIN * 2) tabSize = 0;
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.size == 0) return;
        
        this.updateMouseLocation(mouseX, mouseY);
        switch(this.currentMouseLocation)
        {
         case LEFT_ARROW:
            if(this.selectedTabIndex > 0) this.selectedTabIndex--;
            GuiUtil.playPressedSound(mc);
            break;

        case RIGHT_ARROW:
            if(this.selectedTabIndex < this.size - 1) this.selectedTabIndex++;
            GuiUtil.playPressedSound(mc);
            break;

        case TAB:
            this.selectedTabIndex = this.currentMouseIndex;
            break;
            
        case NONE:
        default:
            break;
        
        }
    }
    
    @Override
    protected void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.size == 0) return;
        
        this.updateMouseLocation(mouseX, mouseY);
        if(this.currentMouseLocation == MouseLocation.TAB) this.selectedTabIndex = this.currentMouseIndex;
    }
    
    @Override 
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        if(this.size == 0) return;
        
        this.selectedTabIndex = MathHelper.clamp(this.selectedTabIndex + this.mouseIncrementDelta(), 0, this.size - 1);
    }
   
    public int size()
    {
        return this.size;
    }
    
    public void setSelectedIndex(int index)
    {
        this.selectedTabIndex = this.size == 0
                ? NO_SELECTION 
                : MathHelper.clamp(index, 0, this.size - 1);
    }
    
    public int getSelectedIndex()
    {
        return this.size == 0 ? NO_SELECTION : this.selectedTabIndex;
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }
 
}
