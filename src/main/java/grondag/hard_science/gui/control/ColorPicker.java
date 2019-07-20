package grondag.hard_science.gui.control;

import grondag.exotic_matter.model.color.BlockColorMapProvider;
import grondag.exotic_matter.model.color.Chroma;
import grondag.exotic_matter.model.color.ColorMap;
import grondag.exotic_matter.model.color.Hue;
import grondag.exotic_matter.model.color.Luminance;
import grondag.exotic_matter.model.color.ColorMap.EnumColorMap;
import grondag.exotic_matter.varia.Color;
import grondag.hard_science.gui.GuiUtil;
import grondag.hard_science.gui.IGuiRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ColorPicker extends GuiControl<ColorPicker>
{
    private Hue selectedHue = Hue.AZURE;
    private Chroma selectedChroma = null;

    private int colorMapID = 0;


    private double centerX;
    private double centerY;
    private double radiusInner;
    private double radiusOuter;
    private double arc = 360.0 / Hue.COUNT;

    private double gridLeft;
    private double gridTop;
    private double gridIncrementX;
    private double gridIncrementY;

    public Hue getHue() { return selectedHue; }
    public void setHue(Hue h) { selectedHue = h; }
    public int getColorMapID() { return colorMapID; }
    
    public boolean showLampColors = false;
    
    public void setColorMapID( int colorMapID ) 
    { 
        this.colorMapID = colorMapID;
        this.selectedHue = BlockColorMapProvider.INSTANCE.getColorMap(colorMapID).hue;
        this.selectedChroma = BlockColorMapProvider.INSTANCE.getColorMap(colorMapID).chroma;
    }

    public ColorPicker()
    {
        this.setAspectRatio(height(1.0));
    }

    @Override
    protected void drawContent(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        for(int h = 0; h < Hue.COUNT; h++)
        {
            double radius = (h == this.selectedHue.ordinal()) ? radiusOuter : radiusInner;
            double arcStart = Math.toRadians(arc * h);
            double arcEnd = Math.toRadians(arc * (h + 1));

            double x0 =  centerX;// + Math.sin(arcStart) * radiusInner;
            double y0 =  centerY;// + Math.cos(arcStart) * radiusInner;

            double x1 =  centerX + Math.sin(arcStart) * radius;
            double y1 =  centerY + Math.cos(arcStart) * radius;

            double x2 =  centerX + Math.sin(arcEnd) * radius;
            double y2 =  centerY + Math.cos(arcEnd) * radius;

            double x3 =  centerX;// + Math.sin(arcEnd) * radiusInner;
            double y3 =  centerY;// + Math.cos(arcEnd) * radiusInner;

            GuiUtil.drawQuad(x0, y0, x1, y1, x2, y2, x3, y3, Hue.VALUES[h].hueSample());
        }

        double left;
        double top = this.gridTop;
        double right;
        double bottom;
        
        EnumColorMap map = this.showLampColors ? EnumColorMap.LAMP : EnumColorMap.BASE;
        
        for(int l = 0; l < Luminance.COUNT; l++)
        {
            bottom = top + this.gridIncrementY;
            left = this.gridLeft;
            for(int c = 0 ; c < Chroma.COUNT; c++)
            {
                right = left + this.gridIncrementX;
                ColorMap colormap = BlockColorMapProvider.INSTANCE.getColorMap(selectedHue, Chroma.VALUES[c], Luminance.VALUES[l]);
                if(colormap != null)
                {
                    GuiUtil.drawRect(left, top, right, bottom, colormap.getColor(map));
                }
                left = right;
            }
            top = bottom;
        }

        ColorMap selectedColormap = BlockColorMapProvider.INSTANCE.getColorMap(this.colorMapID);

        double sLeft = this.gridLeft + selectedColormap.chroma.ordinal() * this.gridIncrementX;
        double sTop = this.gridTop + selectedColormap.luminance.ordinal() * this.gridIncrementY;

        GuiUtil.drawRect(sLeft - 1, sTop - 1, sLeft + this.gridIncrementX + 1, sTop + this.gridIncrementY + 1, this.showLampColors ? Color.BLACK : Color.WHITE);
        GuiUtil.drawRect(sLeft - 0.5, sTop - 0.5, sLeft + this.gridIncrementX + 0.5, sTop + this.gridIncrementY + 0.5, selectedColormap.getColor(map));
    }

    private void changeHueIfDifferent(Hue newHue)
    {
        if(newHue != this.selectedHue)
        {
            this.selectedHue = newHue;

            ColorMap currentMap = BlockColorMapProvider.INSTANCE.getColorMap(this.colorMapID);
            Chroma currentChroma = this.selectedChroma;
            Luminance currentLuminance = currentMap.luminance;

            ColorMap newMap = BlockColorMapProvider.INSTANCE.getColorMap(
                    newHue, currentChroma, currentLuminance);

            while(newMap == null)
            {
                currentChroma = Chroma.VALUES[currentChroma.ordinal() - 1];
                newMap = BlockColorMapProvider.INSTANCE.getColorMap(
                        newHue, currentChroma, currentLuminance);
            }

            this.colorMapID = newMap.ordinal;
        }
    }
    
    @Override
    protected void handleMouseClick(Minecraft mc, int mouseX, int mouseY, int clickedMouseButton)
    {
        double distance = Math.sqrt((Math.pow(mouseX - centerX, 2) + Math.pow(mouseY - centerY, 2)));

        if(distance < this.radiusOuter + 2)
        {
            double angle = Math.toDegrees(Math.atan2(mouseX - centerX, mouseY - centerY));
            if(angle < 1) angle += 360;
            int index = (int) Math.floor(angle / this.arc);

            if(index >= Hue.COUNT) index = 0;

            this.changeHueIfDifferent(Hue.VALUES[index]);
         
        }
        else if(mouseX >= this.gridLeft)
        {
            int l = (int) Math.floor((mouseY - this.gridTop) / this.gridIncrementY);
            int c = (int) Math.floor((mouseX - this.gridLeft) / this.gridIncrementX);

            if(l >= 0 && l <  Luminance.COUNT 
                    && c >= 0 && c < Chroma.COUNT )
            {
                ColorMap testMap = BlockColorMapProvider.INSTANCE.getColorMap(
                        this.selectedHue, 
                        Chroma.VALUES[c], 
                        Luminance.VALUES[l]);

                if(testMap != null)
                {
                    this.colorMapID = testMap.ordinal;
                    this.selectedChroma = testMap.chroma;
                }
            }
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
        int inc = this.mouseIncrementDelta();
        if(inc != 0)
        {
            int ord = this.selectedHue.ordinal() + inc;
            if(ord < 0) 
                ord = Hue.COUNT - 1;
            else if(ord >= Hue.COUNT) 
                ord = 0;
            this.changeHueIfDifferent(Hue.VALUES[ord]);
        }
    }

    @Override
    protected void handleCoordinateUpdate()
    {
        this.radiusOuter = outerRadius(this.height);
        this.centerX = this.left + this.radiusOuter;
        this.centerY = this.top + this.radiusOuter;
        this.radiusInner = innerRadius(height);

        this.gridIncrementX = (this.width - this.height) / (Chroma.COUNT + 1);
        this.gridIncrementY = this.radiusInner * 2 / Luminance.COUNT;
        this.gridLeft = this.left + this.height + this.gridIncrementX;
        this.gridTop = this.centerY - radiusInner;
    }
    
    private static double outerRadius(double height) { return height / 2.0; }
    private static double innerRadius(double height) { return outerRadius(height) * 0.85; }
//    private static double gridIncrement(double height) { return innerRadius(height) * 2 / Luminance.values().length; }
//    private static double pixelWidth(double height) { return height + gridIncrement(height) * (Chroma.values().length + 1); }
    
    private static double height(double width)
    {
         /**
          * w = h  + gi(h) * (cvl + 1)
          * h + gi(h) * (cvl + 1) = w
          * h + innerRadius(h) * 2 / lvl * (cvl + 1) = w
          * h + outerRadius(h) * 0.85 * 2 / lvl * (cvl + 1) = w
          * h + h / 2 * 0.85 * 2 / lvl * (cvl + 1) = w
          * h + h (0.85  / lvl * (cvl + 1)) = w
          * h * (1 + 0.85  / lvl * (cvl + 1)) = w
          * h = w / (1 + 0.85  / lvl * (cvl + 1))
          */
        return width / (1.0 + 0.85 / Luminance.COUNT * (Chroma.COUNT + 1));
    }
    
    @Override 
    public ColorPicker setWidth(double width)
    {
        // pixelWidth is always derived from height, so have to work backwards to correct height value
        return this.setHeight(height(width));
    }
    @Override
    public void drawToolTip(IGuiRenderContext renderContext, int mouseX, int mouseY, float partialTicks)
    {
        // TODO Auto-generated method stub
        
    }
}
