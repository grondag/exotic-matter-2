package grondag.hard_science.gui.control;

import java.util.ArrayList;
import java.util.Arrays;

import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.Layout;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Panel extends GuiControl<Panel>
{
    /** if false is horizontal */
    public final boolean isVertical;
    
    private int outerMarginWidth = 0;
    private int innerMarginWidth = 0;
    
    /**
     * If true, don't adjustIfEnabled layout of any child controls.
     * Useful for containers that have to conform to a specific pixel layout. 
     */
    private boolean isLayoutDisabled = false;
    
    protected ArrayList<GuiControl<?>> children = new ArrayList<GuiControl<?>>();
    
    public Panel(boolean isVertical)
    {
        super();
        this.isVertical = isVertical;
    }
    
    public Panel addAll(GuiControl<?>... controls)
    {
        this.children.addAll(Arrays.asList(controls));
        this.isDirty = true;
        return this;
    }

    public Panel add(GuiControl<?> control)
    {
        this.children.add(control);
        this.isDirty = true;
        return this;
    }
    
    public GuiControl<?> get(int i)
    {
        return this.children.get(i);
    }
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.getBackgroundColor() != 0)
        {
            GuiUtil.drawRect(this.left, this.top, this.right, this.bottom, this.getBackgroundColor());
        }
        
        for(GuiControl<?> control : this.children)
        {
            control.drawControl(renderContext, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.isLayoutDisabled || this.children == null || this.children.isEmpty()) return;
        
        int totalWeight = 0;
        int totalFixed = 0;
        
        double variableSpace = (this.isVertical ? this.height : this.width) - this.outerMarginWidth * 2;
        double fixedSpace = (this.isVertical ? this.width : this.height) - this.outerMarginWidth * 2;
        
        // on start pass, gather the size/weights for the expanding dimension
        for(GuiControl<?> control : this.children)
        {
            if(this.isVertical)
            {
                switch(control.getVerticalLayout())
                {
                case FIXED:
                    totalFixed += control.getHeight();
                    break;
                    
                case PROPORTIONAL:
                    totalFixed += fixedSpace * control.getAspectRatio();
                    break;
                    
                case WEIGHTED:
                default:
                    totalWeight += control.getVerticalWeight();
                    break;
                }
            }
            else
            {
                switch(control.getHorizontalLayout())
                {
                case FIXED:
                    totalFixed += control.getWidth();
                    break;
                    
                case PROPORTIONAL:
                    totalFixed += fixedSpace / control.getAspectRatio();
                    break;
                    
                case WEIGHTED:
                default:
                    totalWeight += control.getHorizontalWeight();
                    break;
                }
            }
        }
        
        // now scale the weights to the amount of space available
        double spaceFactor = totalWeight <= 0 ? 0 : (variableSpace - totalFixed - this.innerMarginWidth * (this.children.size() - 1)) / totalWeight;
        
        double contentLeft = this.left + this.outerMarginWidth;
        double contentTop = this.top + this.outerMarginWidth;
        double fixedSize = (this.isVertical ? this.width : this.height) - this.outerMarginWidth * 2;
        
        // on second pass rescale
        for(GuiControl<?> control : this.children)
        {
//            double variableSize;
            
            double controlHeight;
            double controlWidth;
            
            if(this.isVertical)
            {
                controlWidth = control.getHorizontalLayout() == Layout.FIXED
                        ? control.getWidth() : fixedSize;
                
                switch(control.getVerticalLayout())
                {
                case FIXED:
                    controlHeight = control.getHeight();
                    break;

                    case PROPORTIONAL:
                    controlHeight = controlWidth * control.getAspectRatio();
                    break;
                    
                case WEIGHTED:
                default:
                    controlHeight = spaceFactor * control.getVerticalWeight();
                    break;
                }
             
                if(control.getHorizontalLayout() == Layout.PROPORTIONAL)
                {
                    controlWidth = controlHeight / control.getAspectRatio();
                }
             
                control.resize(contentLeft, contentTop, controlWidth, controlHeight);
                contentTop += controlHeight + this.innerMarginWidth;
            }
            else
            {
                controlHeight = control.getVerticalLayout() == Layout.FIXED
                        ? control.getHeight() : fixedSize;
                
                switch(control.getHorizontalLayout())
                {
                case FIXED:
                    controlWidth = control.getWidth();
                    break;

                    case PROPORTIONAL:
                    controlWidth = controlHeight / control.getAspectRatio();
                    break;
                    
                case WEIGHTED:
                default:
                    controlWidth = spaceFactor * control.getHorizontalWeight();
                    break;
                }
             
                if(control.getVerticalLayout() == Layout.PROPORTIONAL)
                {
                    controlHeight = controlWidth * control.getAspectRatio();
                }
             
                control.resize(contentLeft, contentTop, controlWidth, controlHeight);
                contentLeft += controlWidth + this.innerMarginWidth;
            }
        }
    }

    @Override
    public void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        for(GuiControl<?> child : this.children)
        {
            child.mouseClick(mc, mouseX, mouseY, clickedMouseButton);
        }
    }

    @Override
    public void handleMouseDrag(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        for(GuiControl<?> child : this.children)
        {
            child.mouseDrag(mc, mouseX, mouseY, clickedMouseButton);
        }
    }
    
    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        for(GuiControl<?> child : this.children)
        {
            child.mouseScroll(mouseX, mouseY, scrollDelta);
        }       
    }
   
    /** the pixelWidth of the background from the edge of child controls */
    public int getOuterMarginWidth()
    {
        return outerMarginWidth;
    }

    /** sets the pixelWidth of the background from the edge of child controls */
    public Panel setOuterMarginWidth(int outerMarginWidth)
    {
        this.outerMarginWidth = outerMarginWidth;
        this.isDirty = true;
        return this;
    }

    /** the spacing between child controls */
    public int getInnerMarginWidth()
    {
        return innerMarginWidth;
    }

    /** sets the spacing between child controls */
    public Panel setInnerMarginWidth(int innerMarginWidth)
    {
        this.innerMarginWidth = innerMarginWidth;
        this.isDirty = true;
        return this;
    }

    /**
     * Set true to disable automatic layout of child controls.
     * Used for containers that require a fixed layout.
     * Means you must write code to set position and size of all children.
     */
    public boolean isLayoutDisabled()
    {
        return isLayoutDisabled;
    }

    public void setLayoutDisabled(boolean isLayoutDisabled)
    {
        this.isLayoutDisabled = isLayoutDisabled;
    }

    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }
}
