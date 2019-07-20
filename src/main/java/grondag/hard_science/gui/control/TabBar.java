package grondag.hard_science.gui.control;

import java.util.Collection;
import java.util.List;

import grondag.exotic_matter.varia.Useful;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TabBar<T> extends GuiControl<TabBar<T>>
{
    public static final int NO_SELECTION = -1;
    
    private int tabCount;
    private int itemsPerTab;
    private int columnsPerRow = 5;
    private int rowsPerTab;
    private int selectedItemIndex = NO_SELECTION;
    private int selectedTabIndex;
    
    private boolean allowSelection = true;
    
    public static int DEFAULT_TAB_MARGIN = 2;
    public static int DEFAULT_TAB_WIDTH = 8;
    public static int DEFAULT_ITEM_SPACING = 4;
    public static int DEFAULT_CAPTION_HEIGHT = 0;
    private int tabMargin = DEFAULT_TAB_MARGIN;
    private int tabWidth = DEFAULT_TAB_WIDTH;
    private int itemSpacing = DEFAULT_ITEM_SPACING;
    private int itemSelectionMargin = 2;
    private int captionHeight = DEFAULT_CAPTION_HEIGHT;
    
    
    private double actualItemSize;
    /** {@link #actualItemSize()} rounded down */
    private int actualItemPixels;
    private double tabSize;
    private double scrollHeight;
    
    private boolean focusOnSelection = false;
    
    private List<T> items;
    /** used to detect changes to list */
    private int lastListSize = -1;
    
    protected static enum MouseLocation
    {
        NONE,
        TOP_ARROW,
        BOTTOM_ARROW,
        TAB,
        ITEM
    }
    
    protected MouseLocation currentMouseLocation;
    protected int currentMouseIndex;
    
    public TabBar(List<T> items)
    {
        this.items = items;
    }
    
    public void setList(List<T> items)
    {
        this.items = items;
        this.isDirty = true;
    }

    /**
     * Use if the list is externally modified.
     */
    public void setDirty()
    {
        this.isDirty = true;
    }
    
    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(items == null) return;
        
        this.handleListSizeUpdateIfNeeded();
        
        updateMouseLocation(mouseX, mouseY);
        
        int column = 0;
        
        int itemHighlightIndex = this.currentMouseLocation == MouseLocation.ITEM ? this.currentMouseIndex : NO_SELECTION;
        
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        
        this.drawHighlightIfNeeded(itemHighlightIndex, true);
        
        if(this.selectedItemIndex != itemHighlightIndex) this.drawHighlightIfNeeded(this.selectedItemIndex, false);
        
        // skip drawing tabs if there is only one
        if(this.tabCount > 1)
        {
            // if tabs are too small, just do a continuous bar
            double tabStartY = this.top + this.tabWidth + this.itemSpacing;
            if(this.tabSize == 0.0)
            {
                
                GuiUtil.drawRect(this.right - this.tabWidth, tabStartY, this.right, tabStartY + this.scrollHeight, BUTTON_COLOR_INACTIVE);
         
                // box pixelWidth is same as tab height, so need to have it be half that extra to the right so that we keep our margins with the arrows
                double selectionCenterY = tabStartY + this.tabWidth / 2.0 + (this.scrollHeight - this.tabWidth) * (double) this.selectedTabIndex / (this.tabCount - 1);
                
                GuiUtil.drawRect(this.right - this.tabWidth, selectionCenterY -  this.tabWidth / 2.0, this.right, selectionCenterY +  this.tabWidth / 2.0, BUTTON_COLOR_ACTIVE);
                
            }
            else
            {
                int tabHighlightIndex = this.currentMouseLocation == MouseLocation.TAB ? this.currentMouseIndex : NO_SELECTION;
                
                for(int i = 0; i < this.tabCount; i++)
                {
                    GuiUtil.drawRect(this.right - this.tabWidth, tabStartY, this.right, tabStartY + this.tabSize,
                            i == tabHighlightIndex ? BUTTON_COLOR_FOCUS : i == this.selectedTabIndex ? BUTTON_COLOR_ACTIVE : BUTTON_COLOR_INACTIVE);
                    tabStartY += (this.tabSize + this.tabMargin);
                }
            }
            
            double arrowCenterX = this.right - this.tabWidth / 2.0;
    
            GuiUtil.drawQuad(arrowCenterX, this.top, this.right - this.tabWidth, this.top + this.tabWidth, this.right, this.top + this.tabWidth, arrowCenterX, this.top, 
                    this.currentMouseLocation == MouseLocation.TOP_ARROW ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_INACTIVE);
    
            GuiUtil.drawQuad(arrowCenterX, this.bottom, this.right, this.bottom - this.tabWidth, this.right - this.tabWidth, this.bottom - this.tabWidth, arrowCenterX, this.bottom, 
                    this.currentMouseLocation == MouseLocation.BOTTOM_ARROW ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_INACTIVE);
        }
        
        this.setupItemRendering();
        int start = this.getFirstDisplayedIndex();
        int end = this.getLastDisplayedIndex();
        double itemX = this.left;
        double itemY = this.top;
        
        for(int i = start; i < end; i++)
        {
            
            this.drawItem(this.get(i), renderContext.minecraft(), renderContext.renderItem(), itemX, itemY, partialTicks, i == itemHighlightIndex);
            if(++column == this.columnsPerRow)
            {
                column = 0;
                itemY += (this.actualItemSize + this.itemSpacing + this.captionHeight);
                itemX = this.left;
            }
            else
            {
                itemX += (this.actualItemSize + this.itemSpacing);
            }
        }
        
    }
    
    @Override
    public final void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        if(this.items == null) return;
        
        this.handleListSizeUpdateIfNeeded();
        
        this.updateMouseLocation(mouseX, mouseY);
        
        if(this.currentMouseLocation == MouseLocation.ITEM)
        {
            T item = this.get(this.currentMouseIndex);
            if(item != null)
            {
                this.drawToolTip(item, renderContext, mouseX, mouseY, partialTicks);
            }
        }
    }
    
    protected abstract void drawToolTip(T item, IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks);
    
    /**
     * 
     * @param index
     */
    private void drawHighlightIfNeeded(int index, boolean isHighlight)
    {
        if(index == NO_SELECTION) return;
        
        int start = this.getFirstDisplayedIndex();
        int end = this.getLastDisplayedIndex();
        
        if(index < start || index >= end) return;
        
        int idx = index - start;
        int x = (int) (this.left + (idx % this.columnsPerRow) * (this.actualItemSize + this.itemSpacing));
        int y = (int) (this.top + (idx / this.columnsPerRow) * (this.actualItemSize + this.itemSpacing + this.captionHeight));
        
        this.drawHighlight(index, x, y, isHighlight);
    }
    
    /**
     * Coordinates given are top left of item area - does not account for margin offset.
     * If isHighlight = true, mouse is over item. If false, item is selected.
     */
    protected void drawHighlight(int index, double x, double y, boolean isHighlight)
    {
        GuiUtil.drawBoxRightBottom(
                x - itemSelectionMargin, 
                y - itemSelectionMargin, 
                x + this.actualItemSize + itemSelectionMargin, 
                y + this.actualItemSize + itemSelectionMargin, 1, 
                isHighlight ? BUTTON_COLOR_FOCUS : BUTTON_COLOR_ACTIVE);
    }
     
    /** set (non-matrix) GL state needed for proper rending of this tab's items */
    protected abstract void setupItemRendering();
    
    protected abstract void drawItem(T item, Minecraft mc, RenderItem itemRender, double left, double top, float partialTicks, boolean isHighlighted);
    
    private void updateMouseLocation(int mouseX, int mouseY)
    {
        if(items == null) return;
        
        if(mouseX < this.left || mouseX > this.right || mouseY < this.top || mouseY > this.bottom)
        {
            this.currentMouseLocation = MouseLocation.NONE;
        }
        else if(mouseX >= this.right - this.tabWidth)
        {
            if(mouseY <= this.top + this.tabWidth + this.itemSpacing / 2.0)
            {
                this.currentMouseLocation = MouseLocation.TOP_ARROW;
            }
            else if(mouseY >= this.bottom - this.tabWidth - this.itemSpacing / 2.0)
            {
                this.currentMouseLocation = MouseLocation.BOTTOM_ARROW;
            }
            else
            {
                this.currentMouseLocation = MouseLocation.TAB;
                this.currentMouseIndex = MathHelper.clamp((int) ((mouseY - this.top - this.tabWidth - this.itemSpacing / 2) / (this.scrollHeight) * this.tabCount), 0, this.tabCount - 1);
//                this.currentMouseIndex = (int) ((mouseX - this.left - this.tabWidth - this.actualItemMargin / 2) / (this.tabWidth + this.tabMargin));
            }
        }
        else
        {
            this.currentMouseLocation = MouseLocation.ITEM;
            
            int newIndex = this.getFirstDisplayedIndex() + (int)((mouseY - this.top - this.itemSpacing / 2) / (this.actualItemSize + this.itemSpacing + this.captionHeight)) * this.columnsPerRow
                    + Math.min((int)((mouseX - this.left - this.itemSpacing / 2) / (this.actualItemSize + this.itemSpacing)), this.columnsPerRow - 1);
            
            this.currentMouseIndex = (newIndex < this.items.size()) ? newIndex : NO_SELECTION;
        }
    }
    
    @Override
    protected void handleCoordinateUpdate()
    {
        if(this.items != null)
        {
            double horizontalSpaceRemaining = this.width - this.tabWidth;
            this.actualItemSize = horizontalSpaceRemaining / this.columnsPerRow - this.itemSpacing;
            this.actualItemPixels = (int)actualItemSize;
            this.rowsPerTab = (int) ((this.height + this.itemSpacing) / (actualItemSize + this.itemSpacing + this.captionHeight));
            this.scrollHeight = this.height - (this.tabWidth + this.itemSpacing) * 2;
            this.itemsPerTab = columnsPerRow * rowsPerTab;
            this.handleListSizeUpdateIfNeeded();
        }
        
        if(this.focusOnSelection && this.selectedItemIndex != NO_SELECTION)
        {
            if(this.itemsPerTab > 0) this.selectedTabIndex = this.selectedItemIndex / this.itemsPerTab;
            this.focusOnSelection = false;
        }
    }
    
    /**
     * Does NOT check for null list.  Called expected to do so.
     */
    protected void handleListSizeUpdateIfNeeded()
    {
        if(items.size() != this.lastListSize)
        {
            this.tabCount = this.itemsPerTab > 0 ? (this.items.size() + this.itemsPerTab - 1) / this.itemsPerTab : 0;
            this.tabSize = tabCount <= 0 ? 0 : (this.scrollHeight - (this.tabMargin * (this.tabCount - 1))) / tabCount;
            if(tabSize < this.tabMargin * 2) tabSize = 0;
            this.lastListSize = items.size();
        }
    }

    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        if(items == null) return;
        
        this.updateMouseLocation(mouseX, mouseY);
        switch(this.currentMouseLocation)
        {
        case ITEM:
            if(this.currentMouseIndex >= 0) this.setSelectedIndex(this.currentMouseIndex);
            break;

        case TOP_ARROW:
            if(this.selectedTabIndex > 0) this.selectedTabIndex--;
            GuiUtil.playPressedSound(mc);
            break;

        case BOTTOM_ARROW:
            if(this.selectedTabIndex < this.tabCount - 1) this.selectedTabIndex++;
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
        if(items == null) return;
        
        this.updateMouseLocation(mouseX, mouseY);
        switch(this.currentMouseLocation)
        {
        case ITEM:
            if(this.currentMouseIndex >= 0) this.setSelectedIndex(this.currentMouseIndex);
            break;

        case TOP_ARROW:
            break;

        case BOTTOM_ARROW:
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
    protected void handleMouseScroll(int mouseX, int mouseY, int scrollDelta)
    {
        if(items == null) return;
        
        this.selectedTabIndex = MathHelper.clamp(this.selectedTabIndex + this.mouseIncrementDelta(), 0, this.tabCount - 1);
    }
    
    public void add(T item)
    {
        if(items == null) return;
        
        this.items.add(item);
        this.isDirty = true;
    }

    public void addAll(Collection<T> items)
    {
        if(items == null) return;
        
        this.items.addAll(items);
        this.isDirty = true;
    }
    
    public void addAll(T[] itemsIn)
    {
        if(items == null) return;
        
        for(T item : itemsIn)
        {
            this.items.add(item);
        }
        this.isDirty = true;
    }
    
    public T get(int index)
    {
        if(items == null || index == NO_SELECTION) return null;
        
        return this.items.get(index);
    }
    
    public T getSelected()
    {
        if(items == null || this.selectedItemIndex == NO_SELECTION) return null;
        
        return this.get(this.getSelectedIndex());
    }
    
    public List<T> getDisplayed()
    {
        if(items == null) return null;
        
        return this.items.subList(this.getFirstDisplayedIndex(), this.getLastDisplayedIndex());
    }

    public void clear()
    {
        if(items == null) return;
        this.items.clear();
        this.isDirty = true;
    }
    
    public void setItemsPerRow(int itemsPerRow)
    {
        this.columnsPerRow = Math.max(1, itemsPerRow);
        this.isDirty = true;
    }
    
    public int getItemsPerTab()
    {
        if(items == null) return 0;
        this.refreshContentCoordinatesIfNeeded();
        return this.itemsPerTab;
    }
   
    public int size()
    {
        if(items == null) return 0;
        return this.items.size();
    }
    
    public void setSelectedIndex(int index)
    {
        if(items == null || !this.allowSelection) return;
        this.selectedItemIndex = MathHelper.clamp(index, NO_SELECTION, this.items.size() - 1);
        this.showSelected();
    }
    
    public void setSelected(T selectedItem)
    {
        if(items == null || selectedItem == null || !this.allowSelection)
        {
            this.setSelectedIndex(NO_SELECTION);
        }
        else
        {
            int i = this.items.indexOf(selectedItem);
            if(i >= NO_SELECTION) this.setSelectedIndex(i);
        }
    }
    
    public int getSelectedIndex()
    {
        if(items == null) return NO_SELECTION;
        return this.selectedItemIndex;
    }

    /** index of start item on selected tab */
    public int getFirstDisplayedIndex()
    {
        if(items == null) return NO_SELECTION;
        this.refreshContentCoordinatesIfNeeded();
        return this.selectedTabIndex * this.itemsPerTab;
    }
    
    /** index of start item on selected tab, EXCLUSIVE of the last item */
    public int getLastDisplayedIndex()
    {
        if(items == null) return NO_SELECTION;
        this.refreshContentCoordinatesIfNeeded();
        return Useful.min((this.selectedTabIndex + 1) * this.itemsPerTab, this.items.size());
    }
    
    /** 
     * If the currently selected item is on the current tab, is the 0-based position within the tab.
     * Returns NO_SELECTION if the currently selected item is not on the current tab or if no selection.
     */
    public int getHighlightIndex()
    {
        if(items == null || this.selectedItemIndex == NO_SELECTION) return NO_SELECTION;
        this.refreshContentCoordinatesIfNeeded();
        int result = this.selectedItemIndex - this.getFirstDisplayedIndex();
        return (result < 0 || result >= this.getItemsPerTab()) ? NO_SELECTION : result;
    }
    
    /** moves the tab selection to show the currently selected item */
    public void showSelected()
    {
        //can't implement here because layout may not be set when called - defer until next refresh
        this.focusOnSelection = true;
        this.isDirty = true;
    }
    
    protected double actualItemSize() 
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.actualItemSize;
    } 
    
    /** {@link #actualItemSize()} rounded down to nearest integer */
    protected int actualItemPixels() 
    {
        this.refreshContentCoordinatesIfNeeded();
        return this.actualItemPixels;
    } 
    
    public int getTabMargin()
    {
        return tabMargin;
    }

    public  TabBar<T> setTabMargin(int tabMargin)
    {
        this.tabMargin = tabMargin;
        this.setDirty();
        return this;
    }

    public int getTabWidth()
    {
        return tabWidth;
    }

    public  TabBar<T> setTabWidth(int tabWidth)
    {
        this.tabWidth = tabWidth;
        this.setDirty();
        return this;
    }

    public int getItemSpacing()
    {
        return itemSpacing;
    }

    public TabBar<T> setItemSpacing(int itemSpacing)
    {
        this.itemSpacing = itemSpacing;
        this.setDirty();
        return this;
    }

    /** 
     * If false, user can click on items but not select them.
     * Selection index is always {@link #NO_SELECTION} and no selection highlight is drawn.
     */
    public boolean isSelectionEnabled()
    {
        return allowSelection;
    }

    /** see {@link #isSelectionEnabled()} */
    public TabBar<T> setSelectionEnabled(boolean allowSelection)
    {
        this.allowSelection = allowSelection;
        return this;
    }

    /**
     * Pixels distance from item used to draw item borders for hovered/selected items.
     * Default is 2.
     */
    public int getItemSelectionMargin()
    {
        return itemSelectionMargin;
    }

    /**
     * See {@link #getItemSelectionMargin()}
     */
    public TabBar<T> setItemSelectionMargin(int itemSelectionMargin)
    {
        this.itemSelectionMargin = Math.max(0, itemSelectionMargin);
        return this;
    }

    /**
     * Pixels reserved under each item for labeling.
     * Subclass should draw the label.
     * Default is 0.
     */
    public int getCaptionHeight()
    {
        return captionHeight;
    }

    /**
     * See {@link #getCaptionHeight()}
     */
    public TabBar<T>  setCaptionHeight(int captionHeight)
    {
        this.captionHeight = captionHeight;
        return this;
    }
}
