package grondag.hard_science.gui.control;

import grondag.hard_science.gui.IGuiRenderContext;
import grondag.hard_science.gui.Layout;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;

@Environment(EnvType.CLIENT)
public abstract class GuiControl<T extends GuiControl<T>> extends DrawableHelper
{      
    public static final int BUTTON_COLOR_ACTIVE = 0x9AFFFFFF;
    public static final int BUTTON_COLOR_INACTIVE = 0x2AFFFFFF;
    public static final int BUTTON_COLOR_FOCUS = 0xFFBAF6FF;
    public static final int TEXT_COLOR_ACTIVE = 0xFF000000;
    public static final int TEXT_COLOR_INACTIVE = 0xFFEEEEEE;
    public static final int TEXT_COLOR_FOCUS = 0xFF000000;
    public static final int TEXT_COLOR_LABEL = 0xFFFFFFFF;
    
    public static final int CONTROL_INTERNAL_MARGIN = 5;
    public static final int CONTROL_EXTERNAL_MARGIN = 5;    
    public static final int CONTROL_BACKGROUND = 0x4AFFFFFF;
    protected static final int NO_SELECTION = -1;
    
    protected double top;
    protected double left;
    protected double height;
    protected double width;
    protected double bottom;
    protected double right;
    
    protected int horizontalWeight = 1;
    protected int verticalWeight = 1;
    
    protected Layout horizontalLayout = Layout.WEIGHTED;
    protected Layout verticalLayout = Layout.WEIGHTED;
    
    protected int backgroundColor = 0;
    
    protected boolean isDirty = false;
    
    protected boolean isVisible = true;
    
    /** cumulative scroll distance from all events */
    protected int scrollDistance;
    /** cumulative distance before scroll is recognized */
    protected int scrollIncrementDistance = 128;
    /** last scroll increment - used to compute a delta */
    protected int lastScrollIncrement = 0;
    
    /** 
     * If a control has consistent shape, is height / pixelWidth. 
     * Multiply pixelWidth by this number to get height. 
     * Divide height by this number to get pixelWidth.
     */
    protected double aspectRatio = 1.0;
    
    public GuiControl<T> resize(double left, double top, double width, double height)
    {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.isDirty = true;
        return this;
    }
    
    public void drawControl(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        this.refreshContentCoordinatesIfNeeded();
        if(this.isVisible) 
        {
            // set hover start, so that controls further down the stack can overwrite
            if(this.isMouseOver(mouseX, mouseY)) renderContext.setHoverControl(this);;
            this.drawContent(renderContext, mouseX, mouseY, partialTicks);
        }
    }
    
    public abstract void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks);
    

    protected abstract void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks);
    
    /** called after any coordinate-related input changes */
    protected abstract void handleCoordinateUpdate();
    
    protected abstract void handleMouseClick(MinecraftClient mc, int mouseX, int mouseY, int clickedMouseButton);
    
    protected abstract void handleMouseDrag(MinecraftClient mc, int mouseX, int mouseY, int clickedMouseButton);
    
    protected abstract void handleMouseScroll(int mouseX, int mouseY, int scrollDelta);
    
    public void mouseClick(MinecraftClient mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.isVisible)
        {
            this.refreshContentCoordinatesIfNeeded();
            if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return;
            this.handleMouseClick(mc, mouseX, mouseY, clickedMouseButton);
        }
    }
    
    public void mouseDrag(MinecraftClient mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(this.isVisible)
        {
            this.refreshContentCoordinatesIfNeeded();
            if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return;
            this.handleMouseDrag(mc, mouseX, mouseY, clickedMouseButton);
        }
    }
    
    public void mouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        if(this.isVisible)
        {
            this.refreshContentCoordinatesIfNeeded();
            if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom) return;
            this.scrollDistance += scrollDelta;
            this.handleMouseScroll(mouseX, mouseY, scrollDelta);
        }
    }
    
    protected int mouseIncrementDelta()
    {
        int newIncrement = this.scrollDistance / this.scrollIncrementDistance;
        int result = newIncrement - this.lastScrollIncrement;
        if(result != 0)
        {
            this.lastScrollIncrement = newIncrement;
        }
        return result;
    }
    
    protected void refreshContentCoordinatesIfNeeded()
    {
        if(this.isDirty)
        {
            this.bottom = this.top + this.height;
            this.right = this.left + this.width;
            
            this.handleCoordinateUpdate();
            this.isDirty = false;
        }
    }

    public double getTop()
    {
        return top;
    }

    @SuppressWarnings("unchecked")
    public T setTop(double top)
    {
        this.top = top;
        this.isDirty = true;
        return (T) this;
    }
    
    public double getBottom()
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.bottom;
    }

    public double getLeft()
    {
        return left;
    }
    
    @SuppressWarnings("unchecked")
    public T setLeft(double left)
    {
        this.left = left;
        this.isDirty = true;
        return (T) this;
    }

    public double getRight()
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.right;
    }
    
    public double getHeight()
    {
        return height;
    }
    
    /**
     * Use when control needs to be a square size.
     * Controls that require this generally don't enforce it.
     * Sometimes life isn't fair.
     */
    @SuppressWarnings("unchecked")
    public T setSquareSize(double size)
    {
        this.height = size;
        this.width = size;
        this.isDirty = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T setHeight(double height)
    {
        this.height = height;
        this.isDirty = true;
        return (T) this;
    }

    public double getWidth()
    {
        return width;
    }

    @SuppressWarnings("unchecked")
    public T setWidth(double width)
    {
        this.width = width;
        this.isDirty = true;
        return (T) this;
    }
   
    public int getBackgroundColor()
    {
        return backgroundColor;
    }

    @SuppressWarnings("unchecked")
    public T setBackgroundColor(int backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        return (T) this;
    }

    public double getAspectRatio()
    {
        return aspectRatio;
    }

    @SuppressWarnings("unchecked")
    public T setAspectRatio(double aspectRatio)
    {
        this.aspectRatio = aspectRatio;
        return (T) this;
    }

    public int getHorizontalWeight()
    {
        return horizontalWeight;
    }

    @SuppressWarnings("unchecked")
    public T setHorizontalWeight(int horizontalWeight)
    {
        this.horizontalWeight = horizontalWeight;
        return (T) this;
    }

    public int getVerticalWeight()
    {
        return verticalWeight;
    }

    @SuppressWarnings("unchecked")
    public T setVerticalWeight(int verticalWeight)
    {
        this.verticalWeight = verticalWeight;
        return (T) this;
    }

    public Layout getHorizontalLayout()
    {
        return horizontalLayout;
    }

    @SuppressWarnings("unchecked")
    public T setHorizontalLayout(Layout horizontalLayout)
    {
        this.horizontalLayout = horizontalLayout;
        return (T) this;
    }

    public Layout getVerticalLayout()
    {
        return verticalLayout;
    }

    @SuppressWarnings("unchecked")
    public T setVerticalLayout(Layout verticalLayout)
    {
        this.verticalLayout = verticalLayout;
        return (T) this;
    }

    protected boolean isMouseOver(int mouseX, int mouseY)
    {
        return !(mouseX < this.left || mouseX > this.right 
                || mouseY < this.top || mouseY > this.bottom);
    }
    
    public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean isVisible)
    {
        this.isVisible = isVisible;
    }
}